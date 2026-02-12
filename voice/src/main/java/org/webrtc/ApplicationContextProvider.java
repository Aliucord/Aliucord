package org.webrtc;

import android.content.Context;

/** @noinspection unused */
public class ApplicationContextProvider {
    @CalledByNative
    public static Context getApplicationContext() {
        return ContextUtils.getApplicationContext();
    }
}
