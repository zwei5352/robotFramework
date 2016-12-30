package com.roobo.hardware;

import android.content.Context;
import android.hardware.robot.IKeyEventService;
import android.hardware.robot.IKeyEventListener;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.RemoteException;
import android.util.ArrayMap;
import android.util.Log;
import android.view.KeyEvent;


import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;


/**
 * Created by weidu on 16-11-10.
 */

public class KeyEventManager {
    private static final String TAG = "KeyEventManager";

    /**
     * 头部触摸
     * scancode 
     */
    public static final int KEY_SCANDCODE_HEAD = 0x3B;
    public static final int KEY_KEYCODE_HEAD = KeyEvent.KEYCODE_F1;
    /**
     * 左边触摸
     */
    public static final int KEY_SCANDCODE_LEFT = 0x3C;
    public static final int KEY_KEYCODE_LEFT = KeyEvent.KEYCODE_F2;
    /**
     * 右边触摸
     */
    public static final int KEY_SCANDCODE_RIGHT = 0x3D;
    public static final int KEY_KEYCODE_RIGHT = KeyEvent.KEYCODE_F3;
    /**
     * 近距离感应的唤醒事件
     */
    public static final int KEY_SCANDCODE_PSENSOR = 0x3E;
    public static final int KEY_KEYCODE_PSENSOR = KeyEvent.KEYCODE_F4;
    /**
     * mic的唤醒事件
     */
    
    public static final int KEY_SCANDCODE_MICWAKEUP = 0x3F;
    public static final int KEY_KEYCODE_MICWAKEUP = KeyEvent.KEYCODE_F5;
    
    /**
     * BACK按键
     */
    public static final int KEY_SCANDCODE_BACK = 0x40;
    public static final int KEY_KEYCODE_BACK = KeyEvent.KEYCODE_F6;

    private IKeyEventService mService = null;

    private static KeyEventManager manager = null;

    private final ArrayMap<KeyEvent.Callback, Handler> mCallbackMap =
            new ArrayMap<KeyEvent.Callback, Handler>();
    private KeyEventListener mKeyEventListener;

    private final Context mContext;
    private final Object mLock = new Object();

    final KeyEvent.DispatcherState mKeyDispatchState
            = new KeyEvent.DispatcherState();

    /**
     * @hide
     */
    private KeyEventManager(Context ct) {
        mContext = ct;
        Method method;
        IBinder binder = null;

        try {
            method = Class.forName("android.os.ServiceManager").getMethod(
                    "getService", String.class);
            method.setAccessible(true);
            binder = (IBinder) method.invoke(null,
                    new Object[]{"keyEventService"});
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        //binder = ServiceManager.getService("keyEventService");
        if (binder == null) {
            Log.e(TAG, "could not retrieve keyEvent service");
            throw new UnsupportedOperationException();
        }
        mService = IKeyEventService.Stub.asInterface(binder);
    }

    public static synchronized KeyEventManager getInstance(Context ct) {
        if (manager == null) {
            manager = new KeyEventManager(ct);
        }
        return manager;
    }

    public void registerKeyEventListener(KeyEvent.Callback cb, Handler handler) {

        if (handler == null) {
            Looper looper = Looper.myLooper();
            if (looper == null) {
                throw new IllegalArgumentException(
                        "No handler given, and current thread has no looper!");
            }
            handler = new Handler(looper);
        }

        synchronized (mLock) {
            Log.i(TAG, "addTouchEventListener");
            mCallbackMap.put(cb, handler);
        }

        if( mKeyEventListener == null ){
            mKeyEventListener = new KeyEventListener();
            try {
                mService.registerKeyEventListener(mKeyEventListener);
            } catch (RemoteException e) {
                Log.e(TAG, "RemoteException in registerKeyEventListener", e);
                mKeyEventListener = null;
            }
        }

    }

    public void unregisterKeyEventListener(KeyEvent.Callback cb) {

        synchronized (mLock) {
            Log.i(TAG, "removeTouchEventListener");
            mCallbackMap.remove(cb);
        }

        if (mCallbackMap.isEmpty()) {
            if( mKeyEventListener != null ){
                try {
                    mService.unregisterKeyEventListener(mKeyEventListener);
                } catch (RemoteException e) {
                    Log.e(TAG, "RemoteException in registerKeyEventListener", e);
                }
                mKeyEventListener = null;
            }
        }

    }

    private class KeyEventListener extends IKeyEventListener.Stub {

        @Override
        public IBinder asBinder() {
            return this;
        }


        @Override
        public void onKeyEvent(final KeyEvent event) {
            final int callbackCount = mCallbackMap.size();

            for (int i = 0; i < callbackCount; i++) {
                Handler handler = mCallbackMap.valueAt(i);
                final KeyEvent.Callback cb = mCallbackMap.keyAt(i);
                handler.post(
                        new Runnable() {
                            @Override
                            public void run() {
                                event.dispatch(cb, mKeyDispatchState, cb);
                            }
                        });
            }


        }


    } //TouchEventServiceListener


}
