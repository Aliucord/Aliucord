package com.aliucord.installer;

import android.Manifest;
import android.content.ComponentName;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.*;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.browser.customtabs.*;
import androidx.core.content.FileProvider;

import com.aliucord.dexpatcher.*;

import java.io.File;
import java.util.*;

import dalvik.system.DexClassLoader;
import io.flutter.embedding.android.FlutterActivity;
import io.flutter.plugin.common.BinaryMessenger;
import io.flutter.plugin.common.MethodChannel;

public final class MainActivity extends FlutterActivity {
    public MethodChannel channel;
    public MethodChannel.Result permResult;

    @Override
    @SuppressWarnings({"unchecked", "ResultOfMethodCallIgnored"})
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        BinaryMessenger binaryMessenger = Objects.requireNonNull(getFlutterEngine()).getDartExecutor().getBinaryMessenger();
        channel = new MethodChannel(binaryMessenger, "main");

        MethodChannel updaterChannel = new MethodChannel(binaryMessenger, "updater");
        Handler handler = new Handler(Looper.getMainLooper());
        StateUpdater updater = state -> {
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
                case "getGitRev":
                    result.success(BuildConfig.GIT_REVISION);
                    break;
                case "openChromeCustomTab":
                    Uri uri = Uri.parse((String) methodCall.arguments);
                    String packageName = CustomTabsHelper.getPackageNameToUse(this);
                    if (packageName != null) {
                        CustomTabsServiceConnection serviceConnection = new CustomTabsServiceConnection() {
                            @Override
                            public void onCustomTabsServiceConnected(@NonNull ComponentName name, @NonNull CustomTabsClient client) {
                                CustomTabsIntent.Builder tabBuilder = new CustomTabsIntent.Builder();
                                CustomTabsIntent intent = tabBuilder.build();
                                client.warmup(0L);
                                intent.launchUrl(getContext(), uri);
                            }
                            public void onServiceDisconnected(ComponentName name) {}
                        };
                        CustomTabsClient.bindCustomTabsService(this, packageName, serviceConnection);
                    } else {
                        startActivity(new Intent(Intent.ACTION_VIEW, uri));
                    }
                    result.success(null);
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
                        File outApkDir = new File(Environment.getExternalStorageDirectory(), "Aliucord");
                        if (!outApkDir.exists()) outApkDir.mkdirs();
                        String outApk = outApkDir.getAbsolutePath() + "/Aliucord.apk";

                        try {
                            File aliucordDex = new File(getFilesDir(), "classes5.dex");
                            ClassLoader loader = new DexClassLoader(
                                    aliucordDex.getAbsolutePath(),
                                    getCacheDir().getAbsolutePath(),
                                    null,
                                    getClassLoader()
                            );
                            Map<String, List<String>> classes = (Map<String, List<String>>) loader
                                    .loadClass("com.aliucord.Main")
                                    .getMethod("getClassesToPatch")
                                    .invoke(null);

                            DexPatcherOptions options = new DexPatcherOptions(Objects.requireNonNull(methodCall.argument("clearCache")));
                            if (methodCall.argument("replaceBg") == Boolean.TRUE) options.newBg = getAssets().open("bg.png");
                            DexPatcher patcher = new DexPatcher(this, updater, options);
                            patcher.patchApk(methodCall.argument("path"), classes, outApk, true, aliucordDex);
                            handler.post(() -> result.success(null));
                        } catch (Throwable e) {
                            Log.e("Aliucord Installer", null, e);
                            handler.post(() -> result.error("patchApk", e.getMessage(), Utils.stackTraceToString(e.getStackTrace())));
                        }
                    }).start();
                    break;
                case "signApk":
                    new Thread(() -> {
                        try {
                            Signer.signApk(new File(Environment.getExternalStorageDirectory(), "Aliucord/Aliucord.apk"), updater);
                            handler.post(() -> result.success(null));
                        } catch (Throwable e) {
                            Log.e("Aliucord Installer", null, e);
                            handler.post(() -> result.error("signApk", e.getMessage(), Utils.stackTraceToString(e.getStackTrace())));
                        }
                    }).start();
                    break;
                case "clearCache":
                    new Thread(() -> com.aliucord.dexpatcher.Utils.delete(new File(getCacheDir(), "patcher"))).start();
                    result.success(null);
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
                        result.error("installApk", e.getMessage(), Utils.stackTraceToString(e.getStackTrace()));
                    }
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

    @Override
    protected void onNewIntent(@NonNull Intent intent) {
        super.onNewIntent(intent);

        if (channel == null) return;
        Uri data = intent.getData();
        if (data == null) return;
        String code = data.getQueryParameter("code");
        if (channel != null && code != null) channel.invokeMethod("authCallback", code);
    }
}
