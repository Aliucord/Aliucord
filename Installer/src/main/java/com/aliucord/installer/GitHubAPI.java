package com.aliucord.installer;

import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.SharedPreferences;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.*;
import android.util.Log;
import android.text.TextUtils;

import androidx.annotation.NonNull;
import androidx.browser.customtabs.*;

import java.net.HttpURLConnection;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.Thread;
import java.nio.charset.StandardCharsets;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONException;

public class GitHubAPI {
    private final String client_id = "9260a23baccb4ebf2d94";
    private final String client_secret = "0ab74f8ed58600ad48acbb9843ab05615ec4940d";
    private final Uri uri = Uri.parse("https://github.com/login/oauth/authorize?client_id=" + client_id + "&redirect_uri=aliucord-installer%3A%2F%2Fauth&scope=repo");
    private URL commits_url;
    private URL contents_url;
    private final SharedPreferences prefs;
    private String auth_token;
    private final MainActivity main;

    private String sPackageNameToUse;

    public GitHubAPI (SharedPreferences _prefs, MainActivity _main) {
        try {
            commits_url = new URL("https://api.github.com/repos/aliucord/Aliucord/commits");
            contents_url = new URL("https://api.github.com/repos/Aliucord/Aliucord/contents?ref=builds");
        } catch (Throwable ignored) {}
        prefs = _prefs;
        main = _main;
        auth_token = prefs.getString("github_token", "");
        if (isAuthenticated()) new Thread(this::checkForUpdates).start();
    }

    public void startAuthFlow() {
        if (isAuthenticated()) {
            prefs.edit().remove("github_token").apply();
            auth_token = "";
            return;
        }
        
        String packageName = getPackageNameToUse();
        if (packageName != null) {
            CustomTabsServiceConnection tabConnection = new CustomTabsServiceConnection() {
                @Override
                public void onCustomTabsServiceConnected(@NonNull ComponentName componentName, CustomTabsClient tabClient) {
                    CustomTabsIntent.Builder tabBuilder = new CustomTabsIntent.Builder();
                    CustomTabsIntent intent = tabBuilder.build();
                    tabClient.warmup(0L);
                    intent.launchUrl(main, uri);
                }
            
                @Override
                public void onServiceDisconnected(ComponentName name) {}
            };
            CustomTabsClient.bindCustomTabsService(main, packageName, tabConnection);
        } else {
            main.startActivity(new Intent(Intent.ACTION_VIEW, uri));
        }
    }

    public void checkForUpdates() {
        String commit;
        String message;
        URL downloadURL = null;
        try {
            HttpURLConnection commitsConnection = (HttpURLConnection)commits_url.openConnection();
            commitsConnection.setRequestProperty("Accept", "application/vnd.github.v3+json");
            commitsConnection.setRequestProperty("Authorization", "token " + auth_token);
            commitsConnection.setRequestProperty("User-Agent", "Aliucord/" + BuildConfig.GIT_REVISION);
            commitsConnection.connect();
            String res = httpToText(commitsConnection);
            if (res == null) {
                new Handler(Looper.getMainLooper()).post(() -> (new AlertDialog.Builder(main))
                        .setTitle("Update Error")
                        .setMessage("Unauthorized")
                        .show());
                return;
            }
            JSONArray json = new JSONArray(res);
            commit = json.getJSONObject(0).getString("sha").substring(0, 7);
            message = json.getJSONObject(0).getJSONObject("commit").getString("message");

            if (!commit.equals(BuildConfig.GIT_REVISION)) {
                HttpURLConnection contentsConnection = (HttpURLConnection) contents_url.openConnection();
                contentsConnection.setRequestProperty("Accept", "application/vnd.github.v3+json");
                contentsConnection.setRequestProperty("Authorization", "token " + auth_token);
                contentsConnection.setRequestProperty("User-Agent", "Aliucord/" + BuildConfig.GIT_REVISION);
                contentsConnection.connect();
                String rawContents = httpToText(contentsConnection);
                if (rawContents == null) {
                    new Handler(Looper.getMainLooper()).post(() -> (new AlertDialog.Builder(main))
                        .setTitle("Update Error")
                        .setMessage("Unable to fetch builds")
                        .show());
                    return;
                }
                JSONArray contents = new JSONArray(rawContents);
                for (int i = 0; i < contents.length(); ++i) {
                    JSONObject obj = contents.getJSONObject(i);
                    if (obj.has("name") && obj.getString("name").equals("Installer-release.apk")) {
                        downloadURL = new URL(obj.getString("download_url"));
                    }
                }
                if (downloadURL == null) throw new Exception("what");
            }
        } catch (Throwable e) {
            new Handler(Looper.getMainLooper()).post(() -> (new AlertDialog.Builder(main))
                    .setTitle("Update Error")
                    .setMessage("An error occurred checking for updates\n" + e.toString())
                    .show());
            return;
        }

        if (!commit.equals(BuildConfig.GIT_REVISION)) {
            final URL _downloadURL = downloadURL;
            new Handler(Looper.getMainLooper()).post(() -> (new AlertDialog.Builder(main))
                    .setTitle("Update available")
                    .setMessage("A new version is available: " + commit + " - " + message + "\ncurrently running version: " + BuildConfig.GIT_REVISION)
                    .setPositiveButton("Update", (dialog, id) -> {
                        dialog.cancel();
                        try {
                            main.update(_downloadURL);
                        } catch (Throwable e) {
                            (new AlertDialog.Builder(main))
                                    .setTitle("Update Error")
                                    .setMessage("An error occurred downloading updates\n" + e.toString())
                                    .show();
                        }
                    })
                    .setNegativeButton("Cancel", null)
                    .show());
        }
    }

    public void intentCallback(String code) {
        new Thread(() -> {
            String token;
            try {
                HttpURLConnection con = (HttpURLConnection) new URL("https://github.com/login/oauth/access_token").openConnection();
                con.setRequestMethod("POST");
                con.setRequestProperty("Accept", "application/json");
                con.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
                con.setRequestProperty("User-Agent", "Aliucord/" + BuildConfig.GIT_REVISION);
                con.getOutputStream().write(("code=" + code + "&client_id=" + client_id + "&client_secret=" + client_secret).getBytes(StandardCharsets.UTF_8));
                con.connect();
                String res = httpToText(con);
                if (res == null) return;
                JSONObject json;
                json = new JSONObject(res);
                token = json.getString("access_token");
            } catch (IOException|JSONException ignored) {
                return;
            }

            if (token.equals("")) return;

            prefs.edit().putString("github_token", token).apply();
            auth_token = token;
            checkForUpdates();
        }).start();
    }

    public boolean isAuthenticated() {
        return !auth_token.equals("");
    }

    private String httpToText(HttpURLConnection con) {
        String ln;
        StringBuilder res = new StringBuilder();
        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(con.getInputStream(), StandardCharsets.UTF_8));
            while ((ln = reader.readLine()) != null) res.append(ln);
            reader.close();
        } catch (Throwable ignored) { return null; }
        return res.toString();
    }

    // https://stackoverflow.com/a/34331678/13174603
    /**
     * Goes through all apps that handle VIEW intents and have a warmup service. Picks
     * the one chosen by the user if there is one, otherwise makes a best effort to return a
     * valid package name.
     *
     * This is <strong>not</strong> threadsafe.
     *
     * @return The package name recommended to use for connecting to custom tabs related components.
     */
    private String getPackageNameToUse() {
        if (sPackageNameToUse != null) return sPackageNameToUse;
        sPackageNameToUse = CustomTabsClient.getPackageName(main, null);
        if (sPackageNameToUse != null) return sPackageNameToUse;

        PackageManager pm = main.getPackageManager();
        // Get default VIEW intent handler.
        Intent activityIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("http://www.example.com"));
        ResolveInfo defaultViewHandlerInfo = pm.resolveActivity(activityIntent, 0);
        String defaultViewHandlerPackageName = null;
        if (defaultViewHandlerInfo != null) {
            defaultViewHandlerPackageName = defaultViewHandlerInfo.activityInfo.packageName;
        }

        // Get all apps that can handle VIEW intents.
        List<ResolveInfo> resolvedActivityList = pm.queryIntentActivities(activityIntent, 0);
        List<String> packagesSupportingCustomTabs = new ArrayList<>();
        for (ResolveInfo info : resolvedActivityList) {
            Intent serviceIntent = new Intent();
            serviceIntent.setAction(CustomTabsService.ACTION_CUSTOM_TABS_CONNECTION);
            serviceIntent.setPackage(info.activityInfo.packageName);
            if (pm.resolveService(serviceIntent, 0) != null) {
                packagesSupportingCustomTabs.add(info.activityInfo.packageName);
            }
        }

        // Now packagesSupportingCustomTabs contains all apps that can handle both VIEW intents
        // and service calls.
        String STABLE_PACKAGE = "com.android.chrome";
        String BETA_PACKAGE = "com.chrome.beta";
        String DEV_PACKAGE = "com.chrome.dev";
        String LOCAL_PACKAGE = "com.google.android.apps.chrome";
        if (packagesSupportingCustomTabs.isEmpty()) {
            sPackageNameToUse = null;
        } else if (packagesSupportingCustomTabs.size() == 1) {
            sPackageNameToUse = packagesSupportingCustomTabs.get(0);
        } else if (!TextUtils.isEmpty(defaultViewHandlerPackageName)
                && !hasSpecializedHandlerIntents(activityIntent)
                && packagesSupportingCustomTabs.contains(defaultViewHandlerPackageName)) {
            sPackageNameToUse = defaultViewHandlerPackageName;
        } else if (packagesSupportingCustomTabs.contains(STABLE_PACKAGE)) {
            sPackageNameToUse = STABLE_PACKAGE;
        } else if (packagesSupportingCustomTabs.contains(BETA_PACKAGE)) {
            sPackageNameToUse = BETA_PACKAGE;
        } else if (packagesSupportingCustomTabs.contains(DEV_PACKAGE)) {
            sPackageNameToUse = DEV_PACKAGE;
        } else if (packagesSupportingCustomTabs.contains(LOCAL_PACKAGE)) {
            sPackageNameToUse = LOCAL_PACKAGE;
        }
        return sPackageNameToUse;
    }

    /**
     * Used to check whether there is a specialized handler for a given intent.
     * @param intent The intent to check with.
     * @return Whether there is a specialized handler for the given intent.
     */
    private boolean hasSpecializedHandlerIntents(Intent intent) {
        try {
            PackageManager pm = main.getPackageManager();
            List<ResolveInfo> handlers = pm.queryIntentActivities(
                    intent,
                    PackageManager.GET_RESOLVED_FILTER);
            if (handlers.size() == 0) return false;
            for (ResolveInfo resolveInfo : handlers) {
                IntentFilter filter = resolveInfo.filter;
                if (filter == null) continue;
                if (filter.countDataAuthorities() == 0 || filter.countDataPaths() == 0) continue;
                if (resolveInfo.activityInfo == null) continue;
                return true;
            }
        } catch (RuntimeException e) {
            Log.e("Aliucord Installer", "Runtime exception while getting specialized handlers");
        }
        return false;
    }
}
