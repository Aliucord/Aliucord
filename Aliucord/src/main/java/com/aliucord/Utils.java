/*
 * Copyright (c) 2021 Juby210 & Vendicated
 * Licensed under the Open Software License version 3.0
 */

package com.aliucord;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.core.content.res.ResourcesCompat;
import androidx.fragment.app.Fragment;

import com.aliucord.fragments.AppFragmentProxy;
import com.aliucord.utils.ReflectUtils;
import com.aliucord.utils.RxUtils;
import com.discord.api.commands.CommandChoice;
import com.discord.api.user.User;
import com.discord.app.AppActivity;
import com.discord.app.AppComponent;
import com.discord.nullserializable.NullSerializable;
import com.discord.utilities.SnowflakeUtils;
import com.discord.utilities.fcm.NotificationClient;
import com.discord.views.CheckedSetting;
import com.google.gson.Gson;

import java.io.InputStream;
import java.lang.reflect.Type;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import c.a.d.l;
import c.a.l.b;
import rx.Subscriber;
import rx.functions.Action0;
import rx.functions.Action1;

@SuppressWarnings("unused")
public class Utils {
    /** The main (UI) thread */
    public static final Handler mainThread = new Handler(Looper.getMainLooper());
    /**
     * ThreadPool. Please use this for asynchronous Tasks instead of creating Threads manually
     * as spinning up new Threads everytime is heavy on the CPU
     */
    public static final ExecutorService threadPool = Executors.newCachedThreadPool();
    public static AppActivity appActivity;
    public static Context appContext;

    /**
     * Sets the clipboard content
     * @param label User-visible label for the clip data
     * @param text The actual text
     */
    public static void setClipboard(CharSequence label, CharSequence text) {
        ClipboardManager clipboard = (ClipboardManager) getAppContext().getSystemService(Context.CLIPBOARD_SERVICE);
        clipboard.setPrimaryClip(ClipData.newPlainText(label, text));
    }

    /**
     * Converts the singular term of the <code>noun</code> into plural.
     * @param amount Amount of the noun.
     * @param noun The noun
     * @return Pluralised <code>noun</code>
     */
    public static String pluralise(int amount, String noun) {
        return String.format(Locale.ENGLISH, "%d %s%s", amount, noun, amount == 1 ? "" : "s");
    }

    /**
     * Send a toast from any {@link Thread}
     * @param ctx {@link Context}
     * @param message Message to show.
     * @param showLonger Whether to show toast for an extended period of time.
     */
    public static void showToast(Context ctx, String message, boolean showLonger) {
        mainThread.post(() -> Toast.makeText(ctx, message, showLonger ? Toast.LENGTH_LONG : Toast.LENGTH_SHORT).show());
    }

    /**
     * Send a toast from any {@link Thread}
     * @param ctx {@link Context}
     * @param message Message to show.
     */
    public static void showToast(Context ctx, String message) {
        showToast(ctx, message, false);
    }

    public static void setAppContext() {
        appContext = NotificationClient.access$getContext$p(NotificationClient.INSTANCE);
    }

    /**
     * Retrieves {@link android.app.Application} {@link Context}
     * @return {@link Context}
     */
    public static Context getAppContext() {
        if (appContext == null) setAppContext();
        return appContext;
    }

    /**
     * Get resource id from discord package.
     * @param name Name of the resource.
     * @param type Type of the resource.
     * @return ID of the resource, or 0 if not found.
     */
    public static int getResId(String name, String type) {
        Context context = getAppContext();
        if (context == null) return 0;
        return context.getResources().getIdentifier(name, type, "com.discord");
    }

    private static float density;

    /**
     * Converts DP to PX.
     * @param dp DP value.
     * @return <code>dp</code> converted to PX.
     * @see Utils#dpToPx(float)
     */
    public static int dpToPx(int dp) { return Utils.dpToPx((float) dp); }

    /**
     * Converts DP to PX.
     * @param dp DP value.
     * @return <code>dp</code> converted to PX.
     * @see Utils#dpToPx(int)
     */
    public static int dpToPx(float dp) {
        if (density == 0) density = Utils.getAppContext().getResources().getDisplayMetrics().density;
        return (int) (dp * Utils.getAppContext().getResources().getDisplayMetrics().density + 0.5f);
    }

    private static int defaultPadding = 0;
    private static int defaultCardRadius = 0;

    /**
     * Gets the default padding for the items.
     * @return default padding
     * @see Utils#dpToPx(int)
     * @see Utils#dpToPx(float)
     */
    public static int getDefaultPadding() {
        if (defaultPadding == 0) defaultPadding = Utils.dpToPx(16);
        return defaultPadding;
    }

    public static int getDefaultCardRadius() {
        if (defaultCardRadius == 0) defaultCardRadius = Utils.dpToPx(8);
        return defaultCardRadius;
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
            ReflectUtils.setField(choice, "value", value, true);
        } catch (Throwable e) { Main.logger.error(e); }
        return choice;
    }

    public static User buildClyde() {
        return buildClyde(null, null);
    }
    /**
     * Clyde builder
     * @param name Name of Clyde
     * @param avatarUrl Avatar URL of Clyde
     * @return Customized Clyde
     */
    public static User buildClyde(@Nullable String name, @Nullable String avatarUrl) {
        if (name == null) name = "Clyde";
        if (avatarUrl == null) avatarUrl = Constants.Icons.CLYDE;

        return new User(
            -1,
            name,
            new NullSerializable.b<>(avatarUrl),
            new NullSerializable.a<>(),
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
            null,
            new NullSerializable.a<>(),
            null
        );
    }

    /**
     * @deprecated Use {@link ReflectUtils#getField(Object, String, boolean)} or {@link ReflectUtils#getField(Class, Object, String, boolean)}
     */
    @Deprecated
    public static Object getPrivateField(Class<?> clazz, Object instance, String fieldName) throws Exception {
        return ReflectUtils.getField(clazz, instance, fieldName, true);
    }

    /**
     * @deprecated Use {@link ReflectUtils#setField(Object, String, Object, boolean)} or {@link ReflectUtils#setField(Class, Object, String, Object, boolean)}
     */
    @Deprecated
    public static void setPrivateField(Class<?> clazz, Object instance, String fieldName, Object v) throws Exception {
        ReflectUtils.setField(clazz, instance, fieldName, v, true);
    }

    /**
     * Creates a checkable {@link View}.
     * @param context {@link Context}
     * @param type {@link CheckedSetting.ViewType} of the checkable item.
     * @param text Title of the checkable item.
     * @param subtext Summary of the checkable item.
     * @return Checkable item.
     */
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
    public static String toJson(Object obj) { return gson.m(obj); }
    public static String toJsonPretty(Object obj) { return gsonPretty.m(obj); }

    public static CharSequence renderMD(CharSequence source) {
        try {
            return b.k(source, new Object[0], null, 2);
        } catch (Throwable e) { Main.logger.error("Failed to render markdown", e); }
        return source;
    }

    @Deprecated
    public static <T> Subscriber<T> createActionSubscriber(Action1<? super T> onNext) {
        return RxUtils.createActionSubscriber(onNext);
    }

    @Deprecated
    public static <T> Subscriber<T> createActionSubscriber(Action1<? super T> onNext, Action1<Throwable> onError, Action0 onCompleted) {
        return RxUtils.createActionSubscriber(onNext, onError, onCompleted);
    }

    /**
     * Logs a message.
     * @param msg Message to log.
     */
    public static void log(String msg) { Main.logger.debug(msg); }

    static {
        try {
            ReflectUtils.setField(gsonPretty, "k", true, true);
        } catch (Throwable e) { Main.logger.error(e); }
    }
}
