/*
 * Copyright (c) 2021 Juby210
 * Licensed under the Open Software License version 3.0
 */

package com.aliucord;

import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.TextView;

import androidx.core.content.res.ResourcesCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.aliucord.fragments.AppFragmentProxy;
import com.aliucord.utils.ReflectUtils;
import com.discord.api.commands.CommandChoice;
import com.discord.api.user.User;
import com.discord.api.user.UserAvatar;
import com.discord.app.AppActivity;
import com.discord.app.AppComponent;
import com.discord.utilities.SnowflakeUtils;
import com.discord.utilities.fcm.NotificationClient;
import com.discord.views.CheckedSetting;
import com.google.gson.Gson;

import java.io.InputStream;
import java.lang.reflect.Type;
import java.util.Map;
import java.util.Objects;

import c.a.d.l;
import c.a.k.b;
import rx.Subscriber;
import rx.functions.Action0;
import rx.functions.Action1;

@SuppressWarnings("unused")
public class Utils {
    public static AppActivity appActivity;
    public static Context appContext;

    public static void setAppContext() {
        appContext = NotificationClient.access$getContext$p(NotificationClient.INSTANCE);
    }
    public static Context getAppContext() {
        if (appContext == null) setAppContext();
        return appContext;
    }

    public static int getResId(String name, String type) {
        Context context = getAppContext();
        if (context == null) return 0;
        return context.getResources().getIdentifier(name, type, "com.discord");
    }

    private static float density;
    public static int dpToPx(int dp) { return Utils.dpToPx((float) dp); }
    public static int dpToPx(float dp) {
        if (density == 0) density = Utils.getAppContext().getResources().getDisplayMetrics().density;
        return (int) (dp * Utils.getAppContext().getResources().getDisplayMetrics().density + 0.5f);
    }

    private static int defaultPadding = 0;
    public static int getDefaultPadding() {
        if (defaultPadding == 0) defaultPadding = Utils.dpToPx(16);
        return defaultPadding;
    }

    public static <K, V> K getMapKey(Map<K, V> map, V val) {
        for (Map.Entry<K, V> entry : map.entrySet()) {
            if (Objects.equals(val, entry.getValue())) return entry.getKey();
        }
        return null;
    }

    public static void openPage(Context context, Class<? extends AppComponent> clazz) { Utils.openPage(context, clazz, null); }
    public static void openPage(Context context, Class<? extends AppComponent> clazz, Intent intent) { l.d(context, clazz, intent); }
    public static void openPageWithProxy(Context context, Fragment fragment) {
        String id = String.valueOf(SnowflakeUtils.fromTimestamp(System.currentTimeMillis() * 100));
        AppFragmentProxy.fragments.put(id, fragment);
        Utils.openPage(context, AppFragmentProxy.class, new Intent().putExtra("AC_FRAGMENT_ID", id));
    }

    public static CommandChoice createCommandChoice(String name, String value) {
        CommandChoice choice = new CommandChoice();
        try {
            ReflectUtils.setField(choice, "name", name, true);
            ReflectUtils.setField(choice, "value", name, true);
        } catch (Throwable e) { Main.logger.error(e); }
        return choice;
    }

    public static User buildClyde(String name, String avatarUrl) {
        if (name == null) {
            name = "Clyde";
        }

        if (avatarUrl == null) {
            avatarUrl = "https://canary.discord.com/assets/f78426a064bc9dd24847519259bc42af.png";
        }

        return new User(
            -1,
            name,
            new UserAvatar.Avatar(avatarUrl),
            "0000",
            0,
            null,
            true,
            false,
            null,
            null,
            false,
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            null
        );
    }

    /** @deprecated Use ReflectUtils */
    @Deprecated
    public static Object getPrivateField(Class<?> clazz, Object instance, String fieldName) throws Exception {
        return ReflectUtils.getField(clazz, instance, fieldName, true);
    }

    /** @deprecated Use ReflectUtils */
    @Deprecated
    public static void setPrivateField(Class<?> clazz, Object instance, String fieldName, Object v) throws Exception {
        ReflectUtils.setField(clazz, instance, fieldName, v, true);
    }

    public static CheckedSetting createCheckedSetting(Context context, CheckedSetting.ViewType type, CharSequence text, CharSequence subtext) {
        CheckedSetting cs = new CheckedSetting(context, null);
        if (!type.equals(CheckedSetting.ViewType.CHECK)) {
            cs.removeAllViews();
            cs.f(type);
        }

        TextView textView = cs.k.a();
        textView.setTextSize(16.0f);
        textView.setTypeface(ResourcesCompat.getFont(context, Constants.Fonts.whitney_medium));
        textView.setText(text);
        cs.setSubtext(subtext);

        View root = cs.k.b();
        root.setPadding(0, root.getPaddingTop(), root.getPaddingRight(), root.getPaddingBottom());
        return cs;
    }

    public static Fragment chatListFragment;
    public static void rerenderChat() {
        if (chatListFragment == null || chatListFragment.isStateSaved()) return;
        FragmentManager manager = chatListFragment.getFragmentManager();
        if (manager == null) return;
        new Handler(Looper.getMainLooper()).post(() -> {
            FragmentTransaction ft = manager.beginTransaction();
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                ft.detach(chatListFragment).commitNow();
                manager.beginTransaction().attach(chatListFragment).commitNow();
            } else ft.detach(chatListFragment).attach(chatListFragment).commit();
        });
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    public static byte[] readBytes(InputStream stream) throws Throwable {
        int len = stream.available();
        byte[] buf = new byte[len];
        stream.read(buf);
        stream.close();
        return buf;
    }

    public final static Gson gson = new Gson();
    public final static Gson gsonPretty = new Gson();
    public static <T> T fromJson(String json, Type type) { return gson.g(json, type); }
    public static String toJson(Object obj) { return gson.l(obj); }
    public static String toJsonPretty(Object obj) { return gsonPretty.l(obj); }

    public static CharSequence renderMD(CharSequence source) {
        return b.k(source, new Object[0], null, 2);
    }

    public static <T> Subscriber<T> createActionSubscriber(Action1<? super T> onNext) {
        return createActionSubscriber(onNext, null, null);
    }
    public static <T> Subscriber<T> createActionSubscriber(Action1<? super T> onNext, Action1<Throwable> onError, Action0 onCompleted) {
        return new j0.l.e.b<>(onNext, onError == null ? e -> {} : onError, onCompleted == null ? () -> {} : onCompleted);
    }

    public static void log(String msg) { Main.logger.debug(msg); }

    static {
        try {
            ReflectUtils.setField(gsonPretty, "k", true, true);
        } catch (Throwable e) { Main.logger.error(e); }
    }
}
