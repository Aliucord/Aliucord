package com.aliucord.installer;

import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.ComponentName;
import android.content.DialogInterface.OnClickListener;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.*;

import androidx.browser.customtabs.CustomTabsClient;
import androidx.browser.customtabs.CustomTabsIntent;
import androidx.browser.customtabs.CustomTabsServiceConnection;

import java.net.HttpURLConnection;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.Thread;
import java.nio.charset.StandardCharsets;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.stream.Collectors;

import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONException;

public class GitHubAPI {
    final String client_id = "9260a23baccb4ebf2d94";
    final String client_secret = "0ab74f8ed58600ad48acbb9843ab05615ec4940d";
    final Uri uri = Uri.parse("https://github.com/login/oauth/authorize?client_id=" + client_id + "&redirect_uri=aliucord-installer%3A%2F%2Fauth&scope=repo");
    URL commits_url;
    URL contents_url;
    SharedPreferences prefs;
    String auth_token;
    MainActivity main;

    public GitHubAPI (SharedPreferences _prefs, MainActivity _main) {
        try {
            commits_url = new URL("https://api.github.com/repos/aliucord/Aliucord/commits");
            contents_url = new URL("https://api.github.com/repos/Aliucord/Aliucord/contents?ref=builds");
        } catch (MalformedURLException ignored) {}
        prefs = _prefs;
        main = _main;
        auth_token = prefs.getString("github_token", "");
        if (isAuthenticated()) {
            new Thread(() -> checkForUpdates()).start();
        }
    }

    public void startAuthFlow(Context context) {
        if (isAuthenticated()) {
            prefs.edit().remove("github_token").apply();
            auth_token = "";
            return;
        }
        CustomTabsServiceConnection tabConnection = new CustomTabsServiceConnection() {
            @Override
            public void onCustomTabsServiceConnected(ComponentName componentName, CustomTabsClient tabClient) {
                CustomTabsIntent.Builder tabBuilder = new CustomTabsIntent.Builder();
                CustomTabsIntent intent = tabBuilder.build();
                tabClient.warmup(0L);
                intent.launchUrl(context, uri);
            }
        
            @Override
            public void onServiceDisconnected(ComponentName name) {
                
            }
        };
        CustomTabsClient.bindCustomTabsService(context, "com.android.chrome", tabConnection);
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
                new Handler(Looper.getMainLooper()).post(() -> {
                    (new AlertDialog.Builder(main))
                        .setTitle("Update Error")
                        .setMessage("Unauthorized")
                        .show();
                });
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
                    new Handler(Looper.getMainLooper()).post(() -> {
                        (new AlertDialog.Builder(main))
                            .setTitle("Update Error")
                            .setMessage("Unable to fetch builds")
                            .show();
                    });
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
        } catch (Exception e) {
            new Handler(Looper.getMainLooper()).post(() -> {
                (new AlertDialog.Builder(main))
                .setTitle("Update Error")
                .setMessage("An error occurred checking for updates\n" + e.toString())
                .show();
            });
            return;
        }

        if (!commit.equals(BuildConfig.GIT_REVISION)) {
            final URL _downloadURL = downloadURL;
            new Handler(Looper.getMainLooper()).post(() -> {
                (new AlertDialog.Builder(main))
                .setTitle("Update available")
                .setMessage("A new version is available: " + commit + " - " + message + "\ncurrently running version: " + BuildConfig.GIT_REVISION)
                .setPositiveButton("Update", new DialogInterface.OnClickListener()     {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                        try {
                            main.update(_downloadURL);
                        } catch (Exception e) {
                            (new AlertDialog.Builder(main))
                            .setTitle("Update Error")
                            .setMessage("An error occurred downloading updates\n" + e.toString())
                            .show();
                            return;
                        }
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
            });
        }
    }

    public void intentCallback(String code) {
        new Thread(() -> {
            String token;
            try {
                HttpURLConnection con = (HttpURLConnection)new URL("https://github.com/login/oauth/access_token").openConnection();
                con.setRequestMethod("POST");
                con.setRequestProperty("Accept", "application/json");
                con.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
                con.setRequestProperty("User-Agent", "Aliucord/" + BuildConfig.GIT_REVISION);
                con.getOutputStream().write(("code=" + code + "&client_id=" + client_id + "&client_secret=" + client_secret).getBytes("UTF8"));
                con.connect();
                String res = httpToText(con);
                JSONObject json;
                json = new JSONObject(res);
                token = json.getString("access_token");
            } catch (IOException|JSONException ignored) {
                return;
            }

            if (token == null || token == "") return;

            prefs.edit().putString("github_token", token).apply();

            auth_token = token;

            checkForUpdates();
        }).start();
    }

    public boolean isAuthenticated() {
        return !auth_token.equals("");
    }

    private String httpToText(HttpURLConnection con) {
        String responseText;
        try{
            InputStream input = con.getInputStream();
            responseText = new BufferedReader(new InputStreamReader(input, StandardCharsets.UTF_8))
                .lines()
                .collect(Collectors.joining("\n"));
        } catch (IOException ignored) {
            return null;
        }
        return responseText;
    }
}