/*
 * This file is part of Aliucord, an Android Discord client mod.
 * Copyright (c) 2021 Juby210 & Vendicated
 * Licensed under the Open Software License version 3.0
 */

package com.aliucord.coreplugins;

import android.content.Context;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import com.aliucord.Http;
import com.aliucord.Utils;
import com.aliucord.entities.CorePlugin;
import com.aliucord.patcher.Patcher;
import com.aliucord.patcher.Hook;
import com.aliucord.fragments.InputDialog;
import com.aliucord.utils.DimenUtils;
import com.aliucord.views.Button;
import com.discord.app.AppActivity;
import com.discord.app.AppFragment;
import com.discord.models.domain.auth.ModelLoginResult;
import com.discord.stores.StoreAuthentication;
import com.discord.stores.StoreStream;
import com.discord.utilities.view.extensions.ViewExtensions;
import com.discord.views.LoadingButton;
import com.discord.widgets.auth.WidgetAuthLanding;
import com.discord.widgets.auth.WidgetRemoteAuth;
import com.discord.widgets.auth.WidgetRemoteAuthViewModel;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputLayout;
import com.lytefast.flexinput.R;

import java.io.IOException;
import java.util.*;

import de.robv.android.xposed.XC_MethodHook.Unhook;
import kotlin.Unit;

public final class TokenLogin extends CorePlugin {
    private Unhook authLanding;
    private Unhook remoteAuth;
    private Unhook appActivity;

    public TokenLogin() {
        super(new Manifest("TokenLogin"));
        getManifest().description = "Provide functionality to log in with a token directly from the login screen";
    }

    // Token login page
    public static class Page extends AppFragment {
        private LoadingButton loginButton;

        public Page() {
            super(Utils.getResId("widget_auth_login", "layout"));
        }

        @Override
        public void onViewBound(View view) {
            super.onViewBound(view);

            LinearLayout v = view.findViewById(Utils.getResId("auth_login_container", "id"));
            if (v == null) return; // prevent crash if layout change

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
                loginButton = new LoadingButton(view.getContext(), null);
                loginButton.setIsLoading(false);
                loginButton.setText(button.getText());
                int i = v.indexOfChild(button);
                v.removeViewAt(i);
                v.addView(loginButton, i, button.getLayoutParams());
                loginButton.setOnClickListener(e -> {
                    if (input != null && input.getEditText() != null) login(input.getEditText().getText());
                });
            }
        }

        public void login(CharSequence token) {
            String trimmedToken = token.toString().trim();
            if (trimmedToken.isEmpty()) {
                Utils.showToast("Token cannot be empty");
                return;
            }
            setLoading(true);
            Utils.threadPool.execute(() -> {
                try (Http.Request req = Http.Request.newDiscordRNRequest("/users/@me")
                        .setHeader("Authorization", trimmedToken)
                        .setRequestTimeout(10000)) {
                    req.execute().assertOk();
                    StoreAuthentication.access$dispatchLogin(
                        StoreStream.getAuthentication(),
                        new ModelLoginResult(trimmedToken.startsWith("mfa."), null, trimmedToken, null, new ArrayList<>())
                    );
                } catch (Http.HttpException e) {
                    Utils.showToast("Invalid token: " + e.statusCode + ": " + e.statusMessage);
                } catch (IOException e) {
                    Utils.showToast("Failed to verify token: " + e.getMessage());
                } finally {
                    setLoading(false);
                }
            });
        }

        private void setLoading(boolean state) {
            if (loginButton == null) return;

            if (state) {
                loginButton.setIsLoading(true);
                loginButton.setEnabled(false);
            } else {
                Utils.mainThread.post(() -> {
                    loginButton.setIsLoading(false);
                    loginButton.setEnabled(true);
                });
            }
        }
    }

    // Backdrop screen that hosts the MFA dialog (so the bg isn't white)
    public static class MfaHost extends AppFragment {
        public MfaHost() {
            super(Utils.getResId("widget_kick_user", "layout"));
        }

        @Override
        public void onViewBound(View view) {
            super.onViewBound(view);
            view.setVisibility(View.INVISIBLE); // just a dim backdrop; the dialog is the UI
            QrLogin.onMfaHostBound(this);
        }
    }

    // MFA code entry for QR login page
    public static class MfaDialog extends InputDialog {
        @Override
        public void onViewBound(View view) {
            super.onViewBound(view);
            QrLogin.bindMfaDialog(this);
        }
    }

    @Override
    public void start(Context appContext) throws Throwable {
        // Add "Login using token" button to the auth landing screen
        authLanding = Patcher.addPatch(WidgetAuthLanding.class.getDeclaredMethod("onViewBound", View.class), new Hook(param -> {
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

        // The native QR scanner already launches WidgetRemoteAuth for ra codes. Route its "Login"
        // button through our finish.
        // Hook the "Scan QR Code" button in user settings to the existing WidgetRemoteAuth
        // Also hook to the login button so we can catch the error 60003, which warn
        // the user when the handshake has expired.
        remoteAuth = Patcher.addPatch(
            WidgetRemoteAuth.class.getDeclaredMethod("configureUI", WidgetRemoteAuthViewModel.ViewState.class),
            new Hook(param -> QrLogin.onRemoteAuthState((AppFragment) param.thisObject, param.args[0]))
        );

        appActivity = Patcher.addPatch(AppActivity.class, "g", new Class<?>[]{ List.class }, new Hook(param -> {
            if (!((boolean) param.getResult()) && ((AppActivity) param.thisObject).d().equals(Page.class)) param.setResult(true);
        }));
    }

    @Override
    public void stop(Context context) {
        if (authLanding != null) authLanding.unhook();
        if (remoteAuth != null) remoteAuth.unhook();
        if (appActivity != null) appActivity.unhook();
    }
}
