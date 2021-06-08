/*
 * Copyright (c) 2021 Juby210
 * Licensed under the Open Software License version 3.0
 */

package com.aliucord.entities;

import android.graphics.drawable.Drawable;
import android.view.View;

import androidx.annotation.IdRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.discord.app.AppComponent;
import com.discord.models.sticker.dto.ModelSticker;

import java.util.List;

import c0.e0.c;
import kotlin.Unit;
import kotlin.jvm.functions.Function1;

/**
 * Notification builder.
 */
@SuppressWarnings("unused")
public class NotificationData {
    private CharSequence title;
    private CharSequence subtitle;
    private CharSequence body;
    private Drawable attachmentBackground;
    private String attachmentUrl;
    private Drawable attachment;
    private List<ModelSticker> stickers;
    private String iconUrl = "";
    private Integer iconResId;
    private Drawable iconTopRight;
    private Integer autoDismissPeriodSecs = 5;
    private List<? extends c<? extends AppComponent>> validScreens;
    private Function1<? super View, Unit> onClickTopRightIcon;
    private Function1<? super View, Unit> onClick;

    /**
     * @return Title of the notification.
     * @see NotificationData#setTitle(CharSequence)
     */
    public CharSequence getTitle() {
        return title;
    }

    /**
     * Sets the title.
     * @param title Title.
     * @return {@link NotificationData} for chaining.
     */
    public NotificationData setTitle(@Nullable CharSequence title) {
        this.title = title;
        return this;
    }

    /**
     * @return Subtitle of the notification.
     * @see NotificationData#setSubtitle(CharSequence)
     */
    public CharSequence getSubtitle() {
        return subtitle;
    }

    /**
     * Sets the subtitle.
     * @param subtitle Subtitle.
     * @return {@link NotificationData} for chaining.
     */
    public NotificationData setSubtitle(@Nullable CharSequence subtitle) {
        this.subtitle = subtitle;
        return this;
    }

    /**
     * @return Body of the notification.
     * @see NotificationData#setBody(CharSequence)
     */
    public CharSequence getBody() {
        return body;
    }

    /**
     * Sets the body.
     * @param body Body.
     * @return {@link NotificationData} for chaining.
     */
    public NotificationData setBody(@Nullable CharSequence body) {
        this.body = body;
        return this;
    }

    /**
     * @return Attachment background {@link Drawable} of the notification.
     * @see NotificationData#setAttachmentBackground(Drawable)
     */
    public Drawable getAttachmentBackground() {
        return attachmentBackground;
    }

    /**
     * Sets the background for the attachment.
     * @param attachmentBackground Background {@link Drawable} of the attachment.
     * @return {@link NotificationData} for chaining.
     * @see NotificationData#setAttachment(Drawable)
     * @see NotificationData#setAttachmentUrl(String)
     */
    public NotificationData setAttachmentBackground(@Nullable Drawable attachmentBackground) {
        this.attachmentBackground = attachmentBackground;
        return this;
    }

    /**
     * @return Attachment URL of the notification.
     * @see NotificationData#setAttachmentUrl(String)
     */
    public String getAttachmentUrl() {
        return attachmentUrl;
    }

    /**
     * Sets the attachment URL.
     * @param attachmentUrl URL of the attachment.
     * @return {@link NotificationData} for chaining.
     */
    public NotificationData setAttachmentUrl(@Nullable String attachmentUrl) {
        this.attachmentUrl = attachmentUrl;
        return this;
    }

    /**
     * @return Attachment {@link Drawable} of the notification.
     * @see NotificationData#setAttachment(Drawable) (CharSequence)
     */
    public Drawable getAttachment() {
        return attachment;
    }

    /**
     * Sets the attachment.
     * @param attachment Attachment {@link Drawable}.
     * @return {@link NotificationData} for chaining.
     */
    public NotificationData setAttachment(@Nullable Drawable attachment) {
        this.attachment = attachment;
        return this;
    }

    /**
     * @return Stickers of the notification.
     * @see NotificationData#setStickers(List)
     */
    public List<ModelSticker> getStickers() {
        return stickers;
    }

    /**
     * Sets the stickers.
     * @param stickers {@link List} of stickers.
     * @return {@link NotificationData} for chaining.
     */
    public NotificationData setStickers(@Nullable List<ModelSticker> stickers) {
        this.stickers = stickers;
        return this;
    }

    /**
     * @return Icon URL of the notification.
     * @see NotificationData#setIconUrl(String)
     */
    public String getIconUrl() {
        return iconUrl;
    }

    /**
     * Sets the icon URL.
     * @param iconUrl URL of the icon.
     * @return {@link NotificationData} for chaining.
     */
    public NotificationData setIconUrl(@NonNull String iconUrl) {
        this.iconUrl = iconUrl;
        return this;
    }

    /**
     * @return Title of the notification
     * @see NotificationData#setTitle(CharSequence)
     */
    public Integer getIconResId() {
        return iconResId;
    }

    /**
     * Sets the icon.
     * @param iconResId {@link IdRes} of the icon {@link Drawable}.
     * @return {@link NotificationData} for chaining.
     */
    public NotificationData setIconResId(@Nullable Integer iconResId) {
        this.iconResId = iconResId;
        return this;
    }

    /**
     * @return {@link Drawable} of the icon in the top right corner of the notification.
     * @see NotificationData#setIconTopRight(Drawable)
     */
    public Drawable getIconTopRight() {
        return iconTopRight;
    }

    /**
     * Sets the icon on the top right corner of the notification.
     * @param iconTopRight {@link Drawable} of the icon.
     * @return {@link NotificationData} for chaining.
     */
    public NotificationData setIconTopRight(@Nullable Drawable iconTopRight) {
        this.iconTopRight = iconTopRight;
        return this;
    }

    /**
     * @return Auto dismiss period time in seconds
     * @see NotificationData#setAutoDismissPeriodSecs(Integer)
     */
    public Integer getAutoDismissPeriodSecs() {
        return autoDismissPeriodSecs;
    }

    /**
     * Sets the auto dismiss period.
     * @param autoDismissPeriodSecs Dismiss period in seconds.
     * @return {@link NotificationData} for chaining.
     */
    public NotificationData setAutoDismissPeriodSecs(@Nullable Integer autoDismissPeriodSecs) {
        this.autoDismissPeriodSecs = autoDismissPeriodSecs;
        return this;
    }

    //FIXME Documentation is invalid.
    /**
     * @return Valid screens
     * @see NotificationData#setValidScreens(List)
     */
    public List<? extends c<? extends AppComponent>> getValidScreens() {
        return validScreens;
    }

    //FIXME Documentation is invalid.
    /**
     * Sets valid screens for the notification.
     * @param validScreens Valid screens.
     * @return {@link NotificationData} for chaining.
     */
    public NotificationData setValidScreens(@Nullable List<? extends c<? extends AppComponent>> validScreens) {
        this.validScreens = validScreens;
        return this;
    }

    /**
     * @return Block corresponding the onClick action of the icon in the top right corner of the notification.
     * @see NotificationData#setOnClickTopRightIcon(Function1)
     */
    public Function1<? super View, Unit> getOnClickTopRightIcon() {
        return onClickTopRightIcon;
    }

    /**
     * Sets the callback for the notification top right corner icon onClick action.
     * @param onClickTopRightIcon Block to execute after clicking the icon in the top right corner of the notification.
     * @return {@link NotificationData} for chaining.
     */
    public NotificationData setOnClickTopRightIcon(@Nullable Function1<? super View, Unit> onClickTopRightIcon) {
        this.onClickTopRightIcon = onClickTopRightIcon;
        return this;
    }

    /**
     * @return Block corresponding the onClick action of the notification.
     * @see NotificationData#setTitle(CharSequence)
     */
    public Function1<? super View, Unit> getOnClick() {
        return onClick;
    }

    /**
     * Sets the callback for the notification onClick action.
     * @param onClick Block to execute after clicking the notification.
     * @return {@link NotificationData} for chaining.
     */
    public NotificationData setOnClick(@Nullable Function1<? super View, Unit> onClick) {
        this.onClick = onClick;
        return this;
    }
}
