package org.webrtc;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.SOURCE)
public @interface Priority {
    int VERY_LOW = 0;
    int LOW = 1;
    int MEDIUM = 2;
    int HIGH = 3;
}
