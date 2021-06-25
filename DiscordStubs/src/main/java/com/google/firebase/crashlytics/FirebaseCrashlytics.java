package com.google.firebase.crashlytics;

import androidx.annotation.NonNull;

@SuppressWarnings("unused")
public class FirebaseCrashlytics {
    @NonNull
    public static FirebaseCrashlytics getInstance() { return new FirebaseCrashlytics(); }

    public void setCrashlyticsCollectionEnabled(boolean enabled) {}
}
