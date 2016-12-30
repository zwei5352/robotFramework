package android.hardware.robot;

/** @hide */
interface ILedControlService {

	/**---------LED CONTROL ------------*/
	/**
    * 设置某颗灯的亮度，
    * ledNum: 灯号
    * brightness： 亮度值
    * delay： 要设置的灯的亮的时长,毫秒
	*/
	void setLedLight(int ledNum, int brightness, long mDelay);
    /**
    * 获得最大灯的数量
    */
	int getMaxLedNum();
    /**
    * 获得灯支持的最大亮度
    */
	int getMaxLedBrightness(int ledNum);
    /**
    * 获得灯支持的最小亮度
    */
	int getMinLedBrightness(int ledNum);
    /**
     * 获得某颗灯当前的亮度
     */
	int getLedLight(int ledNum);

    /**
    * 展现制定动画
    * id:动画id
    * 时长（毫秒）,传入0则循环显示
    * 20161103,we have left/right leds,so add a ledmask.
    * bit 0: control left ,bit1 control right
    */
    void startAnimation(int id,long mDelay,int ledMask, int loops);

    /*停止所有动画*/
    void stopAnimation();

    int configLed(int level,int maxBrightness,int minBrightness,int stepBrightness);
	
	/**
    * 20161228,set led-light in batch.
    * ctrl_values: is the led ctrl values,include:
    *	every led: ledId,brightness,delayms 
    * ledNum: the valid ledNum at ctrl_values
    * ledMask:bit 0: control left ,bit1 control right
	*/
	void setLedLightsBat(in int[] ctrl_values,int ledNum, int ledMask);
}


