package com.roobo.hardware;

import android.content.Context;

import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;

import android.hardware.robot.IMotionControlStateListener;
import android.hardware.robot.IMotionControlService;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * Created by weidu on 16-11-10.
 */

public class MotionCtrlManager {

    private static final String TAG = "MotionCtrlManager";


    private RobotMotionStateListener mRobotMotionStateListener = null;
    
    /*
     * 舵机当前状态
     */
    public static final int MOTO_STATE_IDLE = 0;	
    public static final int MOTO_STATE_TURNING_LEFT = 1;	
	public static final int MOTO_STATE_TURNING_RIGHT = 2;

    // our listerner to the service.
    RobotControlMotionStateListener mRobotControlMotionStateListener = null;
    private static MotionCtrlManager manager = null;
    private IMotionControlService mService = null;

    private final Context mContext;
    private final Object mLock = new Object();

    /**
     * @hide
     */
    private MotionCtrlManager(Context ct) {
        mContext = ct;
        Method method;
        IBinder binder = null;

        try {
            method = Class.forName("android.os.ServiceManager").getMethod(
                    "getService", String.class);
            method.setAccessible(true);
            binder = (IBinder) method.invoke(null,
                    new Object[]{"motionCtrlService"});
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        //binder = ServiceManager.getService("motionCtrlService");
        if (binder == null) {
            Log.e(TAG, "could not retrieve MotionCtrl service");
            throw new UnsupportedOperationException();
        }
        mService = IMotionControlService.Stub.asInterface(binder);
    }

    public static synchronized MotionCtrlManager getInstance(Context ct) {
        if (manager == null) {
            manager = new MotionCtrlManager(ct);
        }
        return manager;
    }

    /**
     * 初始化电机，连接电机
     * keepPOwerOn: 当电机转到指定的角度之后，是否
     * 		还保持上电状态。主要是改善舵机的启动延迟。
     * 		1： 保持上电，0：舵机转动结束，自动掉电。
     * 	如果connect的时候设置了这个参数，那么只有在 disconnect的时候
     * 才给舵机掉电。stop的时候只是停止转动，并不会掉电。
     */
    public void connect(int keepPOwerOn){
        Log.d(TAG,"connect!!"  );
        try {
            mService.connect(keepPOwerOn);
        } catch (RemoteException e) {
            Log.e(TAG, "RemoteException in connect", e);
        }
    }
    /**
     * 反初始化电机 断开电机链接
     */
    public void disconnect(){
        Log.d(TAG,"disconnect!!"  );
        try {
            mService.disconnect();
        } catch (RemoteException e) {
            Log.e(TAG, "RemoteException in disconnect", e);
        }
    }

    /** 向左转的度数
     *  angle: 
     *  	向左转的目标位置，从  0..getMaxLeftAngle
     *  	不是在当前角度上面再转的角度。
     *  msPerDegree:
     *  	每转一度的延迟，可以控制舵机的转速。范围 0..500 ms.
     *  	0 表示全速转动。
     */
    public void turnLeft(int angle,int msPerDegree){
        Log.d(TAG,"turnLeft,angle=" + angle );
        try {
            mService.turnLeft(angle,msPerDegree);
        } catch (RemoteException e) {
            Log.e(TAG, "RemoteException in turnLeft", e);
        }
    }
    /** 向右转的度数
     * angle: 
     *  	向右转的目标位置，从  0..getMaxRightAngle
     *  	不是在当前角度上面再转的角度。
     *  msPerDegree:
     *  	每转一度的延迟，可以控制舵机的转速。范围 40..500 ms.
     *  	0 表示全速转动。
     */
    public void turnRight(int angle,int msPerDegree){
        Log.d(TAG,"turnRight,angle=" + angle );
        try {
            mService.turnRight(angle,msPerDegree);
        } catch (RemoteException e) {
            Log.e(TAG, "RemoteException in turnRight", e);
        }
    }
    
    /** 相对当前的角度向左转的度数
     *  angle: 
     *  	向左转的相对角度。最总角度是在当前角度的基础上左转。
     *  	超出系统最大的左转角度部分被忽略。
     *  	
     *  msPerDegree:
     *  	每转一度的延迟，可以控制舵机的转速。范围 0..500 ms.
     *  	0 表示全速转动。
     */
    public void turnLeftRelative(int angle,int msPerDegree){
        Log.d(TAG,"turnLeftRelative,angle=" + angle );
        try {
            mService.turnLeftRelative(angle,msPerDegree);
        } catch (RemoteException e) {
            Log.e(TAG, "RemoteException in turnLeft", e);
        }
    }
    /** 相对当前角度向右转的度数
     * angle: 
     *  	向右转的相对角度。超出右转最大角度的部分被忽略。
     *  msPerDegree:
     *  	每转一度的延迟，可以控制舵机的转速。范围 40..500 ms.
     *  	0 表示全速转动。
     */
    public void turnRightRelative(int angle,int msPerDegree){
        Log.d(TAG,"turnRightRelative,angle=" + angle );
        try {
            mService.turnRightRelative(angle,msPerDegree);
        } catch (RemoteException e) {
            Log.e(TAG, "RemoteException in turnRight", e);
        }
    }
    
    /** 获得向右旋转的最大度数
     */
    public int getMaxRightAngle(){
        Log.d(TAG,"getMaxRightAngle" );
        int maxAngle;
        try {
            maxAngle = mService.getMaxRightAngle();
        } catch (RemoteException e) {
            Log.e(TAG, "RemoteException in getMaxRightAngle", e);
            maxAngle = 0;
        }
        return maxAngle;
    }

    /** 获得向左旋转的最大度数
     */
    public int getMaxLeftAngle(){
        Log.d(TAG,"getMaxLeftAngle" );
        int maxAngle;
        try {
            maxAngle = mService.getMaxLeftAngle();
        } catch (RemoteException e) {
            Log.e(TAG, "RemoteException in getMaxLeftAngle", e);
            maxAngle = 0;
        }
        return maxAngle;
    }

    /**
     * 设置运动状态监听器
     * 监视器的回调参数：
     * 	第一个是 舵机当前的状态： state
     *  第二个是舵机当前的角度：  position.
     * 
     */
    public void setMotionStateChangeListener(RobotMotionStateListener listener){
        Log.d(TAG,"setMotionStateChangeListener,listener=" + listener  );
        mRobotMotionStateListener = listener;
        if( mRobotMotionStateListener != null ){
            if( mRobotControlMotionStateListener == null ){
                mRobotControlMotionStateListener = new RobotControlMotionStateListener();
                try {
                    mService.setMotionStateChangeListener(mRobotControlMotionStateListener);
                } catch (RemoteException e) {
                    Log.e(TAG, "RemoteException in setMotionStateChangeListener", e);
                    mRobotControlMotionStateListener = null;
                }
            }
        } else {
            if( mRobotControlMotionStateListener != null ){
                try {
                    mService.setMotionStateChangeListener(null);
                } catch (RemoteException e) {
                    Log.e(TAG, "RemoteException in setMotionStateChangeListener", e);
                }
                mRobotControlMotionStateListener = null;
            }
        }
    }

    /**
     * 左转到头
     * 从当前位置（任何位置）左转到头，即转到  getMaxLeftAngle 
     * msPerDegree：
     * 		控制转速，参数和 turnRight 说明一样。
     */
    public void turnLeftToEnd(int msPerDegree) {
        Log.d(TAG,"turnLeftToEnd" );
        try {
            mService.turnLeftToEnd(msPerDegree);
        } catch (RemoteException e) {
            Log.e(TAG, "RemoteException in turnLeftToEnd", e);
        }
    }
    /**右转到头
     * 从当前位置（任何位置）右转到头，即转到  getMaxRightAngle 
     */
    public void turnRightToEnd(int msPerDegree){
        Log.d(TAG,"turnRightToEnd" );
        try {
            mService.turnRightToEnd(msPerDegree);
        } catch (RemoteException e) {
            Log.e(TAG, "RemoteException in turnRightToEnd", e);
        }
    }

    /**
     * 停止舵机转动，舵机停在当前位置。
     * 如果舵机已经停止，改函数无作用。
     */
    public void stop(){
        Log.d(TAG,"stop" );
        try {
            mService.stop();
        } catch (RemoteException e) {
            Log.e(TAG, "RemoteException in stop", e);
        }
    }
    /**复位
     * 让舵机转到中间 0 度位置。然后停止。
     * 20161203，增加 int msPerDegree 参数，控制
     * 复位的转速。
     */
    public void reset(int msPerDegree){
        Log.d(TAG,"reset" );
        try {
            mService.reset(msPerDegree);
        } catch (RemoteException e) {
            Log.e(TAG, "RemoteException in reset", e);
        }
    }

    /**
     * 获取舵机当前的绝对位置。
     * 返回值：
     * 		< 0 ,表示 左转的角度，从 -getMaxLeftAngle .. 0
     * 		=0  ,表示在中间位置。
     * 		> 0 ,表示右转的角度，从 0 .. getMaxRightAngle.
     */
    public int getCurrentPosition(){
        Log.d(TAG,"getCurrentPosition" );
        int curDegree;
        try {
            curDegree = mService.getCurrentPosition();
        } catch (RemoteException e) {
            Log.e(TAG, "RemoteException in getCurrentPosition", e);
            curDegree = 0;
        }
        return curDegree;
    }

    /**获取舵机当前状态
     * 返回值： 
     * 	MOTO_STATE_IDLE 或者 MOTO_STATE_TURNING_LEFT 或者 MOTO_STATE_TURNING_RIGHT
     */
    public int getMotionState(){
        Log.d(TAG,"getMotionState" );
        int state;
        try {
            state = mService.getMotionState();
        } catch (RemoteException e) {
            Log.e(TAG, "RemoteException in getMotionState", e);
            state = 0;
        }
        return state;
    }
    
    /**
     * start calibration. the calibration will make the 
     * MOTO turn to 0 degree,left-max degree,and then right-max 
     * degree,and record the adc-value for checking real degree.
     */
    public void startCalibration(){
        Log.d(TAG,"reset" );
        try {
            mService.startCalibration();
        } catch (RemoteException e) {
            Log.e(TAG, "RemoteException in reset", e);
        }
    }
    
    /**
     * 状态监测回调函数
     * @param state
     * 	当前状态，见 getMotionState.
     * @param position
     * 	当前角度，见 getCurrentPosition
     */
    private void onStateChange(int state,int position){
        Log.d(TAG,"MotionStateChange:position=" + position );
        if( mRobotMotionStateListener != null ){
            mRobotMotionStateListener.onStateChange(state, position);
        }
    }

    public interface RobotMotionStateListener {
        /**
         * call when robot-special key event happend!!
         * state > 403 ,运动出错, 代表错误码
         */
        void onStateChange(int state,int position);
    }

    private final class RobotControlMotionStateListener extends IMotionControlStateListener.Stub {
        @Override
        public void onStateChange(int state,int position) throws RemoteException {
            MotionCtrlManager.this.onStateChange(state,position);
        }
    }
}
