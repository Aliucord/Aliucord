/*
 * This file is part of Aliucord, an Android Discord client mod.
 * Copyright (c) 2023 Juby210 & Vendicated
 * Licensed under the Open Software License version 3.0
 */

package com.aliucord.coreplugins

import android.content.Context
import com.aliucord.Http
import com.aliucord.entities.CorePlugin
import com.aliucord.patcher.*
import com.aliucord.utils.GsonUtils
import com.discord.api.channel.Channel
import com.discord.api.message.Message
import com.discord.api.sticker.Sticker
import com.discord.api.sticker.StickerType
import com.discord.models.user.User
import com.discord.stores.StoreStream
import com.discord.utilities.messagesend.`MessageQueue$doSend$2`
import com.discord.utilities.rest.SendUtils
import com.discord.utilities.stickers.StickerUtils
import com.discord.utilities.stickers.StickerUtils.StickerSendability
import com.discord.utilities.user.UserUtils
import com.discord.widgets.chat.input.sticker.*
import de.robv.android.xposed.XC_MethodHook
import rx.subjects.BehaviorSubject
import java.util.Locale

internal class DefaultStickers : CorePlugin(Manifest("DefaultStickers")) {
    override val isHidden = true
    override val isRequired = true

    @Suppress("UNCHECKED_CAST")
    override fun start(context: Context) {
        val stickerPickerViewModel = StickerPickerViewModel::class.java
        val localeField = stickerPickerViewModel.getDeclaredField("locale").apply { isAccessible = true }
        Patcher.addPatch(stickerPickerViewModel.getDeclaredMethod("createCategoryItems", StickerPickerViewModel.StoreState.Loaded::class.java, List::class.java, List::class.java), object : XC_MethodHook() {
            override fun beforeHookedMethod(param: MethodHookParam) {
                val data = param.args[0] as StickerPickerViewModel.StoreState.Loaded
                val me = data.meUser
                if (!UserUtils.INSTANCE.getCanUsePremiumStickers(me)) {
                    val items = param.args[1] as MutableList<*>
                    val companion = StickerPickerViewModel.Companion
                    val locale = localeField[param.thisObject] as Locale
                    val query = data.searchInputStringUpper.lowercase(locale)
                    val animationSettings = data.stickerAnimationSettings

                    for (pack in data.enabledStickerPacks) {
                        items.addAll(StickerPickerViewModel.Companion.`access$buildStickerListItems`(companion, pack, query, animationSettings, locale, me) as List<Nothing>)
                    }
                }
            }

            override fun afterHookedMethod(param: MethodHookParam) {
                val data = param.args[0] as StickerPickerViewModel.StoreState.Loaded
                if (!UserUtils.INSTANCE.getCanUsePremiumStickers(data.meUser)) {
                    val res = param.result as MutableList<StickerCategoryItem>
                    val selected = data.selectedCategoryId
                    var i = res.lastOrNull()?.categoryRange?.second ?: 0
                    for (pack in data.enabledStickerPacks) {
                        val j = pack.stickers.size + 1 + i
                        res.add(StickerCategoryItem.PackItem(pack, i to j, selected == pack.id))
                        i = j
                    }
                }
            }
        })

        Patcher.addPatch(StickerUtils::class.java.getDeclaredMethod("getStickerSendability", Sticker::class.java, User::class.java, Channel::class.java, Long::class.javaObjectType), PreHook { (it, sticker: Sticker) ->
            if (sticker.k() == StickerType.STANDARD) it.result = StickerSendability.SENDABLE
        })

        patcher.before<`MessageQueue$doSend$2`<*, *>>("call", SendUtils.SendPayload.ReadyToSend::class.java) { (it, payload: SendUtils.SendPayload.ReadyToSend) ->
            if (!UserUtils.INSTANCE.getCanUsePremiumStickers(StoreStream.getUsers().me)) {
                val message = payload.message
                if (message.stickerIds.isNotEmpty()) {
                    it.result = BehaviorSubject.l0(Http.Request.newDiscordRNRequest("/channels/${`$message`.channelId}/messages", "POST").executeWithJson(GsonUtils.gsonRestApi, message).json(GsonUtils.gsonRestApi, Message::class.java))
                }
            }
        }
    }

    override fun stop(context: Context) {}
}
