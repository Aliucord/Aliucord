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
