package android.hardware.robot;

/** @hide */
interface IMotionControlStateListener {
	
	/** 运动状态监听器
	  * state is enum:
	  * MOTION_COMPLETE = 400, 运动完成
	  * MOTION_LEFT_END = 401, 向左旋转到了终点
	  * MOTION_RIGHT_END = 402 向右旋转到了终点
      * state > 403 ,运动出错, 代表错误码
	  * position:当前位置
	  */
	void onStateChange(int state,int position);

}

