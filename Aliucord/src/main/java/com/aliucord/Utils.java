/*
 * This file is part of Aliucord, an Android Discord client mod.
 * Copyright (c) 2021 Juby210 & Vendicated
 * Licensed under the Open Software License version 3.0
 */

package com.aliucord;

import android.content.*;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.res.ResourcesCompat;
import androidx.fragment.app.Fragment;

import com.aliucord.fragments.AppFragmentProxy;
import com.aliucord.utils.*;
import com.discord.api.commands.CommandChoice;
import com.discord.api.user.User;
import com.discord.app.AppActivity;
import com.discord.app.AppComponent;
import com.discord.nullserializable.NullSerializable;
import com.discord.stores.StoreStream;
import com.discord.utilities.SnowflakeUtils;
import com.discord.utilities.fcm.NotificationClient;
import com.discord.views.CheckedSetting;
import com.google.gson.Gson;

import java.io.*;
import java.lang.reflect.Type;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import c.a.d.l;
import rx.Subscriber;
import rx.functions.Action0;
import rx.functions.Action1;

/** Utility class that holds miscellaneous Utilities */
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
     * Launches an URL in the user's preferred Browser
     * @param url The url to launch
     */
    public static void launchUrl(String url) {
        launchUrl(Uri.parse(url));
    }

    /**
     * Launches an URL in the user's preferred Browser
     * @param url The url to launch
     */
    public static void launchUrl(Uri url) {
        appActivity.startActivity(new Intent(Intent.ACTION_VIEW).setData(url));
    }

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

    private static final Map<String, Integer> resIdCache = new HashMap<>();
    /**
     * Get resource id from discord package.
     * @param name Name of the resource.
     * @param type Type of the resource.
     * @return ID of the resource, or 0 if not found.
     */
    public static int getResId(String name, String type) {
        Context context = getAppContext();
        if (context == null) return 0;

        return resIdCache.computeIfAbsent(name, k -> context.getResources().getIdentifier(k, type, "com.discord"));
    }

    /**
     * Converts DP to PX.
     * @param dp DP value
     * @return <code>DP</code> converted to PX
     * @deprecated Use {@link DimenUtils#dpToPx(int)}
     */
    @Deprecated
    public static int dpToPx(int dp) { return DimenUtils.dpToPx(dp); }

    /**
     * Converts DP to PX.
     * @param dp DP value
     * @return <code>DP</code> converted to PX
     * @deprecated Use {@link DimenUtils#dpToPx(float)}
     */
    @Deprecated
    public static int dpToPx(float dp) { return DimenUtils.dpToPx(dp); }

    /**
     * Gets the default padding for the items. (16 DP)
     * @return default padding
     * @see DimenUtils#dpToPx(int)
     * @see DimenUtils#dpToPx(float)
     * @deprecated Use {@link DimenUtils#getDefaultPadding()}
     */
    @Deprecated
    public static int getDefaultPadding() { return DimenUtils.getDefaultPadding(); }

    /**
     * Gets the default radius for cards. (8 DP)
     * @return default padding
     * @see DimenUtils#dpToPx(int)
     * @see DimenUtils#dpToPx(float)
     * @deprecated Use {@link DimenUtils#getDefaultCardRadius()}
     */
    @Deprecated
    public static int getDefaultCardRadius() { return DimenUtils.getDefaultCardRadius(); }

    /**
     * Finds the mapping key for Object val where Objects.equals(val, entry.value)
     * @param map The map to find the Object in
     * @param val The object to find the key of
     * @return Key of mapping or null if no such mapping exists
     * @deprecated Use {@link MapUtils#getMapKey(Map, Object)}
     */
    @Nullable
    @Deprecated
    public static <K, V> K getMapKey(@NonNull Map<K, V> map, @Nullable V val) { return MapUtils.getMapKey(map, val); }

    public static void openPage(Context context, Class<? extends AppComponent> clazz, Intent intent) {
        l.d(context, clazz, intent);
    }
    public static void openPage(Context context, Class<? extends AppComponent> clazz) {
        Utils.openPage(context, clazz, null);
    }
    public static void openPageWithProxy(Context context, Fragment fragment) {
        String id = String.valueOf(SnowflakeUtils.fromTimestamp(System.currentTimeMillis() * 100));
        AppFragmentProxy.fragments.put(id, fragment);
        Utils.openPage(context, AppFragmentProxy.class, new Intent().putExtra("AC_FRAGMENT_ID", id));
    }

    /**
     * Creates a CommandChoice that can be used inside Command args
     * @param name The name of the choice
     * @param value The value representing this choice
     * @return CommandChoice
     */
    public static CommandChoice createCommandChoice(String name, String value) {
        return new CommandChoice(name, value);
    }

    /**
     * Builds Clyde User
     * @return Built Clyde
     */
    @NonNull
    public static User buildClyde() {
        return buildClyde(null, null);
    }

    /**
     * Builds a Pseudo Clyde User
     * @param name Name of user
     * @param avatarUrl Avatar URL of user
     * @return Built user
     */
    @NonNull
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
        return ReflectUtils.getField(clazz, instance, fieldName);
    }

    /**
     * @deprecated Use {@link ReflectUtils#setField(Object, String, Object, boolean)} or {@link ReflectUtils#setField(Class, Object, String, Object, boolean)}
     */
    @Deprecated
    public static void setPrivateField(Class<?> clazz, Object instance, String fieldName, Object v) throws Exception {
        ReflectUtils.setField(clazz, instance, fieldName, v);
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

    /**
     * Reads the InputStream into a byte[]
     * @param stream The stream to read
     * @return The read bytes
     * @throws Throwable if an I/O error occurs
     * @deprecated Use {@link IOUtils#readBytes(InputStream)}
     */
    @Deprecated
    public static byte[] readBytes(InputStream stream) throws Throwable { return IOUtils.readBytes(stream); }

    /**
     * Pipe an {@link InputStream} into an {@link OutputStream}
     * @param is InputStream
     * @param os OutputStream
     * @throws IOException if an I/O error occurs
     * @deprecated Use {@link IOUtils#pipe(InputStream, OutputStream)}
     */
    @Deprecated
    public static void pipe(InputStream is, OutputStream os) throws IOException { IOUtils.pipe(is, os); }

    /**
     * Tints a {@link Drawable} to {@link Color#BLACK} if a user has set light theme.
     * @param drawable Drawable
     * @return Drawable for chaining
     */
    public static Drawable tintToTheme(Drawable drawable) {
        if (drawable != null && StoreStream.getUserSettingsSystem().getTheme().equals("light")) drawable.setTint(Color.BLACK);
        return drawable;
    }

    /**
     * <a href="https://github.com/google/gson">Gson</a> instance
     * @deprecated Use {@link GsonUtils#gson}
     */
    @Deprecated
    public final static Gson gson = GsonUtils.gson;

    /**
     * Pretty <a href="https://github.com/google/gson">Gson</a> instance
     * @deprecated Use {@link GsonUtils#gsonPretty}
     */
    @Deprecated
    public final static Gson gsonPretty = GsonUtils.gsonPretty;

    /**
     * Deserializes a JSON string into the specified class
     * @param json The JSON string to deserialize
     * @param clazz The class to deserialize the JSON into
     * @return Deserialized JSON
     * @deprecated Use {@link GsonUtils#fromJson(String, Class)}
     */
    @Deprecated
    public static <T> T fromJson(String json, Class<T> clazz) { return GsonUtils.fromJson(json, clazz); }
    /**
     * Deserializes a JSON string into the specified object
     * @param json The JSON string to deserialize
     * @param type The type of the object to deserialize the JSON into
     * @return Deserialized JSON
     * @deprecated Use {@link GsonUtils#fromJson(String, Type)}
     */
    @Deprecated
    public static <T> T fromJson(String json, Type type) { return GsonUtils.fromJson(json, type); }

    /**
     * Serializes an Object to JSON
     * @param obj The object to serialize
     * @return Serialized JSON
     * @deprecated Use {@link GsonUtils#toJson(Object)}
     */
    @Deprecated
    public static String toJson(Object obj) { return GsonUtils.toJson(obj); }
    /**
     * Serializes an Object to pretty printed JSON
     * @param obj The object to serialize
     * @return Serialized JSON
     * @deprecated Use {@link GsonUtils#toJsonPretty(Object)}
     */
    @Deprecated
    public static String toJsonPretty(Object obj) { return GsonUtils.toJsonPretty(obj); }

    /**
     * Renders discord spice markdown
     * @param source The markdown to render
     * @return Rendered markdown
     * @deprecated Use {@link MDUtils#render(CharSequence)}
     */
    @Deprecated
    public static CharSequence renderMD(CharSequence source) { return MDUtils.render(source); }

    /**
     * Creates new class instance without using a constructor
     * @param clazz Class
     * @return Created instance
     * @deprecated Use {@link ReflectUtils#allocateInstance(Class)}
     */
    @Deprecated
    public static <T> T allocateInstance(@NonNull Class<T> clazz) { return ReflectUtils.allocateInstance(clazz); }

    /**
     * @deprecated Use {@link RxUtils#createActionSubscriber(Action1)}
     */
    @Deprecated
    public static <T> Subscriber<T> createActionSubscriber(Action1<? super T> onNext) {
        return RxUtils.createActionSubscriber(onNext);
    }

    /**
     * @deprecated Use {@link RxUtils#createActionSubscriber(Action1, Action1, Action0)}
     */
    @Deprecated
    public static <T> Subscriber<T> createActionSubscriber(Action1<? super T> onNext, Action1<Throwable> onError, Action0 onCompleted) {
        return RxUtils.createActionSubscriber(onNext, onError, onCompleted);
    }

    /**
     * Logs a message on debug level.
     * @param msg Message to log.
     */
    public static void log(String msg) { Main.logger.debug(msg); }
}
