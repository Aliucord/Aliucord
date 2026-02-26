package org.webrtc;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.SOURCE)
/* loaded from: classes5.dex */
public @interface NetworkPreference {
    int NEUTRAL = 0;
    int NOT_PREFERRED = -1;
}
