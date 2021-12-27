/*
 * This file is part of Aliucord, an Android Discord client mod.
 * Copyright (c) 2021 Juby210 & Vendicated
 * Licensed under the Open Software License version 3.0
 */

package com.aliucord.coreplugins;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import com.aliucord.Utils;
import com.aliucord.entities.Plugin;
import com.aliucord.patcher.Patcher;
import com.aliucord.patcher.Hook;
import com.aliucord.utils.DimenUtils;
import com.aliucord.views.Button;
import com.discord.app.AppActivity;
import com.discord.app.AppFragment;
import com.discord.models.domain.auth.ModelLoginResult;
import com.discord.stores.StoreAuthentication;
import com.discord.stores.StoreStream;
import com.discord.utilities.view.extensions.ViewExtensions;
import com.discord.widgets.auth.WidgetAuthLanding;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputLayout;
import com.lytefast.flexinput.R;

import java.util.List;

import kotlin.Unit;

final class TokenLogin extends Plugin {
    TokenLogin() {
        super(new Manifest("TokenLogin"));
    }

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
            StoreAuthentication.access$dispatchLogin(StoreStream.getAuthentication(), new ModelLoginResult(token.toString().startsWith("mfa."), null, token.toString(), null));
        }
    }

    @Override
    @SuppressLint("SetTextI18n")
    public void start(Context appContext) throws Throwable {
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

        Patcher.addPatch(AppActivity.class, "g", new Class<?>[]{ List.class }, new Hook(param -> {
            if (!((boolean) param.getResult()) && ((AppActivity) param.thisObject).d().equals(Page.class)) param.setResult(true);
        }));
    }

    @Override
    public void stop(Context context) {}
}
