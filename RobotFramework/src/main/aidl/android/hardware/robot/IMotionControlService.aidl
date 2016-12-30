package android.hardware.robot;

import android.hardware.robot.IMotionControlStateListener;
/** @hide */
interface IMotionControlService {

    /**
    * 初始化电机，连接电机
    */
	void connect(int keepPowerON);
    /**
    * 反初始化电机 断开电机链接
    */
	void disconnect();
	/** 向左转的度数 */
	void turnLeft(int angle,int msPerDegree);
	/** 向右转的度数 */
	void turnRight(int angle,int msPerDegree);

	/** 向左转的度数 */
	void turnLeftRelative(int angle,int msPerDegree);
	/** 向右转的度数 */
	void turnRightRelative(int angle,int msPerDegree);
	
    /** 获得向右旋转的最大度数 */
	int getMaxRightAngle();
    /** 获得向左旋转的最大度数 */
	int getMaxLeftAngle();
	/**
	 * 设置运动状态监听器
	*/
	void setMotionStateChangeListener(IMotionControlStateListener listener);

	/**左转到头*/
	void turnLeftToEnd(int msPerDegree);
	/**左转到头*/
	void turnRightToEnd(int msPerDegree);
	/**停止*/
	void stop();
	/**复位*/
	void reset(int msPerDegree);
	/**当前位置*/
	int getCurrentPosition();

    /**当前状态*/
    int getMotionState();

	/** request to start calibration*/
	void startCalibration();
}

