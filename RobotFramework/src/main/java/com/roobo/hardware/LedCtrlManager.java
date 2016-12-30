package com.roobo.hardware;

import android.content.Context;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;

import android.hardware.robot.ILedControlService;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * Created by weidu on 16-11-10.
 */

public class LedCtrlManager {
    private static final String TAG = "LedCtrlManager";
    
    public static final int LED_ID_LEFT_BASE = 0;
    public static final int LED_ID_RIGHT_BASE = 16;
	
    public static final int LED_ANI_ID_STOP = 0;	// 停止当前动画
    public static final int LED_ANI_ID_BREATH = 1;  // 呼吸灯动画
    public static final int LED_ANI_ID_LOOP = 2;    // 跑马灯动画
    public static final int LED_ANI_ID_LOOP_CNT = 3; // 跑马灯动画，可以设置圈数.
    
    /**
     * use for setLedLightsBat
     */
    public static final int BLN_LEFT_MASK = 2;	
	public static final int BLN_RIGHT_MASK = 4;
	
    private static LedCtrlManager manager = null;
    private ILedControlService mService = null;

    private final Context mContext;
    private final Object mLock = new Object();

    /**
     * 20161230,add constuction.
     * @author Administrator
     *
     */
    public class LedCtrlInfo {
        public int ledId;
        public int  brightLess;
        public int delay;
        
        public LedCtrlInfo(int id , int bright , int delayMs ){
        	ledId = id;
        	brightLess = bright;
        	delay = delayMs;
        }
    }


    /**
     * @hide
     */
    private LedCtrlManager(Context ct) {
        mContext = ct;
        Method method;
        IBinder binder = null;

        try {
            method = Class.forName("android.os.ServiceManager").getMethod(
                    "getService", String.class);
            method.setAccessible(true);
            binder = (IBinder) method.invoke(null,
                    new Object[]{"ledCtrlService"});
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        //binder = ServiceManager.getService("ledCtrlService");
        if (binder == null) {
            Log.e(TAG, "could not retrieve ledCtrl service");
            throw new UnsupportedOperationException();
        }
        mService = ILedControlService.Stub.asInterface(binder);
    }

    public static synchronized LedCtrlManager getInstance(Context ct) {
        if (manager == null) {
            manager = new LedCtrlManager(ct);
        }
        return manager;
    }

    /**
     * 设置某颗灯的亮度，
     * ledNum: 灯号
     * 	左边的LED, ledNum = LED_ID_LEFT_BASE + id;
     *  右边的LED, ledNum = LED_ID_RIGHT_BASE + id; 
     *   id = 0 .. getMaxLedNum.left, 0 .. getMaxLedNum.right
     * brightness： 亮度值 ， 0..255
     * delay： 要设置的灯的亮的时长,毫秒
     * 			0: 表示一直亮。
     */
    public void setLedLight(int ledNum, int brightness, long mDelay){
        Log.d(TAG,"setLedLight:ledNum=" + ledNum
                +",brightness=" + brightness
                +",mDelay=" + mDelay );
        try {
            mService.setLedLight(ledNum,brightness,mDelay);
        } catch (RemoteException e) {
            Log.e(TAG, "RemoteException in setLedLight", e);
        }
    }
    
    /*
     * this is the service support function.
     */
    private void setLedBatInternal(int[] ctrl_values , int led_nums,boolean left ){
    	int ledMask = 0;
    	if( left ){
    		ledMask |= BLN_LEFT_MASK;  // bit 0: left leds
    	} else {
    		ledMask |= BLN_RIGHT_MASK;  // bit 1: right leds.
    	}
    	try {
            mService.setLedLightsBat( ctrl_values,led_nums,ledMask);
        } catch (RemoteException e) {
            Log.e(TAG, "RemoteException in setLedLight", e);
        }
    }
    
    private int[] gotLedCtrlValues(LedCtrlInfo[] ctrls,int lednums){
    	int total_len = lednums*3;
    	int[] ctrl_values = new int[total_len];
    	for(int i = 0; i < lednums ; i++ ){
    		ctrl_values[i*3] = ctrls[i].ledId;
    		ctrl_values[i*3+1] = ctrls[i].brightLess;
    		ctrl_values[i*3+2] = ctrls[i].delay;
    	}
    	return ctrl_values;
    }
    
    /**
     * set all left leds light at bat(one time)
     * @param ctrls
     * 		array of LedCtrlInfo,each item sets one led.
     * 		brightLess = -1 means don't change the old value.
     * 		brightLess = 0 means turn off.
     */
    public void setLeftLed(LedCtrlInfo[] ctrls){
    	int lednums = ctrls.length;
    	int max = getMaxLedNum(1);
    	if(lednums > max ){
    		lednums = max;
    	}
    	int[] ctrl_values = gotLedCtrlValues(ctrls,lednums);
    	setLedBatInternal(ctrl_values,ctrls.length,true);
    }
    
    /**
     * set all right leds light at bat(one time)
     * @param ctrls
     * 		array of LedCtrlInfo,each item sets one led.
     * 		brightLess = -1 means don't change the old value.
     * 		brightLess = 0 means turn off.
     */
    public void setRightLed(LedCtrlInfo[] ctrls){
    	int lednums = ctrls.length;
    	int max = getMaxLedNum(2);
    	if(lednums > max ){
    		lednums = max;
    	}
    	int[] ctrl_values = gotLedCtrlValues(ctrls,lednums);
    	setLedBatInternal(ctrl_values,ctrls.length,false);
    }
    
    /**
     * 获得最大灯的数量
     * the servers return numLeft at high 16bit,numRight at low 16bit.
     * left_right_all: 0 -- got left+right led number.
     * 				   1 -- got left led number
     * 				   2 -- got right led number
     */
    public int getMaxLedNum( int left_right_all ){
        int num;
        int numLeft;
        try {
            num = mService.getMaxLedNum();
        } catch (RemoteException e) {
            Log.e(TAG, "RemoteException in getMaxLedNum", e);
            num = 0;
        }
        numLeft = (num>>16);
        num &= 0XFFFF;
        if( left_right_all == 1 ){
        	return numLeft;
        } else if( left_right_all == 2){
        	return num;
        }
        return num+numLeft;
    }

    /**
     * 获得灯支持的最大亮度
     * hardware brightness: 0--255 共256 级。
     * 这个值返回的是 动画状态下 ， LED 的最大和最小亮度。
     */
    public int getMaxLedBrightness(int ledNum){
        int brightness;
        try {
            brightness = mService.getMaxLedBrightness(ledNum);
        } catch (RemoteException e) {
            Log.e(TAG, "RemoteException in getMaxLedBrightness", e);
            brightness = 0;
        }
        return brightness;
    }

    /**
     * 获得灯支持的最小亮度
     */
    public int getMinLedBrightness(int ledNum){
        int brightness;
        try {
            brightness = mService.getMinLedBrightness(ledNum);
        } catch (RemoteException e) {
            Log.e(TAG, "RemoteException in getMinLedBrightness", e);
            brightness = 0;
        }
        return brightness;
    }

    /**
     * 获得某颗灯当前的亮度
     * ledNum: 参数和 setLedLight 一样。
     */
    public int getLedLight(int ledNum){
        int curBrightness;
        try {
            curBrightness = mService.getLedLight(ledNum);
        } catch (RemoteException e) {
            Log.e(TAG, "RemoteException in getLedLight", e);
            curBrightness = 0;
        }
        return curBrightness;
    }

    /**
     * 展现制定动画
     * id:动画id : 
     * 	LED_ANI_ID_XXX.
     *  对于 LED_ANI_ID_STOP 来说，mdelay,loops 无作用。
     * mDelay:
     * 	动画每帧变化之间的时长，可以控制动画的速度。
     * ledMask:
     * 	BIT0: 1 -- 启动左边 LED 动画
     *  BIT1： 1 -- 启动右边 LED 动画
     * loops: 
     * 	只对 LED_ANI_ID_LOOP_CNT 有效，表示跑马灯转动的圈数。
     */
    public void startAnimation(int id,long mDelay,int ledMask,int loops){
        Log.d(TAG,"startAnimation:id=" + id
                +",mDelay=" + mDelay );
        try {
            mService.startAnimation(id,mDelay,ledMask,loops);
        } catch (RemoteException e) {
            Log.e(TAG, "RemoteException in startAnimation", e);
        }
    }

    /**
     * 停止当前所有电话，左边和右边的。
     * 如果需要单独控制 某一边的动画停止，可以通过 
     * startAnimation , id 传 LED_ANI_ID_STOP 来停止。
     */
    public void stopAnimation(){
        Log.d(TAG,"stopAnimation!!"  );
        try {
            mService.stopAnimation();
        } catch (RemoteException e) {
            Log.e(TAG, "RemoteException in stopAnimation", e);
        }
    }

    /**
     *  配置 LED 的属性。
     *  level ： 
     *  	0--3 表示 LED电流的大小，值越大，电流越大，灯就越亮。
     *  maxBrightness,minBrightness: 
     *  	控制 LED 动画的 最大亮度和最小亮度。
     *  	setLedLight 的亮度不受这两个控制，setLedLight 的亮度范围是 0..255.
     *  stepBrightness:
     *  	控制动画（呼吸灯）每一步变化的亮度最小值，驱动里面做了动态调整。
     */
    public int configLed(int level,int maxBrightness,
                         int minBrightness,int stepBrightness){
        Log.d(TAG,"configLed:level=" + level
                +",maxBrightness=" + maxBrightness
                +",stepBrightness=" + stepBrightness );
        int ret;
        try {
            ret = mService.configLed(level,maxBrightness,minBrightness,stepBrightness);
        } catch (RemoteException e) {
            Log.e(TAG, "RemoteException in configLed", e);
            ret = -1;
        }
        return ret;
    }

}
