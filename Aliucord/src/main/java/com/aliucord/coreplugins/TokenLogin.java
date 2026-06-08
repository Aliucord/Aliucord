/*
 * This file is part of Aliucord, an Android Discord client mod.
 * Copyright (c) 2021 Juby210 & Vendicated
 * Licensed under the Open Software License version 3.0
 */

package com.aliucord.coreplugins;

import android.content.Context;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import com.aliucord.Http;
import com.aliucord.Utils;
import com.aliucord.entities.CorePlugin;
import com.aliucord.fragments.ConfirmDialog;
import com.aliucord.fragments.InputDialog;
import com.aliucord.patcher.Hook;
import com.aliucord.patcher.Patcher;
import com.aliucord.patcher.PreHook;
import com.aliucord.utils.DimenUtils;
import com.aliucord.utils.IOUtils;
import com.aliucord.views.Button;
import com.discord.app.AppActivity;
import com.discord.app.AppFragment;
import com.discord.models.domain.auth.ModelLoginResult;
import com.discord.stores.StoreAuthentication;
import com.discord.stores.StoreStream;
import com.discord.utilities.view.extensions.ViewExtensions;
import com.discord.widgets.auth.WidgetAuthLanding;
import com.discord.widgets.media.WidgetQRScanner;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputLayout;
import com.lytefast.flexinput.R;

import org.json.JSONObject;

import java.io.InputStream;
import java.io.IOException;
import java.util.*;

import kotlin.Unit;

public final class TokenLogin extends CorePlugin {
    private static final String TAG = "TokenLogin";
    private static final String QR_PREFIX = "https://discord.com/ra/";

    public TokenLogin() {
        super(new Manifest(TAG));
        getManifest().description = "Provide functionality to log in with a token directly from the login screen";
    }

    static void doStartRemoteAuth(AppFragment host, String fingerprint) {
        Utils.threadPool.submit(() -> {
            try {
                String body = new JSONObject().put("fingerprint", fingerprint).toString();
                String response = discordPost("/users/@me/remote-auth", body, null);

                if (response == null) {
                    Log.e(TAG, "RA start fail: null response");
                    Utils.showToast("Remote auth request failed", true);
                    return;
                }

                JSONObject json = new JSONObject(response);

                if (!json.has("handshake_token")) {
                    Log.e(TAG, "RA start fail: no handshake_token value");
                    Utils.showToast("Remote auth failed: " + json.optString("message", response), true);
                    return;
                }

                String handshakeToken = json.getString("handshake_token");
                Log.i(TAG, "RA handshake token value: " + handshakeToken);
                Utils.mainThread.post(() -> showConfirmDialog(host, handshakeToken));
            } catch (Exception e) {
                Log.e(TAG, "RA start error:", e);
                Utils.showToast("Error: " + e.getMessage(), true);
            }
        });
    }

    private static void showConfirmDialog(AppFragment host, String handshakeToken) {
        ConfirmDialog dialog = new ConfirmDialog()
            .setTitle("QR Login")
            .setDescription("Log in using the scanned QR code?");
        dialog.setOnOkListener(v -> {
            dialog.dismiss();
            Utils.threadPool.submit(() -> doFinishRemoteAuth(host, handshakeToken));
        });
        dialog.setOnCancelListener(v -> {
            dialog.dismiss();
            Utils.threadPool.submit(() -> {
                try {
                    String body = new JSONObject().put("handshake_token", handshakeToken).toString();
                    String response = discordPost("/users/@me/remote-auth/cancel", body, null);
                    Log.i(TAG, "RA cancel response: " + response);
                } catch (Exception e) {
                    Log.e(TAG, "RA cancel error:", e);
                }
            });
        });
        dialog.show(host.getChildFragmentManager(), "qr_confirm");
    }

    private static void doFinishRemoteAuth(AppFragment host, String handshakeToken) {
        doFinishRemoteAuth(host, handshakeToken, null);
    }

    private static void doFinishRemoteAuth(AppFragment host, String handshakeToken, String mfaJwt) {
        try {
            Log.i(TAG, "Finish RA start. Handshake Token: " + handshakeToken);
            String body = new JSONObject()
                .put("handshake_token", handshakeToken)
                .toString();

            String response = discordPost("/users/@me/remote-auth/finish", body, mfaJwt);

            if (response == null) {
                Log.e(TAG, "RA finish fail: null response");
                Utils.showToast("Login failed", true);
                return;
            }

            if (response.trim().isEmpty()) {
                Log.i(TAG, "QR login success: 204 empty");
                Utils.showToast("Logged in!", false);
                return;
            }

            JSONObject json = new JSONObject(response);
            int code = json.optInt("code", -1);
            if (code == 60003) {
                String ticket = json.getJSONObject("mfa").getString("ticket");
                Utils.mainThread.post(() -> showMfaDialog(host, handshakeToken, ticket));
            } else if (code != -1) {
                Log.e(TAG, "RA finish error code: " + code);
                Utils.showToast("Error: " + json.optString("message", "Unknown error"), true);
            } else {
                Log.i(TAG, "QR login success: " + response);
                Utils.showToast("Logged in!", false);
            }
        } catch (Exception e) {
            Log.e(TAG, "RA finish error:", e);
            Utils.showToast("Error: " + e.getMessage(), true);
        }
    }

    private static void showMfaDialog(AppFragment host, String handshakeToken, String ticket) {
        InputDialog dialog = new InputDialog()
            .setTitle("Two-Factor Auth")
            .setDescription("Enter your 6-digit authentication code")
            .setPlaceholderText("Authentication code");
        dialog.setOnOkListener(v -> {
            String code = dialog.getInput();
            dialog.dismiss();
            Utils.threadPool.submit(() -> doFinishMfa(host, handshakeToken, ticket, code));
        });
        dialog.show(host.getChildFragmentManager(), "mfa_input");
    }

    private static void doFinishMfa(AppFragment host, String handshakeToken, String ticket, String code) {
        try {
            String body = new JSONObject()
                .put("ticket", ticket)
                .put("mfa_type", "totp")
                .put("data", code)
                .toString();
            String response = discordPost("/mfa/finish", body, null);

            if (response == null) {
                Log.e(TAG, "MFA finish fail: null response");
                Utils.showToast("MFA verification failed", true);
                return;
            }

            JSONObject json = new JSONObject(response);
            if (json.optInt("code", -1) == 60008) {
                Log.e(TAG, "MFA invalid code");
                Utils.showToast("Invalid two-factor code", true);
            } else if (json.has("token")) {
                String token = json.getString("token");
                doFinishRemoteAuth(host, handshakeToken, token);
            } else {
                Log.e(TAG, "MFA success but no token in response");
                Utils.showToast("MFA error", true);
            }
        } catch (Exception e) {
            Log.e(TAG, "MFA finish error:", e);
            Utils.showToast("Error: " + e.getMessage(), true);
        }
    }

    static String discordPost(String route, String jsonBody, String mfaToken) {
        Log.i(TAG, "discordPost req => Route: " + route + " | Body: " + jsonBody);
        Http.Request req = null;
        try {
            req = Http.Request.newDiscordRequest(route, "POST");
            req.setHeader("Content-Type", "application/json");

            if (mfaToken != null && !mfaToken.isEmpty()) {
                req.setHeader("X-Discord-MFA-Authorization", mfaToken);
                req.setHeader("Cookie", "__Secure-recent_mfa=" + mfaToken);
            }

            req.executeWithBody(jsonBody);

            InputStream is;
            try {
                is = req.conn.getInputStream();
            } catch (IOException e) {
                Log.w(TAG, "discordPost status code isnt OK, reading error stream");
                is = req.conn.getErrorStream();
            }
            String respText = is == null ? "" : IOUtils.readAsText(is);
            Log.i(TAG, "discordPost response <= Route: " + route + " // Response: " + respText);
            return respText;
        } catch (Exception e) {
            Log.e(TAG, "Request to " + route + " failed", e);
            return null;
        } finally {
            if (req != null) req.close();
        }
    }

    // Token login page
    public static class Page extends AppFragment {
        public Page() {
            super(Utils.getResId("widget_auth_login", "layout"));
        }

        @Override
        public void onViewBound(View view) {
            super.onViewBound(view);

            LinearLayout v = view.findViewById(Utils.getResId("auth_login_container", "id"));
            v.removeViewAt(1); // remove email input
            v.removeViewAt(2); // remove forgot password
            v.removeViewAt(2); // remove use a password manager

            TextInputLayout input = (TextInputLayout) v.getChildAt(1);
            if (input != null) {
                input.setHint("Token");
                ViewExtensions.setOnImeActionDone(input, false, e -> {
                    if (!e.getText().equals("")) login(e.getText());
                    return Unit.a;
                });
            }

            MaterialButton button = (MaterialButton) v.getChildAt(2);
            if (button != null) {
                button.setOnClickListener(e -> {
                    if (input == null || input.getEditText() == null) return;
                    CharSequence token = input.getEditText().getText();
                    if (!token.equals("")) login(token);
                });
            }
        }

        public void login(CharSequence token) {
            StoreAuthentication.access$dispatchLogin(StoreStream.getAuthentication(), new ModelLoginResult(token.toString().startsWith("mfa."), null, token.toString().trim(), null, new ArrayList<>()));
        }
    }

    @Override
    public void start(Context appContext) throws Throwable {
        // Add "Login using token" button to the auth landing screen
        Patcher.addPatch(WidgetAuthLanding.class.getDeclaredMethod("onViewBound", View.class), new Hook(param -> {
            Context context = ((WidgetAuthLanding) param.thisObject).requireContext();
            RelativeLayout view = (RelativeLayout) param.args[0];
            LinearLayout v = (LinearLayout) view.getChildAt(1);

            int padding = DimenUtils.dpToPx(18);
            Button btn = new Button(context);
            btn.setPadding(0, padding, 0, padding);
            btn.setText("Login using token");
            btn.setTextSize(16.0f);
            if (StoreStream.getUserSettingsSystem().getTheme().equals("light"))
                btn.setBackgroundColor(context.getResources().getColor(R.c.uikit_btn_bg_color_selector_secondary_light, null));
            else btn.setBackgroundColor(context.getResources().getColor(R.c.uikit_btn_bg_color_selector_secondary_dark, null));
            btn.setOnClickListener(e -> Utils.openPage(e.getContext(), Page.class));
            v.addView(btn);
        }));

        // Hook the "Scan QR Code" button in user settings
        Class<?> resultClass = Class.forName("com.google.zxing.Result");
        Patcher.addPatch(
            WidgetQRScanner.class.getDeclaredMethod("handleResult", resultClass),
            new PreHook(param -> {
                if (param.args[0] == null) return;
                String url = param.args[0].toString();
                Log.i(TAG, "QR Url: " + url);
                if (!url.startsWith(QR_PREFIX)) return;
                param.setResult(null); // make original code do nothing
                AppFragment host = (AppFragment) param.thisObject;
                String fingerprint = url.substring(QR_PREFIX.length());
                Log.i(TAG, "Intercepted RA value: " + fingerprint);
                doStartRemoteAuth(host, fingerprint);
            })
        );

        Patcher.addPatch(AppActivity.class, "g", new Class<?>[]{ List.class }, new Hook(param -> {
            if (!((boolean) param.getResult()) && ((AppActivity) param.thisObject).d().equals(Page.class)) param.setResult(true);
        }));
    }

    @Override
    public void stop(Context context) {}
}
