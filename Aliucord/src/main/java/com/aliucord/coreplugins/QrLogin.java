/*
 * This file is part of Aliucord, an Android Discord client mod.
 * Copyright (c) 2026 Juby210 & Vendicated
 * Licensed under the Open Software License version 3.0
 */

package com.aliucord.coreplugins;

import android.text.InputType;

import androidx.fragment.app.FragmentManager;

import com.aliucord.Http;
import com.aliucord.Logger;
import com.aliucord.Utils;
import com.aliucord.fragments.ConfirmDialog;
import com.aliucord.fragments.InputDialog;
import com.aliucord.utils.IOUtils;
import com.discord.app.AppActivity;
import com.discord.app.AppFragment;
import com.discord.views.LoadingButton;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.atomic.AtomicBoolean;

final class QrLogin {
    private static final String QR_PREFIX = "https://discord.com/ra/";
    private static final int TIMEOUT = 15000;
    private static final Logger logger = new Logger("QrLogin");
    private static final AtomicBoolean busy = new AtomicBoolean(false);

    private QrLogin() {}

    static boolean tryHandleScan(AppFragment host, Object zxingResult) {
        if (zxingResult == null) return false;
        String url = zxingResult.toString();
        if (!url.startsWith(QR_PREFIX)) return false;
        if (!busy.compareAndSet(false, true)) return true;

        String fingerprint = url.substring(QR_PREFIX.length());
        AppActivity activity = host.requireAppActivity();
        FragmentManager fm = activity.getSupportFragmentManager();
        Utils.mainThread.post(() -> {
            try {
                activity.onBackPressed();
            } catch (Throwable ignored) {}
            confirm(fm, fingerprint);
        });
        return true;
    }

    private static void confirm(FragmentManager fm, String fingerprint) {
        ConfirmDialog dialog = new ConfirmDialog()
            .setTitle("QR Login")
            .setDescription("Log in using the scanned QR code?");
        dialog.setOnOkListener(v -> {
            setLoading(dialog);
            Utils.threadPool.submit(() -> authorize(fm, dialog, fingerprint));
        });
        dialog.setOnCancelListener(v -> {
            dialog.dismiss();
            reset();
        });
        dialog.show(fm, "qr_confirm");
    }

    private static void authorize(FragmentManager fm, ConfirmDialog dialog, String fingerprint) {
        try {
            String body = new JSONObject().put("fingerprint", fingerprint).toString();
            String response = post("/users/@me/remote-auth", body, null);
            JSONObject json = parse(response);
            if (json == null || !json.has("handshake_token")) {
                String message = json == null
                    ? "Remote auth request failed"
                    : "Remote auth failed: " + json.optString("message", "request failed");
                dismissThen(dialog, () -> { logger.errorToast(message); reset(); });
                return;
            }
            finishRemoteAuth(fm, dialog, json.getString("handshake_token"), null);
        } catch (Exception e) {
            dismissThen(dialog, () -> { logger.errorToast("Remote auth error", e); reset(); });
        }
    }

    private static void finishRemoteAuth(FragmentManager fm, ConfirmDialog dialog, String handshakeToken, String mfaToken) {
        String response;
        try {
            String body = new JSONObject().put("handshake_token", handshakeToken).toString();
            response = post("/users/@me/remote-auth/finish", body, mfaToken);
        } catch (Exception e) {
            dismissThen(dialog, () -> { logger.errorToast("Login failed", e); reset(); });
            return;
        }
        String finalResponse = response;
        Utils.mainThread.post(() -> {
            dialog.dismiss();
            try {
                if (finalResponse == null) { logger.errorToast("Login failed"); reset(); return; }
                if (finalResponse.trim().isEmpty()) { logger.infoToast("Logged in!"); reset(); return; }
                JSONObject json = new JSONObject(finalResponse);
                int code = json.optInt("code", -1);
                if (code == 60003 && mfaToken == null) {
                    handleMfaRequired(fm, handshakeToken, json);
                } else if (code != -1) {
                    logger.errorToast("Login failed: " + json.optString("message", "code " + code));
                    reset();
                } else {
                    logger.infoToast("Logged in!");
                    reset();
                }
            } catch (Exception e) {
                logger.errorToast("Login failed", e);
                reset();
            }
        });
    }

    private static void handleMfaRequired(FragmentManager fm, String handshakeToken, JSONObject json) {
        JSONObject mfa = json.optJSONObject("mfa");
        String ticket = mfa != null ? mfa.optString("ticket") : null;
        if (ticket == null) {
            logger.errorToast("MFA required but no ticket received");
            reset();
            return;
        }
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
        if (hasTotp) promptMfa(fm, handshakeToken, ticket, "totp", true);
        else if (hasBackup) promptMfa(fm, handshakeToken, ticket, "backup", false);
        else {
            logger.errorToast("Unsupported 2FA method: " + available);
            reset();
        }
    }

    private static void promptMfa(FragmentManager fm, String handshakeToken, String ticket, String type, boolean numeric) {
        InputDialog dialog = new InputDialog()
            .setTitle("Two-Factor Auth")
            .setDescription(numeric ? "Enter your 6-digit authentication code" : "Enter a backup code")
            .setPlaceholderText(numeric ? "Authentication code" : "Backup code");
        if (numeric) dialog.setInputType(InputType.TYPE_CLASS_NUMBER);
        dialog.setOnOkListener(v -> {
            String code = dialog.getInput().trim();
            if (code.isEmpty()) return;
            dialog.dismiss();
            ConfirmDialog loading = loadingDialog(fm);
            Utils.threadPool.submit(() -> submitMfa(fm, loading, handshakeToken, ticket, type, numeric, code));
        });
        dialog.setOnCancelListener(v -> {
            dialog.dismiss();
            Utils.threadPool.submit(() -> cancel(handshakeToken));
            reset();
        });
        dialog.show(fm, "mfa_input");
    }

    private static void submitMfa(FragmentManager fm, ConfirmDialog loading, String handshakeToken, String ticket, String type, boolean numeric, String code) {
        JSONObject json;
        try {
            String body = new JSONObject()
                .put("ticket", ticket)
                .put("mfa_type", type)
                .put("data", code)
                .toString();
            json = parse(post("/mfa/finish", body, null));
        } catch (Exception e) {
            dismissThen(loading, () -> { logger.errorToast("MFA verification failed", e); reset(); });
            return;
        }
        if (json != null && json.optInt("code", -1) == -1 && json.has("token")) {
            finishRemoteAuth(fm, loading, handshakeToken, json.optString("token"));
            return;
        }
        boolean invalid = json != null && json.optInt("code", -1) == 60008;
        dismissThen(loading, () -> {
            if (invalid) {
                logger.errorToast("Invalid code, try again");
                promptMfa(fm, handshakeToken, ticket, type, numeric);
            } else {
                logger.errorToast("MFA verification failed");
                reset();
            }
        });
    }

    private static void cancel(String handshakeToken) {
        try {
            String body = new JSONObject().put("handshake_token", handshakeToken).toString();
            post("/users/@me/remote-auth/cancel", body, null);
        } catch (Exception e) {
            logger.error("Remote auth cancel failed", e);
        }
    }

    private static ConfirmDialog loadingDialog(FragmentManager fm) {
        ConfirmDialog dialog = new ConfirmDialog()
            .setTitle("Verifying")
            .setDescription("Please wait…");
        dialog.setOnOkListener(v -> {});
        dialog.setOnCancelListener(v -> {});
        dialog.show(fm, "qr_loading");
        Utils.mainThread.post(() -> setLoading(dialog));
        return dialog;
    }

    private static void setLoading(ConfirmDialog dialog) {
        try {
            LoadingButton ok = dialog.getOKButton();
            ok.setText("");
            ok.setIsLoading(true);
            ok.setEnabled(false);
            dialog.getCancelButton().setEnabled(false);
        } catch (Throwable ignored) {}
    }

    private static void dismissThen(ConfirmDialog dialog, Runnable action) {
        Utils.mainThread.post(() -> {
            dialog.dismiss();
            action.run();
        });
    }

    private static void reset() { busy.set(false); }

    private static String post(String route, String body, String mfaToken) {
        try (Http.Request req = Http.Request.newDiscordRequest(route, "POST")) {
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
        }
    }

    private static JSONObject parse(String response) {
        if (response == null || response.trim().isEmpty()) return null;
        try {
            return new JSONObject(response);
        } catch (Exception e) {
            return null;
        }
    }
}
