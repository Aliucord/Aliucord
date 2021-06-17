/*
 * Copyright (c) 2021 Juby210
 * Licensed under the Open Software License version 3.0
 */

package com.aliucord.wrappers;

import com.discord.api.permission.PermissionOverwrite;

/**
 * Wraps the obfuscated {@link PermissionOverwrite} class to provide nice method names and require only one central
 * update if method names change after an update
 */
@SuppressWarnings({"unused", "deprecation"})
public class PermissionOverwriteWrapper {
    private final PermissionOverwrite overwrite;

    public PermissionOverwriteWrapper(PermissionOverwrite permissionOverwrite) {
        overwrite = permissionOverwrite;
    }

    /** Returns the raw (obfuscated) {@link PermissionOverwrite} Object associated with this wrapper */
    public PermissionOverwrite raw() {
        return overwrite;
    }

    public long getId() {
        return getId(overwrite);
    }

    public long getAllowed() {
        return getAllowed(overwrite);
    }

    public long getDenied() {
        return getDenied(overwrite);
    }

    public PermissionOverwrite.Type getType() {
        return getType(overwrite);
    }



    public static long getId(PermissionOverwrite overwrite) {
        return overwrite.a();
    }

    public static long getAllowed(PermissionOverwrite overwrite) {
        return overwrite.c();
    }

    public static long getDenied(PermissionOverwrite overwrite) {
        return overwrite.d();
    }

    public static PermissionOverwrite.Type getType(PermissionOverwrite overwrite) {
        return overwrite.f();
    }
}
