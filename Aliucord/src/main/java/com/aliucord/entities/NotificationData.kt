/*
 * Copyright (c) 2021 Juby210 & Vendicated
 * Licensed under the Open Software License version 3.0
 */
package com.aliucord.entities

import android.graphics.drawable.Drawable
import android.view.View
import com.discord.api.sticker.Sticker
import com.discord.app.AppComponent
import d0.e0.c

/**
 * Notification builder.
 */
@Suppress("unused", "MemberVisibilityCanBePrivate")
class NotificationData {
    /**
     * @return Title of the notification.
     * @see NotificationData.setTitle
     */
    var title: CharSequence? = null
        private set

    /**
     * @return Subtitle of the notification.
     * @see NotificationData.setSubtitle
     */
    var subtitle: CharSequence? = null
        private set

    /**
     * @return Body of the notification.
     * @see NotificationData.setBody
     */
    var body: CharSequence? = null
        private set

    /**
     * @return Attachment background [Drawable] of the notification.
     * @see NotificationData.setAttachmentBackground
     */
    var attachmentBackground: Drawable? = null
        private set

    /**
     * @return Attachment URL of the notification.
     * @see NotificationData.setAttachmentUrl
     */
    var attachmentUrl: String? = null
        private set

    /**
     * @return Attachment [Drawable] of the notification.
     * @see NotificationData.setAttachment
     */
    var attachment: Drawable? = null
        private set

    /**
     * @return Stickers of the notification.
     * @see NotificationData.setStickers
     */
    var stickers: List<Sticker>? = null
        private set

    /**
     * @return Icon URL of the notification.
     * @see NotificationData.setIconUrl
     */
    var iconUrl = ""
        private set

    /**
     * @return Title of the notification
     * @see NotificationData.setTitle
     */
    var iconResId: Int? = null
        private set

    /**
     * @return [Drawable] of the icon in the top right corner of the notification.
     * @see NotificationData.setIconTopRight
     */
    var iconTopRight: Drawable? = null
        private set

    /**
     * @return Auto dismiss period time in seconds
     * @see NotificationData.setAutoDismissPeriodSecs
     */
    var autoDismissPeriodSecs: Int? = 5
        private set

    /**
     * @return Valid screens
     * @see NotificationData.setValidScreens
     */
    var validScreens: List<c<out AppComponent?>?>? = null
        private set

    /**
     * @return Block corresponding the onClick action of the icon in the top right corner of the notification.
     * @see NotificationData.setOnClickTopRightIcon
     */
    var onClickTopRightIcon: Function1<View?, Unit>? = null
        private set

    /**
     * @return Block corresponding the onClick action of the notification.
     * @see NotificationData.setTitle
     */
    var onClick: Function1<View?, Unit>? = null
        private set

    /**
     * Sets the title.
     * @param title Title.
     * @return [NotificationData] for chaining.
     */
    fun setTitle(title: CharSequence?): NotificationData {
        this.title = title
        return this
    }

    /**
     * Sets the subtitle.
     * @param subtitle Subtitle.
     * @return [NotificationData] for chaining.
     */
    fun setSubtitle(subtitle: CharSequence?): NotificationData {
        this.subtitle = subtitle
        return this
    }

    /**
     * Sets the body.
     * @param body Body.
     * @return [NotificationData] for chaining.
     */
    fun setBody(body: CharSequence?): NotificationData {
        this.body = body
        return this
    }

    /**
     * Sets the background for the attachment.
     * @param attachmentBackground Background [Drawable] of the attachment.
     * @return [NotificationData] for chaining.
     * @see NotificationData.setAttachment
     * @see NotificationData.setAttachmentUrl
     */
    fun setAttachmentBackground(attachmentBackground: Drawable?): NotificationData {
        this.attachmentBackground = attachmentBackground
        return this
    }

    /**
     * Sets the attachment URL.
     * @param attachmentUrl URL of the attachment.
     * @return [NotificationData] for chaining.
     */
    fun setAttachmentUrl(attachmentUrl: String?): NotificationData {
        this.attachmentUrl = attachmentUrl
        return this
    }

    /**
     * Sets the attachment.
     * @param attachment Attachment [Drawable].
     * @return [NotificationData] for chaining.
     */
    fun setAttachment(attachment: Drawable?): NotificationData {
        this.attachment = attachment
        return this
    }

    /**
     * Sets the stickers.
     * @param stickers [List] of stickers.
     * @return [NotificationData] for chaining.
     */
    fun setStickers(stickers: List<Sticker>?): NotificationData {
        this.stickers = stickers
        return this
    }

    /**
     * Sets the icon URL.
     * @param iconUrl URL of the icon.
     * @return [NotificationData] for chaining.
     */
    fun setIconUrl(iconUrl: String): NotificationData {
        this.iconUrl = iconUrl
        return this
    }

    /**
     * Sets the icon.
     * @param iconResId [IdRes] of the icon [Drawable].
     * @return [NotificationData] for chaining.
     */
    fun setIconResId(iconResId: Int?): NotificationData {
        this.iconResId = iconResId
        return this
    }

    /**
     * Sets the icon on the top right corner of the notification.
     * @param iconTopRight [Drawable] of the icon.
     * @return [NotificationData] for chaining.
     */
    fun setIconTopRight(iconTopRight: Drawable?): NotificationData {
        this.iconTopRight = iconTopRight
        return this
    }

    /**
     * Sets the auto dismiss period.
     * @param autoDismissPeriodSecs Dismiss period in seconds.
     * @return [NotificationData] for chaining.
     */
    fun setAutoDismissPeriodSecs(autoDismissPeriodSecs: Int?): NotificationData {
        this.autoDismissPeriodSecs = autoDismissPeriodSecs
        return this
    }

    /**
     * Sets valid screens for the notification.
     * @param validScreens Valid screens.
     * @return [NotificationData] for chaining.
     */
    fun setValidScreens(validScreens: List<c<out AppComponent?>?>?): NotificationData {
        this.validScreens = validScreens
        return this
    }

    /**
     * Sets the callback for the notification top right corner icon onClick action.
     * @param onClickTopRightIcon Block to execute after clicking the icon in the top right corner of the notification.
     * @return [NotificationData] for chaining.
     */
    fun setOnClickTopRightIcon(onClickTopRightIcon: Function1<View?, Unit>?): NotificationData {
        this.onClickTopRightIcon = onClickTopRightIcon
        return this
    }

    /**
     * Sets the callback for the notification onClick action.
     * @param onClick Block to execute after clicking the notification.
     * @return [NotificationData] for chaining.
     */
    fun setOnClick(onClick: Function1<View?, Unit>?): NotificationData {
        this.onClick = onClick
        return this
    }
}
