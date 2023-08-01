/*
 * Copyright (c) 2021 Juby210 & Vendicated
 * Licensed under the Open Software License version 3.0
 */

package com.aliucord.installer;

import android.Manifest;
import android.content.Intent;
import android.content.pm.*;
import android.content.res.AssetManager;
import android.net.Uri;
import android.os.*;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.FileProvider;

import com.aliucord.libzip.Zip;

import java.io.File;
import java.util.*;

import io.flutter.embedding.android.FlutterActivity;
import io.flutter.plugin.common.BinaryMessenger;
import io.flutter.plugin.common.MethodChannel;

public final class MainActivity extends FlutterActivity {
    public MethodChannel channel;
    public MethodChannel.Result permResult;

    @Override
    @SuppressWarnings("ResultOfMethodCallIgnored")
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        BinaryMessenger binaryMessenger = Objects.requireNonNull(getFlutterEngine()).getDartExecutor().getBinaryMessenger();
        channel = new MethodChannel(binaryMessenger, "main");

        MethodChannel updaterChannel = new MethodChannel(binaryMessenger, "updater");
        Handler handler = new Handler(Looper.getMainLooper());
        Action1<String> updater = state -> {
            Log.d("Aliucord Installer", state);
            handler.post(() -> updaterChannel.invokeMethod("updateState", state));
        };

        channel.setMethodCallHandler((methodCall, result) -> {
            switch (methodCall.method) {
                case "checkPermissions":
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        String perm = Manifest.permission.WRITE_EXTERNAL_STORAGE;
                        if (checkSelfPermission(perm) != PackageManager.PERMISSION_GRANTED) {
                            requestPermissions(new String[]{ perm }, 1);
                            permResult = result;
                            break;
                        }
                    }
                    result.success(true);
                    break;
                case "getFreeSpace":
                    StatFs statFs = new StatFs(Environment.getDataDirectory().getPath());
                    long bytesAvailable = statFs.getBlockSizeLong() * statFs.getBlockCountLong();
                    result.success(bytesAvailable / (1024f * 1024f));
                    break;
                case "getVersionCode":
                    result.success(BuildConfig.VERSION_CODE);
                    break;
                case "getVersionName":
                    result.success(BuildConfig.VERSION_NAME);
                    break;
                case "toast":
                    Toast.makeText(this, methodCall.arguments(), Toast.LENGTH_SHORT).show();
                    result.success(null);
                    break;

                case "getInstalledDiscordApps":
                    PackageManager pm = getPackageManager();
                    new Thread(() -> {
                        List<ApplicationInfo> apps = pm.getInstalledApplications(PackageManager.GET_META_DATA);

                        List<Map<String, Object>> discordApps = new ArrayList<>();
                        for (ApplicationInfo info : apps) {
                            if (!info.packageName.equals("com.discord") &&
                                    !info.packageName.equals("com.aliucord") &&
                                    !info.packageName.startsWith("com.cutthecord")) continue;

                            discordApps.add(Utils.appInfoToMap(pm, info, true));
                        }

                        handler.post(() -> result.success(discordApps));
                    }).start();
                    break;
                case "getApkInfo":
                    String apkPath = methodCall.arguments();
                    PackageInfo info = getPackageManager().getPackageArchiveInfo(apkPath, 0);
                    if (info == null) result.success(null);
                    else {
                        Map<String, Object> map = new HashMap<>();
                        map.put("apkPath", apkPath);
                        map.put("packageName", info.packageName);
                        map.put("versionCode", info.versionCode);
                        map.put("versionName", info.versionName);
                        result.success(map);
                    }
                    break;

                case "patchApk":
                    new Thread(() -> {
                        String path = methodCall.argument("path");
                        if (path == null) return;
                        File outApkDir = new File(Environment.getExternalStorageDirectory(), "Aliucord");
                        if (!outApkDir.exists()) outApkDir.mkdirs();
                        String outApk = outApkDir.getAbsolutePath() + "/Aliucord.apk";

                        try {
                            File aliucordDex = new File(getFilesDir(), "classes.dex");

                            // NOTE: some files that may be not replaced if using aliucord as base (and currently are):
                            // icon files, AndroidManifest.xml, classes5.dex (pine classes)

                            updater.call("Copying original apk (" + path + ")");
                            File outApkFile = new File(outApk);
                            Utils.copyFile(new File(path), outApkFile);

                            updater.call("Repacking apk");
                            Zip zip = new Zip(outApk, 6, 'r');

                            boolean patched = false;
                            int j = zip.getTotalEntries();
                            for (int i = 0; i < j; i++) {
                                zip.openEntryByIndex(i);
                                String name = zip.getEntryName();
                                if (name.equals("classes5.dex")) patched = true;
                                zip.closeEntry();
                            }

                            String cacheDir = getCacheDir().getAbsolutePath();
                            if (!patched) for (int i = 1; i <= 3; i++) {
                                zip.openEntry("classes" + (i == 1 ? "" : i) + ".dex");
                                zip.extractEntry(cacheDir + "/classes" + (i + 1) + ".dex");
                                zip.closeEntry();
                            }
                            zip.close();

                            zip = new Zip(outApk, 6, 'a');
                            if (patched) {
                                zip.deleteEntry("classes.dex");
                                zip.deleteEntry("classes5.dex");
                                zip.deleteEntry("classes6.dex");

                                for (String arch : new String[] { "arm64-v8a", "armeabi-v7a", "x86", "x86_64" }) {
                                    for (String file : new String[] { "/libaliuhook.so", "/liblsplant.so", "/libc++_shared.so" }) {
                                        zip.deleteEntry("lib/" + arch + file);
                                    }
                                }
                            } else {
                                for (int i = 1; i <= 3; i++) zip.deleteEntry("classes" + (i == 1 ? "" : i) + ".dex");
                            }
                            zip.deleteEntry("AndroidManifest.xml");

                            if (!patched) for (int i = 2; i <= 4; i++) {
                                String name = "classes" + i + ".dex";
                                File cacheFile = new File(cacheDir, name);
                                zip.openEntry(name);
                                zip.compressFile(cacheFile.getAbsolutePath());
                                zip.closeEntry();
                                cacheFile.delete();
                            }

                            zip.openEntry("classes.dex");
                            zip.compressFile(aliucordDex.getAbsolutePath());
                            zip.closeEntry();

                            AssetManager assets = getAssets();

                            zip.openEntry("AndroidManifest.xml");
                            zip.compressFile(getFilesDir().getAbsolutePath() + "/AndroidManifest.xml");
                            zip.closeEntry();

                            Utils.writeEntry(zip, "classes5.dex", Utils.readBytes(assets.open("aliuhook/classes.dex")));
                            for (String arch : new String[] { "arm64-v8a", "armeabi-v7a", "x86", "x86_64" }) {
                                for (String file : new String[] { "/libaliuhook.so", "/liblsplant.so", "/libc++_shared.so"}) {
                                    Utils.writeEntry(zip, "lib/" + arch + file, Utils.readBytes(assets.open("aliuhook/" + arch + file)));
                                }
                            }

                            Utils.writeEntry(zip, "classes6.dex", Utils.readBytes(assets.open("kotlin/classes.dex")));
                            zip.close();

                            if (methodCall.argument("replaceBg") != Boolean.FALSE) {
                                updater.call("Replacing icons");
                                Utils.replaceIcon(assets, outApk);
                            }
                            handler.post(() -> result.success(null));
                        } catch (Throwable e) {
                            Log.e("Aliucord Installer", null, e);
                            handler.post(() -> result.error("patchApk", e.toString(), Utils.stackTraceToString(e.getStackTrace())));
                        }
                    }).start();
                    break;
                case "signApk":
                    updater.call("Signing apk file");
                    new Thread(() -> {
                        try {
                            Signer.signApk(new File(Environment.getExternalStorageDirectory(), "Aliucord/Aliucord.apk"));
                            handler.post(() -> result.success(null));
                        } catch (Throwable e) {
                            Log.e("Aliucord Installer", null, e);
                            handler.post(() -> result.error("signApk", e.toString(), Utils.stackTraceToString(e.getStackTrace())));
                        }
                    }).start();
                    break;
                case "installApk":
                    try {
                        Intent intent = new Intent(Intent.ACTION_VIEW);
                        File file = new File((String) methodCall.arguments);
                        Uri apkUri = Build.VERSION.SDK_INT > Build.VERSION_CODES.M ?
                                FileProvider.getUriForFile(this, BuildConfig.APPLICATION_ID + ".provider", file) :
                                Uri.fromFile(file);
                        intent.setDataAndType(apkUri, "application/vnd.android.package-archive");
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                        startActivity(intent);
                        result.success(null);
                    } catch (Throwable e) {
                        result.error("installApk", e.toString(), Utils.stackTraceToString(e.getStackTrace()));
                    }
                    break;
                case "checkKeystoreDeleted":
                    if (new File(Environment.getExternalStorageDirectory(), "Aliucord/ks.keystore").exists()) {
                        result.success(false);
                    } else try {
                        getPackageManager().getPackageInfo("com.aliucord", 0);
                        result.success(true);
                    } catch (PackageManager.NameNotFoundException ignored) {
                        result.success(false);
                    }
                    break;
                case "uninstallAliucord":
                    Intent intent = new Intent(Intent.ACTION_DELETE);
                    intent.setData(Uri.parse("package:com.aliucord"));
                    startActivity(intent);
                    result.success(null);
                    break;
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == 1 && permResult != null) {
            permResult.success(grantResults[0] == PackageManager.PERMISSION_GRANTED);
            permResult = null;
        }
    }
}
