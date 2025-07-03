/*
 * This file is part of Aliucord, an Android Discord client mod.
 * Copyright (c) 2024 Juby210 & Vendicated
 * Licensed under the Open Software License version 3.0
 */

package com.aliucord.coreplugins.rn

import android.content.Context
import android.view.View
import com.aliucord.api.rn.user.RNUserProfile
import com.aliucord.patcher.*
import com.aliucord.wrappers.users.globalName
import com.discord.api.channel.Channel
import com.discord.api.channel.`ChannelUtils$getDisplayName$1`
import com.discord.api.sticker.Sticker
import com.discord.api.sticker.StickerFormatType
import com.discord.api.sticker.StickerPartial
import com.discord.api.user.User
import com.discord.api.user.UserProfile
import com.discord.app.AppFragment
import com.discord.models.member.GuildMember
import com.discord.models.user.CoreUser
import com.discord.models.user.MeUser
import com.discord.stores.*
import com.discord.utilities.auth.`AuthUtils$createDiscriminatorInputValidator$1`
import com.discord.utilities.icon.IconUtils
import com.discord.utilities.user.UserUtils
import com.discord.widgets.settings.account.WidgetSettingsAccountUsernameEdit
import com.discord.widgets.user.UserNameFormatterKt
import com.discord.widgets.user.WidgetUserPasswordVerify
import com.discord.widgets.user.profile.UserProfileHeaderView
import com.discord.widgets.user.profile.UserProfileHeaderViewModel
import com.google.android.material.textfield.TextInputLayout
import com.google.gson.reflect.TypeToken
import de.robv.android.xposed.XC_MethodHook
import okhttp3.*
import rx.Observable
import java.lang.reflect.Type
import java.util.Collections
import com.discord.models.user.User as ModelUser

fun patchNextCallAdapter() {
    val oldUserProfile = TypeToken.getParameterized(Observable::class.java, UserProfile::class.java).type
    val newUserProfile = TypeToken.getParameterized(Observable::class.java, RNUserProfile::class.java).type

    // nextCallAdapter https://github.com/square/retrofit/blob/c0fd64b5d3ddcc6665a16a4814c5b1596762305d/retrofit/src/main/java/retrofit2/Retrofit.java#L252
    Patcher.addPatch(i0.y::class.java.getDeclaredMethod("a", Type::class.java, Array<Annotation>::class.java), PreHook {
        if (it.args[0] == oldUserProfile) it.args[0] = newUserProfile
    })
}

fun patchGlobalName() {
    val apiUser = User::class.java
    val coreUser = CoreUser::class.java
    val meUser = MeUser::class.java

    Patcher.addPatch(coreUser.getDeclaredConstructor(apiUser), Hook {
        (it.args[0] as User).globalName?.let { name -> (it.thisObject as CoreUser).globalName = name }
    })
    Patcher.addPatch(CoreUser.Companion::class.java.getDeclaredMethod("merge", coreUser, apiUser), Hook {
        ((it.args[1] as User).globalName ?: (it.args[0] as CoreUser).globalName)?.let { name ->
            (it.result as CoreUser).globalName = name
        }
    })

    Patcher.addPatch(meUser.getDeclaredConstructor(apiUser), Hook {
        (it.args[0] as User).globalName?.let { name -> (it.thisObject as MeUser).globalName = name }
    })
    Patcher.addPatch(MeUser.Companion::class.java.getDeclaredMethod("merge", meUser, apiUser), Hook {
        ((it.args[1] as User).globalName ?: (it.args[0] as MeUser).globalName)?.let { name ->
            (it.result as MeUser).globalName = name
        }
    })

    Patcher.addPatch(UserUtils::class.java.getDeclaredMethod("padDiscriminator", Int::class.java), PreHook {
        if (it.args[0] == 0) it.result = ""
    })

    val modelUser = ModelUser::class.java
    Patcher.addPatch(GuildMember.Companion::class.java.getDeclaredMethod("getNickOrUsername", modelUser, GuildMember::class.java, Channel::class.java, List::class.java), Hook {
        val user = it.args[0] as ModelUser
        if (it.result == user.username) user.globalName?.let { name -> it.result = name }
    })

    Patcher.addPatch(UserNameFormatterKt::class.java.getDeclaredMethod("getSpannableForUserNameWithDiscrim", modelUser, String::class.java, Context::class.java, Int::class.java, Int::class.java, Int::class.java, Int::class.java, Int::class.java, Int::class.java), PreHook {
        if (it.args[1] == null) (it.args[0] as ModelUser).globalName?.let { name -> it.args[1] = name }
    })

    Patcher.addPatch(UserProfileHeaderView::class.java.getDeclaredMethod("getSecondaryNameTextForUser", modelUser, GuildMember::class.java), PreHook {
        val user = it.args[0] as ModelUser
        if (user.globalName != null) it.result = UserUtils.INSTANCE.getUserNameWithDiscriminator(user, null, null)
    })
    val headerViewModel = UserProfileHeaderViewModel.ViewState.Loaded::class.java
    Patcher.addPatch(UserProfileHeaderView::class.java.getDeclaredMethod("configureSecondaryName", headerViewModel), object : XC_MethodHook() {
        val showAkasField = headerViewModel.getDeclaredField("showAkas").apply { isAccessible = true }
        var showAkas: Any? = null

        override fun beforeHookedMethod(param: MethodHookParam) {
            showAkas = showAkasField[param.args[0]]
            showAkasField[param.args[0]] = false
        }

        override fun afterHookedMethod(param: MethodHookParam) {
            showAkasField[param.args[0]] = showAkas
        }
    })

    Patcher.addPatch(`ChannelUtils$getDisplayName$1`::class.java.getDeclaredMethod("invoke", Any::class.java), PreHook {
        (it.args[0] as User).globalName?.let { name -> it.result = name }
    })
}

fun patchDefaultAvatars() {
    Patcher.addPatch(
        IconUtils::class.java.getDeclaredMethod(
            "getForUser",
            Long::class.javaObjectType,
            String::class.java,
            Int::class.javaObjectType,
            Boolean::class.java,
            Int::class.javaObjectType
        ),
        InsteadHook {
            val id = it.args[0] as Long?
            val avatar = it.args[1] as String?

            if (avatar != null && id != null) {
                val animated = it.args[3] as Boolean
                val size = it.args[4] as Int?
                val ext = IconUtils.INSTANCE.getImageExtension(avatar, animated)

                "https://cdn.discordapp.com/avatars/$id/$avatar.$ext" +
                    (size?.let { "?size=${IconUtils.getMediaProxySize(size)}" } ?: "")
            } else {
                val discrim = it.args[2] as Int?

                "asset://asset/images/default_avatar_" +
                    (discrim?.takeUnless { it == 0 }?.mod(5) ?: ((id ?: 0) shr 22).mod(6)) +
                    ".png"
            }
        }
    )
}

fun patchUsername() {
    if (StoreStream.getUsers().me.discriminator != 0) return

    Patcher.addPatch(
        `AuthUtils$createDiscriminatorInputValidator$1`::class.java.getDeclaredMethod("getErrorMessage", TextInputLayout::class.java),
        InsteadHook.DO_NOTHING
    )
    Patcher.addPatch(WidgetSettingsAccountUsernameEdit::class.java.getDeclaredMethod("configureUI", MeUser::class.java), Hook {
        val binding = WidgetSettingsAccountUsernameEdit.`access$getBinding$p`(it.thisObject as WidgetSettingsAccountUsernameEdit)

        (binding.b.parent as View).visibility = View.GONE
    })

    Patcher.addPatch(WidgetUserPasswordVerify::class.java.getDeclaredMethod("updateAccountInfo", String::class.java), PreHook {
        (it.thisObject as AppFragment).mostRecentIntent.removeExtra("INTENT_EXTRA_DISCRIMINATOR")
    })
}

fun patchUserProfile() {
    /** discord doesn't check in [com.discord.widgets.user.WidgetUserMutualGuilds.Model] if mutualGuilds list is null */
    Patcher.addPatch(UserProfile::class.java.getDeclaredMethod("d"), Hook {
        if (it.result == null) it.result = Collections.EMPTY_LIST
    })
}

fun patchStickers() {
    val hook = Hook {
        if (it.result == StickerFormatType.UNKNOWN) it.result = StickerFormatType.PNG
    }
    Patcher.addPatch(Sticker::class.java.getDeclaredMethod("a"), hook)
    Patcher.addPatch(StickerPartial::class.java.getDeclaredMethod("a"), hook)
}

fun patchVoice() {
    // don't send heartbeat ("op": 3) on connect
    Patcher.addPatch(b.a.q.n0.a::class.java.getDeclaredMethod("k"), InsteadHook.DO_NOTHING)
}
