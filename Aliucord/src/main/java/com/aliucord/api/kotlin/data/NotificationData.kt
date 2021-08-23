package com.aliucord.api.kotlin.data

import android.graphics.drawable.Drawable
import android.view.View
import com.aliucord.entities.NotificationData
import com.discord.api.sticker.Sticker
import com.discord.app.AppComponent
import d0.e0.c

fun notificationData(
    attachment: Drawable? = null,
    attachmentBackground: Drawable? = null,
    attachmentUrl: String? = null,
    autoDismissPeriodSecs: Int = 5,
    body: String? = null,
    iconResId: Int? = null,
    iconTopRight: Drawable? = null,
    iconUrl: String = "",
    onClick: ((View) -> Unit)? = null,
    onClickTopRightIcon: ((View) -> Unit)? = null,
    stickers: List<Sticker>? = null,
    subtitle: String? = null,
    title: String? = null,
    validScreens: List<c<out AppComponent?>?>? = null,
) = NotificationData()
    .setAttachment(attachment)
    .setAttachmentBackground(attachmentBackground)
    .setAttachmentUrl(attachmentUrl)
    .setAutoDismissPeriodSecs(autoDismissPeriodSecs)
    .setBody(body)
    .setIconResId(iconResId)
    .setIconTopRight(iconTopRight)
    .setIconUrl(iconUrl)
    .setOnClick(onClick)
    .setOnClickTopRightIcon(onClickTopRightIcon)
    .setStickers(stickers)
    .setSubtitle(subtitle)
    .setTitle(title)
    .setValidScreens(validScreens)