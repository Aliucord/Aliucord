package com.discord.utilities.lifecycle;

import android.app.Application;

import com.aliucord.injector.InjectorKt;
import com.discord.app.App;

import d0.z.d.m;

/**
 * This is a class within the Discord app that conveniently happens to be close to the
 * app's the real entrypoint, that being {@link App#onCreate()}. We can override this
 * class with Injector and call our own Injector entrypoint to load Aliucord.
 */
@SuppressWarnings("unused")
public final class ApplicationProvider {
    public static final ApplicationProvider INSTANCE = new ApplicationProvider();
    private static Application application;

    private ApplicationProvider() {}

    public Application get() {
        Application app = application;
        if (app == null) {
            m.throwUninitializedPropertyAccessException("application");
        }
        return app;
    }

    public void init(Application app) {
        m.checkNotNullParameter(app, "application");
        application = app;

        // Aliucord changed: call injector
        InjectorKt.init(app);
    }
}
