/*
 * This file is part of Aliucord, an Android Discord client mod.
 * Copyright (c) 2026 Juby210 & Vendicated
 * Licensed under the Open Software License version 3.0
 */

package com.aliucord.coreplugins;

import android.graphics.drawable.ColorDrawable;
import android.text.InputType;
import android.view.View;
import android.widget.EditText;

import androidx.fragment.app.Fragment;

import com.aliucord.Http;
import com.aliucord.Logger;
import com.aliucord.Utils;
import com.aliucord.fragments.InputDialog;
import com.aliucord.utils.IOUtils;
import com.discord.app.AppFragment;
import com.discord.utilities.color.ColorCompat;
import com.discord.widgets.auth.WidgetRemoteAuthViewModel;
import com.google.android.material.button.MaterialButton;
import com.lytefast.flexinput.R;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;

final class QrLogin {
    private static final int TIMEOUT = 15000;
    private static final String LOGIN_BUTTON = "remote_auth_login_button";
    private static final String MFA_TAG = "qr_mfa";
    private static final String EXPIRED_MESSAGE =
        "Try scanning again and tap Log In before leaving the app — Discord expires the QR session otherwise.";

    private static final int FINISH_SUCCESS = 0;
    private static final int FINISH_MFA = 1;
    private static final int FINISH_FAIL = 2;

    private static final Logger logger = new Logger("QrLogin");

    private static volatile String handshakeToken;
    private static volatile String mfaTicket;
    private static volatile String mfaType;
    private static volatile boolean mfaNumeric;
    private static volatile JSONObject pendingMfa;
    private static volatile String pendingError;

    private QrLogin() {}

    static void onRemoteAuthState(AppFragment host, Object viewState) {
        if (viewState instanceof WidgetRemoteAuthViewModel.ViewState.Loaded loaded) {
            if (loaded.getLoginAllowed()) bindLogin(host, loaded.getHandshakeToken());
        } else if (viewState instanceof WidgetRemoteAuthViewModel.ViewState.Failed) {
            logger.errorToast(EXPIRED_MESSAGE);
        }
    }

    private static void bindLogin(AppFragment host, String token) {
        MaterialButton button = findButton(host);
        if (button == null) return;
        button.setEnabled(true);
        button.setOnClickListener(v -> {
            button.setEnabled(false);
            Utils.threadPool.submit(() -> confirmLogin(host, token));
        });
    }

    private static void confirmLogin(AppFragment host, String token) {
        switch (remoteFinish(token, null)) {
            case FINISH_SUCCESS:
                loginDone(host);
                break;
            case FINISH_MFA:
                startMfa(host, token, pendingMfa);
                break;
            default:
                logger.errorToast(pendingError);
                reEnable(host);
                break;
        }
    }

    private static void startMfa(AppFragment host, String token, JSONObject mfa) {
        String ticket = mfa != null ? mfa.optString("ticket") : "";
        if (ticket.isEmpty()) { logger.errorToast("MFA required but no ticket received"); reEnable(host); return; }

        boolean hasTotp = false;
        boolean hasBackup = false;
        StringBuilder available = new StringBuilder();
        JSONArray methods = mfa.optJSONArray("methods");
        if (methods != null) {
            for (int i = 0; i < methods.length(); i++) {
                JSONObject method = methods.optJSONObject(i);
                String type = method != null ? method.optString("type") : "";
                if (type.isEmpty()) continue;
                if (available.length() > 0) available.append(", ");
                available.append(type);
                if (type.equals("totp")) hasTotp = true;
                else if (type.equals("backup")) hasBackup = true;
            }
        }
        if (!hasTotp && !hasBackup) {
            logger.errorToast("Unsupported 2FA method: " + available);
            reEnable(host);
            return;
        }

        handshakeToken = token;
        mfaTicket = ticket;
        mfaType = hasTotp ? "totp" : "backup";
        mfaNumeric = hasTotp;

        Utils.mainThread.post(() -> {
            Utils.openPage(host.requireContext(), TokenLogin.MfaHost.class);
            host.requireActivity().finish();
        });
    }

    static void onMfaHostBound(AppFragment host) {
        if (mfaTicket == null) {
            host.requireActivity().finish();
            return;
        }
        host.requireActivity().getWindow().setBackgroundDrawable(
            new ColorDrawable(ColorCompat.getThemedColor(host.requireContext(), R.b.colorBackgroundPrimary)));
        if (host.getChildFragmentManager().findFragmentByTag(MFA_TAG) == null)
            new TokenLogin.MfaDialog().show(host.getChildFragmentManager(), MFA_TAG);
    }

    static void bindMfaDialog(InputDialog dialog) {
        dialog.setCancelable(false);
        dialog.getHeader().setText("Two-Factor Auth");
        dialog.getBody().setText(mfaNumeric ? "Enter your 6-digit authentication code" : "Enter a backup code");
        dialog.getInputLayout().setHint(mfaNumeric ? "Authentication code" : "Backup code");
        EditText input = dialog.getInputLayout().getEditText();
        if (mfaNumeric && input != null) input.setInputType(InputType.TYPE_CLASS_NUMBER);

        MaterialButton ok = dialog.getOKButton();
        ok.setOnClickListener(v -> {
            String code = dialog.getInput().trim();
            if (code.isEmpty()) return;
            ok.setEnabled(false);
            Utils.threadPool.submit(() -> submitMfa(dialog, code));
        });
        dialog.getCancelButton().setOnClickListener(v -> {
            cancelFlow();
            dialog.requireActivity().finish();
        });
    }

    private static void submitMfa(InputDialog dialog, String code) {
        if (mfaTicket == null) { mfaError(dialog, "Session expired"); return; }
        String mfaToken = mfaVerify(code);
        if (mfaToken == null) { mfaError(dialog, pendingError); return; }
        if (remoteFinish(handshakeToken, mfaToken) == FINISH_SUCCESS) loginDone(dialog);
        else mfaError(dialog, pendingError);
    }

    private static String mfaVerify(String code) {
        try {
            String body = new JSONObject()
                .put("ticket", mfaTicket)
                .put("mfa_type", mfaType)
                .put("data", code)
                .toString();
            String response = post("/mfa/finish", body, null);
            if (response == null) { pendingError = "MFA verification failed"; return null; }
            JSONObject json = new JSONObject(response);
            if (json.optInt("code", -1) == 60008) { pendingError = "Invalid code, try again"; return null; }
            if (json.has("token")) return json.optString("token");
            pendingError = "MFA verification failed";
            return null;
        } catch (Exception e) {
            logger.error("mfaVerify failed", e);
            pendingError = "MFA verification failed";
            return null;
        }
    }

    private static int remoteFinish(String token, String mfaToken) {
        try {
            String response = post("/users/@me/remote-auth/finish", obj(token), mfaToken);
            if (response == null) { pendingError = "Login failed"; return FINISH_FAIL; }
            if (response.trim().isEmpty()) return FINISH_SUCCESS;
            JSONObject json = new JSONObject(response);
            int code = json.optInt("code", -1);
            if (code == 60003) { pendingMfa = json.optJSONObject("mfa"); return FINISH_MFA; }
            if (code == -1) return FINISH_SUCCESS;
            pendingError = "Login failed: " + json.optString("message", "code " + code);
            return FINISH_FAIL;
        } catch (Exception e) {
            logger.error("remoteFinish failed", e);
            pendingError = "Login failed";
            return FINISH_FAIL;
        }
    }

    private static void cancelFlow() {
        String token = handshakeToken;
        clearState();
        if (token != null)
            Utils.threadPool.submit(() -> post("/users/@me/remote-auth/cancel", obj(token), null));
    }

    private static void loginDone(Fragment fragment) {
        logger.infoToast("Logged in!");
        clearState();
        Utils.mainThread.post(() -> fragment.requireActivity().finish());
    }

    private static void mfaError(InputDialog dialog, String message) {
        logger.errorToast(message);
        Utils.mainThread.post(() -> {
            try { dialog.getOKButton().setEnabled(true); } catch (Throwable ignored) {}
        });
    }

    private static void reEnable(AppFragment host) {
        Utils.mainThread.post(() -> {
            MaterialButton button = findButton(host);
            if (button != null) button.setEnabled(true);
        });
    }

    private static MaterialButton findButton(AppFragment host) {
        View root = host.getView();
        return root == null ? null : root.findViewById(Utils.getResId(QrLogin.LOGIN_BUTTON, "id"));
    }

    private static void clearState() {
        handshakeToken = null;
        mfaTicket = null;
        mfaType = null;
        pendingMfa = null;
    }

    private static String obj(String value) {
        try {
            return new JSONObject().put("handshake_token", value).toString();
        } catch (Exception e) {
            return "{}";
        }
    }

    private static String post(String route, String body, String mfaToken) {
        Http.Request req = null;
        try {
            req = Http.Request.newDiscordRequest(route, "POST");
            req.setHeader("Content-Type", "application/json");
            req.setRequestTimeout(TIMEOUT);
            if (mfaToken != null && !mfaToken.isEmpty()) {
                req.setHeader("X-Discord-MFA-Authorization", mfaToken);
                req.setHeader("Cookie", "__Secure-recent_mfa=" + mfaToken);
            }
            req.executeWithBody(body);
            InputStream is;
            try {
                is = req.conn.getInputStream();
            } catch (IOException e) {
                is = req.conn.getErrorStream();
            }
            return is == null ? "" : IOUtils.readAsText(is);
        } catch (Exception e) {
            logger.error("POST " + route + " failed", e);
            return null;
        } finally {
            if (req != null) req.close();
        }
    }
}
