package org.webrtc;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.SOURCE)
public @interface VideoFrameBufferType {
    int NATIVE = 0;
    int I420 = 1;
    int I420A = 2;
    int I422 = 3;
    int I444 = 4;
    int I010 = 5;
    int I210 = 6;
    int I410 = 7;
    int NV12 = 8;
}
