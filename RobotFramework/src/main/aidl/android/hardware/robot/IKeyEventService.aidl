// IKeyEventService.aidl
package android.hardware.robot;

import android.hardware.robot.IKeyEventListener;

// Declare any non-default types here with import statements
/** @hide */
interface IKeyEventService {
    void registerKeyEventListener(IKeyEventListener listener);

    void unregisterKeyEventListener(IKeyEventListener listener);
}
