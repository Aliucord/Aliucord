/*
 * This file is part of Aliucord, an Android Discord client mod.
 * Copyright (c) 2022 Juby210 & Vendicated
 * Licensed under the Open Software License version 3.0
 */

package com.discord.app;

import android.app.Application;

import com.aliucord.injector.InjectorKt;

import kotlin.jvm.internal.DefaultConstructorMarker;

/**
 * This is a class within the Discord app which conveniently happens to be empty.
 * Specifically, it is an empty Companion class for the Discord app's {@link Application} entrypoint,
 * which gets initialized by the static constructor as soon as the parent class is loaded.
 * Thus, it offers an amazing entrypoint for Aliucord since we can safely override this empty class.
 */
@SuppressWarnings("unused")
public final class App$a {
    static {
        InjectorKt.init();
    }

    public App$a(DefaultConstructorMarker defaultConstructorMarker) {}
}
