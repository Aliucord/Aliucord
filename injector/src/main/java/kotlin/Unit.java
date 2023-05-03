/*
 * This file is part of Aliucord, an Android Discord client mod.
 * Copyright (c) 2022 Juby210 & Vendicated
 * Licensed under the Open Software License version 3.0
 */

package kotlin;

// This class fixes missing INSTANCE field in kotlin.Unit class
// It is missing by default because Discord already has this class and proguard changed field name
@SuppressWarnings("unused")
public final class Unit {
    public static final Unit a = new Unit();
    public static final Unit INSTANCE = a;

    public String toString() {
        return "kotlin.Unit";
    }
}
