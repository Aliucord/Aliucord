/*
 * Copyright (c) 2021 Juby210
 * Licensed under the Open Software License version 3.0
 */

package com.aliucord.dexpatcher;

public final class DexPatcherOptions {
    public boolean clearCache = true;
    public boolean replaceIcon = true;

    @SuppressWarnings("unused")
    public DexPatcherOptions() {}
    public DexPatcherOptions(boolean clearCache) {
        this.clearCache = clearCache;
    }
}
