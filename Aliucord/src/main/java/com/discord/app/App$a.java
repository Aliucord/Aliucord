package com.discord.app;

import android.os.Bundle;
import android.util.Log;

import com.aliucord.fragments.ConfirmDialog;

import java.io.File;
import java.io.IOException;

import kotlin.jvm.internal.DefaultConstructorMarker;
import top.canyie.pine.Pine;
import top.canyie.pine.PineConfig;
import top.canyie.pine.callback.MethodHook;

public class App$a {
    static MethodHook.Unhook unhook;

    static {
        // If we get here, it means that this dex was injected directly using the Installer
        PineConfig.debug = false;
        PineConfig.debuggable = false;
        PineConfig.disableHiddenApiPolicy = false;
        PineConfig.disableHiddenApiPolicyForPlatformDomain = false;

        try {
            unhook = Pine.hook(AppActivity.class.getDeclaredMethod("onCreate", Bundle.class), new MethodHook() {
                @Override
                public void afterCall(Pine.CallFrame callFrame) {
                    var appActivity = (AppActivity) callFrame.thisObject;
                    try {
                        new ConfirmDialog()
                                .setTitle("Oops")
                                .setDescription("Aliucord was not properly installed. This probably means your updater is outdated! Please update it and reinstall Aliucord")
                                .showNow(appActivity.getSupportFragmentManager(), "ALIUCORD_INSTALLED_INCORRECTLY");

                        unhook.unhook();
                        unhook = null;
                    } catch (Throwable th) {Log.e("", "", th); }
                }
            });
        } catch (Throwable ignored) { Log.e("EEEEEK", "", ignored); }
    }

    public static void downloadLatestAliucordDex(File outputFile) throws IOException {
        // STUB
    }

    public App$a(DefaultConstructorMarker defaultConstructorMarker) {}
}
