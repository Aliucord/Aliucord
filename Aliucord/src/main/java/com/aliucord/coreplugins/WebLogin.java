package com.aliucord.coreplugins;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import android.view.View;
import android.webkit.JavascriptInterface;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.webkit.WebSettings;
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
import com.discord.widgets.auth.WidgetAuthLanding;
import com.lytefast.flexinput.R$c;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class WebLogin extends Plugin {
    @SuppressWarnings("ResultOfMethodCallIgnored")
    public static class Page extends AppFragment {
        public Page() {
            super(Utils.getResId("widget_auth_login", "layout"));
        }

        public class LoginInterface {
            // Allows the injected JS to send the token back to the app
            @JavascriptInterface
            public void getLogin(String token) {
                login(token);
            }
        }

        @Override
        public void onViewBound(View view) {
            super.onViewBound(view);

            LinearLayout v = view.findViewById(Utils.getResId("auth_login_container", "id"));
            v.removeViewAt(1); // remove email input
            v.removeViewAt(1); // remove password input
            v.removeViewAt(1); // remove forgot password
            v.removeViewAt(1); // remove use a password manager
            v.removeViewAt(1); // remove login button

            WebView loginView = new WebView(getActivity());
            loginView.setVisibility(View.GONE);
            v.addView(loginView);
            loginView.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT));
            loginView.setBackgroundColor(Color.TRANSPARENT);
            loginView.setScrollbarFadingEnabled(true);
            WebSettings settings = loginView.getSettings();
            settings.setJavaScriptEnabled(true);
            settings.setLoadWithOverviewMode(true);
            settings.setUseWideViewPort(false);
            loginView.addJavascriptInterface(new LoginInterface(), "Aliucord");
            loginView.setWebViewClient(new WebViewClient() {
                @Override  
                public void onPageFinished(WebView view, String url)  
                {  
                    view.evaluateJavascript(
                        "const loginStyles = `\n"+
                        ".splashBackground-1FRCko, body, :root {\n"+
                        "  background: transparent;\n"+
                        "}\n"+
                        ".logo-2iEHEq, .rightSplit-2US0xy, .canvas-3XuBXe, .mainLoginContainer-1ddwnR > .colorHeaderPrimary-26Jzh-, .mainLoginContainer-1ddwnR > .colorHeaderSecondary-3Sp3Ft, .authBox-hW6HRx::before {\n"+
                        "  display: none;\n"+
                        "}\n"+
                        ".wrapper-6URcxg {\n"+
                        "  align-items: unset;\n"+
                        "  min-height: unset;\n"+
                        "  height: unset;\n"+
                        "}\n"+
                        ".wrapper-6URcxg > div {\n"+
                        "  position: absolute;\n"+
                        "  left: 0;\n"+
                        "  top: 0;\n"+
                        "  height: 100%;\n"+
                        "  width: 100%;\n"+
                        "  max-width: unset;\n"+
                        "  border-radius: 0;\n"+
                        "}\n"+
                        ":root .authBox-hW6HRx {\n"+
                        "  width: 100%;\n"+
                        "  max-width: unset;\n"+
                        "  border-radius: 0;\n"+
                        "  box-shadow: none;\n"+
                        "  padding: 0;\n"+
                        "}\n"+
                        ":root .authBox-hW6HRx > .centeringWrapper-2Rs1dR {\n"+
                        "  height: 100%;\n"+
                        "}\n"+
                        ".qrLogin-1AOZMt {\n"+
                        "  display: none !important;\n"+
                        "}\n"+
                        "#app-mount, body {\n"+
                        "  overflow: visible;\n"+
                        "}\n"+
                        ":root, .authBox-hW6HRx {\n"+
                        "  --background-tertiary: transparent;\n"+
                        "  --background-mobile-primary: transparent;\n"+
                        "}`\n"+
                        "const elem = window.document.createElement('style');\n"+
                        "elem.innerHTML = loginStyles;\n"+
                        "window.document.head.appendChild(elem);\n"+
                        "Object.values(webpackJsonp.push([[],{['']:(_,e,r)=>{e.cache=r.c}},[['']]]).cache).find(m=>m.exports&&m.exports.default&&m.exports.default.dispatch!==void 0).exports.default.subscribe('LOGIN_SUCCESS', (e) => window.Aliucord.getLogin(e.token));\n"+
                        "Object.values(webpackJsonp.push([[],{['']:(_,e,r)=>{e.cache=r.c}},[['']]]).cache).find(m=>m.exports&&m.exports.default&&m.exports.default.displayName === 'AuthWrapper').exports.default.prototype.mobileTransitionTo = () => {};", null
                    );

                    view.setVisibility(View.VISIBLE);
                }
            });
            loginView.loadUrl("https://discord.com/login");
        }

        public void login(String token) {
            StoreAuthentication.access$dispatchLogin(StoreStream.getAuthentication(), new ModelLoginResult(false, null, token, null));
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
            btn.setText("Alternative login");
            btn.setTextSize(16.0f);
            if (StoreStream.getUserSettings().getTheme().equals("light"))
                btn.setBackgroundColor(context.getResources().getColor(R$c.uikit_btn_bg_color_selector_secondary_light));
            else btn.setBackgroundColor(context.getResources().getColor(R$c.uikit_btn_bg_color_selector_secondary_dark));
            btn.setOnClickListener(e -> Utils.openPage(e.getContext(), Page.class));
            v.addView(btn);

            return ret;
        });

        Patcher.addPatch("com.discord.app.AppActivity", "h", (_this, args, ret) -> {
            if (!((boolean) ret) && ((AppActivity) _this).d().equals(Page.class)) return true;
            return ret;
        });
    }

    @Override
    public void stop(Context context) {}
}
