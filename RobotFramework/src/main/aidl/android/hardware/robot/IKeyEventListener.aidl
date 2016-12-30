
package android.hardware.robot;

import android.view.KeyEvent;

/** @hide */
interface IKeyEventListener {

    /** 触摸事件监听器， 这个监听器由我们来实现 */
    void onKeyEvent(in KeyEvent event);

}


