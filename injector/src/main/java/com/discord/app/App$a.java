/*
 * This file is part of Aliucord, an Android Discord client mod.
 * Copyright (c) 2022 Juby210 & Vendicated
 * Licensed under the Open Software License version 3.0
 */

package com.discord.app;

import com.aliucord.injector.InjectorKt;

import kotlin.jvm.internal.DefaultConstructorMarker;

// This is a class by Discord which conveniently happens to be empty
// Thus it offers an amazing entry point for an injection since we can safely override the class
@SuppressWarnings("unused")
public final class App$a {
    static {
        InjectorKt.init();
    }

    public App$a(DefaultConstructorMarker defaultConstructorMarker) {}
}
