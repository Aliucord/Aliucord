package com.aliucord.installer;

import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.ComponentName;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.net.Uri;

import androidx.appcompat.app.AppCompatActivity;
import androidx.browser.customtabs.CustomTabsClient;
import androidx.browser.customtabs.CustomTabsIntent;
import androidx.browser.customtabs.CustomTabsServiceConnection;

import java.net.HttpURLConnection;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.Runnable;
import java.lang.Thread;
import java.nio.charset.StandardCharsets;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.stream.Collectors;

import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONException;

import android.util.Log;

public class GithubAuth {
    String client_id = "9260a23baccb4ebf2d94";
    String client_secret = "0ab74f8ed58600ad48acbb9843ab05615ec4940d";
    Uri uri;
    SharedPreferences prefs;
    String auth_token;
    MainActivity main;

    public GithubAuth (SharedPreferences _prefs, MainActivity _main) {
        prefs = _prefs;
        main = _main;
        auth_token = prefs.getString("github_token", "");
        if (auth_token != "") {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    checkForUpdates();
                }
            }).start();
        }
        uri = Uri.parse("https://github.com/login/oauth/authorize?client_id=" + client_id + "&redirect_uri=aliucord-installer%3A%2F%2Fauth&scope=repo");
    }

    public void startAuthFlow(Context context) {
        if (auth_token != "") {
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
        try {
            HttpURLConnection con = (HttpURLConnection)new URL("https://api.github.com/repos/aliucord/Aliucord/commits").openConnection();
            con.setRequestProperty("Accept", "application/vnd.github.v3+json");
            con.setRequestProperty("Authorization", "token " + auth_token);
            con.setRequestProperty("User-Agent", "Aliucord/" + BuildConfig.GIT_REVISION);
            con.connect();
            String res = httpToText(con);
            if (res == null) {
                main.runOnUiThread(new Runnable() {
                    public void run() {
                        AlertDialog.Builder builder = new AlertDialog.Builder(main);
        
                        builder.setMessage("Unauthorized")
                            .setTitle("Update Error");
                
                        AlertDialog dialog = builder.create();
                
                        dialog.show();
                    }
                });
                return;
            }
            JSONArray json = new JSONArray(res);
            commit = json.getJSONObject(0).getString("sha").substring(0, 7);
            message = json.getJSONObject(0).getJSONObject("commit").getString("message");
        } catch (IOException|JSONException ignored) {
            main.runOnUiThread(new Runnable() {
                public void run() {
                    AlertDialog.Builder builder = new AlertDialog.Builder(main);
    
                    builder.setMessage("An error occured checking for updates.")
                        .setTitle("Update Error");
            
                    AlertDialog dialog = builder.create();
            
                    dialog.show();
                }
            });
            return;
        }
        main.runOnUiThread(new Runnable() {
            public void run() {
                AlertDialog.Builder builder = new AlertDialog.Builder(main);

                builder.setMessage("latest commit is \n" + commit + "\n with message \n" + message + "\n currently running commit " + BuildConfig.GIT_REVISION)
                    .setTitle("git info");
        
                AlertDialog dialog = builder.create();
        
                dialog.show();
            }
        });
    }

    public void intentCallback(String code) {
        new Thread(new Runnable() {
            @Override
            public void run() {
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
            }
        }).start();
    }

    public boolean isAuthed() {
        return auth_token != "";
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