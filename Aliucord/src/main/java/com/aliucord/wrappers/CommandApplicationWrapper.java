/*
 * Copyright (c) 2021 Juby210 & Vendicated
 * Licensed under the Open Software License version 3.0
 */

package com.aliucord.wrappers;

import com.discord.api.commands.Application;

/**
 * Wraps the obfuscated {@link Application} class to provide nice method names and require only one central
 * update if method names change after an update
 */
@SuppressWarnings({"unused", "deprecation"})
public class CommandApplicationWrapper {
    private final Application application;

    public CommandApplicationWrapper(Application application) {
        this.application = application;
    }

    /** Returns the raw (obfuscated) {@link Application} Object associated with this wrapper */
    public final Application raw() {
        return application;
    }

    public final int getCommandCount() {
        return getCommandCount(application);
    }

    public final String getIcon() {
        return getIcon(application);
    }

    public final long getId() {
        return getId(application);
    }

    public final String getName() {
        return getName(application);
    }



    public static int getCommandCount(Application application) {
        return application.a();
    }

    public static String getIcon(Application application) {
        return application.b();
    }

    public static long getId(Application application) {
        return application.c();
    }

    public static String getName(Application application) {
        return application.d();
    }
}
