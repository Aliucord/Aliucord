package com.aliucord.dexpatcher;

import java.io.InputStream;

public class DexPatcherOptions {
    public boolean clearCache = true;
    public InputStream newBg;

    @SuppressWarnings("unused")
    public DexPatcherOptions() {}
    public DexPatcherOptions(boolean clearCache) {
        this.clearCache = clearCache;
    }
}
