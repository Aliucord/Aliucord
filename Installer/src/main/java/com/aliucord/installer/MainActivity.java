package com.aliucord.installer;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;
import androidx.preference.PreferenceManager;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.*;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.aliucord.dexpatcher.*;
import com.google.android.material.textfield.TextInputEditText;
import com.nbsp.materialfilepicker.MaterialFilePicker;
import com.nbsp.materialfilepicker.ui.FilePickerActivity;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Pattern;

import dalvik.system.DexClassLoader;

public class MainActivity extends AppCompatActivity {
    final String SUPPORTED_DISCORD_VERSION = "1509";
    final URL DISCORD_APK_URL = new URL("https://cdn.discordapp.com/attachments/411645018105970699/830188157390946394/Discord-70.3.apk");
    static final String DEFAULT_DEX_LOCATION = "/storage/emulated/0/Aliucord/Aliucord.dex";
    SharedPreferences prefs;
    GitHubAPI authHandler;

    public MainActivity() throws MalformedURLException {}

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        prefs = PreferenceManager.getDefaultSharedPreferences(this);
        checkPermissions(null);

        authHandler = new GitHubAPI(prefs, this);
    }

    public void checkPermissions(View view) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            String perm = "android.permission.WRITE_EXTERNAL_STORAGE";
            if (checkSelfPermission(perm) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{ perm }, 1);
                return;
            }
        }
        findViewById(R.id.grantPermissionLayout).setVisibility(View.GONE);
        findViewById(R.id.mainLayout).setVisibility(View.VISIBLE);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == 1 && grantResults.length == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED) checkPermissions(null);
    }

    public void selectFromStorage(View view) {
        new MaterialFilePicker()
                .withActivity(this)
                .withCloseMenu(true)
                .withHiddenFiles(true)
                .withFilter(Pattern.compile(".*\\.apk$"))
                .withFilterDirectories(false)
                .withTitle("Select Discord APK")
                .withRequestCode(1)
                .start();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode != RESULT_OK || data == null || requestCode != 1) return;
        ((TextInputEditText) findViewById(R.id.pathInput)).setText(data.getStringExtra(FilePickerActivity.RESULT_FILE_PATH));
    }

    public void selectFromInstalledApp(View view) {
        PackageManager pm = getPackageManager();
        List<ApplicationInfo> packages = pm.getInstalledApplications(PackageManager.GET_META_DATA);
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Select apk from installed app");

        List<ApplicationInfo> discordPackages = new ArrayList<>();
        List<Item> items = new ArrayList<>();
        for (ApplicationInfo info : packages) {
            if (!info.packageName.equals("com.discord") &&
                    !info.packageName.equals("com.aliucord") &&
                    !info.packageName.startsWith("com.cutthecord")) continue;

            Drawable icon = new WrappedDrawable(info.loadIcon(pm));
            icon.setBounds(0, 0, 100, 100);

            String label = pm.getApplicationLabel(info) + " (" + info.packageName + ")";
            String version = "unknown";
            boolean incompatible = true;
            try {
                PackageInfo packageInfo = pm.getPackageInfo(info.packageName, 0);
                version = packageInfo.versionName + " (" + packageInfo.versionCode + ")";
                if (String.valueOf(packageInfo.versionCode).startsWith(SUPPORTED_DISCORD_VERSION)) incompatible = false;
            } catch (PackageManager.NameNotFoundException ignored) {}

            items.add(new Item(icon, label, version, incompatible));
            discordPackages.add(info);
        }

        new AlertDialog.Builder(this)
                .setTitle("Select apk from installed app")
                .setAdapter(new CustomArrayAdapter(this, items), (dialog, item) ->
                        ((TextInputEditText) findViewById(R.id.pathInput)).setText(discordPackages.get(item).publicSourceDir)
                ).show();
    }

    @SuppressLint("SetTextI18n")
    public void selectDownloadAtRuntime(View view) {
        ((TextInputEditText) findViewById(R.id.pathInput)).setText(getCacheDir() + "/discord.apk");
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        if (authHandler.isAuthenticated()) {
            menu.findItem(R.id.action_github).setTitle(R.string.gh_auth_logout);
        } else {
            menu.findItem(R.id.action_github).setTitle(R.string.gh_auth);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.action_settings) {
            startActivity(new Intent(this, SettingsActivity.class));
            return true;
        }
        if (item.getItemId() == R.id.action_github) {
            authHandler.startAuthFlow();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void showError(Exception e) {
        Log.e("Aliucord Installer", "Exception while patching apk: ", e);

        StringBuilder stacktrace = new StringBuilder();
        for (StackTraceElement el : e.getStackTrace()) stacktrace.append("\n   at ").append(el.toString());

        new AlertDialog.Builder(this)
                .setTitle("Error")
                .setMessage("Exception while patching apk: " + e.toString() + "\n\nStacktrace: " + stacktrace)
                .setPositiveButton(android.R.string.ok, null)
                .show();
        findViewById(R.id.patchingLayout).setVisibility(View.GONE);
        findViewById(R.id.mainLayout).setVisibility(View.VISIBLE);
    }

    @SuppressWarnings({"ResultOfMethodCallIgnored", "unchecked", "deprecation"})
    public void install(View view) {
        TextInputEditText pathInput = findViewById(R.id.pathInput);
        String text = Objects.requireNonNull(pathInput.getText()).toString();
        boolean invalid = text.equals("");
        if (!invalid) invalid = (!(new File(text).exists()) && !text.equals(getCacheDir() + "/discord.apk"));
        if (invalid) {
            Toast.makeText(this, R.string.invalid_path, Toast.LENGTH_SHORT).show();
            return;
        }

        findViewById(R.id.mainLayout).setVisibility(View.GONE);
        findViewById(R.id.patchingLayout).setVisibility(View.VISIBLE);
        TextView patchingState = findViewById(R.id.patchingState);
        StateUpdater updater = state -> {
            Log.d("Aliucord Installer", state);
            new Handler(Looper.getMainLooper()).post(() -> patchingState.setText(state));
        };

        File outApkDir = new File(Environment.getExternalStorageDirectory(), "Aliucord");
        if (!outApkDir.exists()) outApkDir.mkdirs();
        String outApk = outApkDir.getAbsolutePath() + "/Aliucord.apk";

        new Thread(() -> {
            try {
                if (text.equals(getCacheDir() + "/discord.apk")) {
                    File internalApk = new File(getCacheDir(), "discord.apk");
                    if (internalApk.exists()) {
                        PackageManager pm = getPackageManager();
                        PackageInfo apkInfo = pm.getPackageArchiveInfo(internalApk.getAbsolutePath(), 0);

                        if (apkInfo == null || !String.valueOf(apkInfo.versionCode).startsWith(SUPPORTED_DISCORD_VERSION)) downloadDiscordApk(updater);
                    } else {
                        downloadDiscordApk(updater);
                    }
                }

                File aliucordDex = new File(getFilesDir(), "classes5.dex");
                if (prefs.getBoolean("use_dex_from_storage", false)) {
                    File dexFile = new File(prefs.getString("dex_location", DEFAULT_DEX_LOCATION));
                    if (dexFile.exists()) Utils.copyFile(dexFile, aliucordDex);
                    else copyAliucordFromAssets(aliucordDex);
                } else copyAliucordFromAssets(aliucordDex);

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

                boolean clearCache = prefs.getBoolean("clear_cache", true);
                boolean forceClear = !prefs.getString("last_patched", "").equals(SUPPORTED_DISCORD_VERSION);

                DexPatcherOptions options = new DexPatcherOptions(clearCache ? clearCache : forceClear);
                if (prefs.getBoolean("replace_bg", true)) options.newBg = getAssets().open("bg.png");
                DexPatcher patcher = new DexPatcher(this, updater, options);
                patcher.patchApk(text, classes, outApk, true, aliucordDex);
                File outFile = new File(outApk);
                Signer.signApk(outFile, updater);

                if (clearCache) Utils.delete(new File(getCacheDir(), "patcher"));
                if (forceClear) prefs.edit().putString("last_patched", SUPPORTED_DISCORD_VERSION).apply();

                Intent intent = new Intent(Intent.ACTION_VIEW);
                Uri apkUri = FileProvider.getUriForFile(
                        this,
                        getApplicationContext().getPackageName() + ".provider",
                        new File(outApkDir, "Aliucord.apk")
                );
                intent.setDataAndType(apkUri, "application/vnd.android.package-archive");
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                startActivity(intent);

                new Handler(Looper.getMainLooper()).post(() -> {
                    findViewById(R.id.patchingLayout).setVisibility(View.GONE);
                    findViewById(R.id.mainLayout).setVisibility(View.VISIBLE);
                    patchingState.setText(R.string.patching_placeholder);
                });
            } catch (Exception e) { new Handler(Looper.getMainLooper()).post(() -> showError(e)); }
        }).start();
    }

    private void copyAliucordFromAssets(File dest) throws IOException {
        Utils.copyAsset(getAssets().open("Aliucord.dex"), dest);
    }

    private void downloadDiscordApk(StateUpdater updater) throws Exception {
        updater.update("Downloading discord apk");
        HttpURLConnection connection = (HttpURLConnection) DISCORD_APK_URL.openConnection();
        connection.connect();

        if (connection.getResponseCode() != HttpURLConnection.HTTP_OK) {
            throw new Exception("Server returned code " + connection.getResponseCode());
        }

        int lengthOfFile = connection.getContentLength();
        InputStream input = new BufferedInputStream(connection.getInputStream(), 8192);
        OutputStream output = new FileOutputStream(getCacheDir() + "/discord.apk");

        byte[] data = new byte[8192];

        int count;
        long total = 0;
        int perc = 0;
        int newPerc;
        while ((count = input.read(data)) != -1) {
            total += count;
            output.write(data, 0, count);
            newPerc = (int) ((total * 100) / lengthOfFile);
            if (newPerc > perc) updater.update("Downloading discord apk.. " + newPerc + "%");
            perc = newPerc;
        }

        output.flush();
        output.close();
        input.close();
    }

    public void update(URL downloadURL) {
        findViewById(R.id.mainLayout).setVisibility(View.GONE);
        findViewById(R.id.patchingLayout).setVisibility(View.VISIBLE);
        TextView patchingState = findViewById(R.id.patchingState);
        StateUpdater updater = state -> {
            Log.d("Aliucord Installer", state);
            new Handler(Looper.getMainLooper()).post(() -> patchingState.setText(state));
        };

        updater.update("Updating... 0%");

        new Thread(() -> {
            try {
                File outApkDir = new File(Environment.getExternalStorageDirectory(), "Aliucord");
                HttpURLConnection connection = (HttpURLConnection) downloadURL.openConnection();
                connection.connect();

                if (connection.getResponseCode() != HttpURLConnection.HTTP_OK) {
                    throw new Exception("Server returned code " + connection.getResponseCode());
                }

                int lengthOfFile = connection.getContentLength();
                InputStream input = new BufferedInputStream(connection.getInputStream(), 8192);
                OutputStream output = new FileOutputStream(outApkDir.getAbsolutePath() + "/installer.apk");

                byte[] data = new byte[8192];

                int count;
                long total = 0;
                int perc = 0;
                int newPerc;
                while ((count = input.read(data)) != -1) {
                    total += count;
                    output.write(data, 0, count);
                    newPerc = (int) ((total * 100) / lengthOfFile);
                    if (newPerc > perc) updater.update("Updating... " + newPerc + "%");
                    perc = newPerc;
                }

                output.flush();
                output.close();
                input.close();

                Intent intent = new Intent(Intent.ACTION_VIEW);
                Uri apkUri = FileProvider.getUriForFile(
                        this,
                        getApplicationContext().getPackageName() + ".provider",
                        new File(outApkDir.getAbsolutePath(), "installer.apk")
                );
                intent.setDataAndType(apkUri, "application/vnd.android.package-archive");
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                startActivity(intent);

                try {
                    new Handler(Looper.getMainLooper()).post(() -> {
                        findViewById(R.id.patchingLayout).setVisibility(View.GONE);
                        findViewById(R.id.mainLayout).setVisibility(View.VISIBLE);
                        ((TextView) findViewById(R.id.patchingState)).setText(R.string.patching_placeholder);
                    });
                } catch (Exception e) { new Handler(Looper.getMainLooper()).post(() -> showError(e)); }
            } catch (Exception e) {
                new Handler(Looper.getMainLooper()).post(() -> {
                    StringBuilder stacktrace = new StringBuilder();
                    for (StackTraceElement el : e.getStackTrace()) stacktrace.append("\n   at ").append(el.toString());
            
                    new AlertDialog.Builder(this)
                            .setTitle("Error")
                            .setMessage("Exception while updating: " + e.toString() + "\n\nStacktrace: " + stacktrace)
                            .setPositiveButton(android.R.string.ok, null)
                            .show();
                });
            }
        }).start();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        String code = intent.getData().getQueryParameter("code");
        if (code != null) authHandler.intentCallback(code);
    }
}
