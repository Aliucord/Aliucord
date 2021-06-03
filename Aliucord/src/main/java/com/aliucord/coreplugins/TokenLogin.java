/*
 * Copyright (c) 2021 Juby210
 * Licensed under the Open Software License version 3.0
 */

package com.aliucord.coreplugins;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import androidx.annotation.NonNull;

import com.aliucord.Utils;
import com.aliucord.entities.Plugin;
import com.aliucord.patcher.Patcher;
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
import com.lytefast.flexinput.R$c;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import kotlin.Unit;

public class TokenLogin extends Plugin {
    @SuppressWarnings("ResultOfMethodCallIgnored")
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
                    CharSequence token = input.getEditText().getText();
                    if (!token.equals("")) login(token);
                });
            }
        }

        public void login(CharSequence token) {
            StoreAuthentication.access$dispatchLogin(StoreStream.getAuthentication(), new ModelLoginResult(false, null, token.toString(), null));
        }
    }

    @NonNull
    @Override
    public Manifest getManifest() { return new Manifest(); }
    public static Map<String, List<String>> getClassesToPatch() {
        Map<String, List<String>> map = new HashMap<>();
        map.put("com.discord.widgets.auth.WidgetAuthLanding", Collections.singletonList("onViewBound"));
        return map;
    }

    @Override
    @SuppressLint("SetTextI18n")
    public void start(Context appContext) {
        Patcher.addPatch("com.discord.widgets.auth.WidgetAuthLanding", "onViewBound", (_this, args, ret) -> {
            Context context = ((WidgetAuthLanding) _this).requireContext();
            RelativeLayout view = (RelativeLayout) args.get(0);
            LinearLayout v = (LinearLayout) view.getChildAt(1);

            int padding = Utils.dpToPx(18);
            Button btn = new Button(context, false);
            btn.setPadding(0, padding, 0, padding);
            btn.setText("Login using token");
            btn.setTextSize(16.0f);
            if (StoreStream.getUserSettingsSystem().getTheme().equals("light"))
                btn.setBackgroundColor(context.getResources().getColor(R$c.uikit_btn_bg_color_selector_secondary_light));
            else btn.setBackgroundColor(context.getResources().getColor(R$c.uikit_btn_bg_color_selector_secondary_dark));
            btn.setOnClickListener(e -> Utils.openPage(e.getContext(), Page.class));
            v.addView(btn);

            return ret;
        });

        Patcher.addPatch("com.discord.app.AppActivity", "h", (_this, args, ret) -> {
            if (!((boolean) ret) && ((AppActivity) _this).e().equals(Page.class)) return true;
            return ret;
        });
    }

    @Override
    public void stop(Context context) {}
}
