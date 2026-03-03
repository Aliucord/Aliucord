package com.aliucord.coreplugins

import android.content.Context
import com.aliucord.entities.CorePlugin
import com.aliucord.patcher.*
import com.aliucord.wrappers.ChannelWrapper.Companion.id
import com.discord.api.permission.Permission
import com.discord.stores.StoreSlowMode
import com.discord.stores.StoreStream
import com.discord.utilities.permissions.PermissionUtils
import com.discord.widgets.chat.overlay.WidgetChatOverlay
import com.lytefast.flexinput.R

private const val BYPASS_SLOWMODE_PERMISSION = 1L shl 52

internal class SlowmodeFix : CorePlugin(Manifest("SlowmodeFix")) {
    override val isHidden = true
    override val isRequired = true

    override fun start(context: Context) {
        // Patches the bypass slowmode permission checker to use the new permission instead
        patcher.instead<PermissionUtils>(
            "hasBypassSlowmodePermissions",
            Long::class.javaObjectType,
            StoreSlowMode.Type::class.java,
        ) { (_, permissions: Long?) ->
            PermissionUtils.can(Permission.ADMINISTRATOR, permissions)
                || PermissionUtils.can(BYPASS_SLOWMODE_PERMISSION, permissions)
        }

        // Configures the slowmode text indicator to show immunity status
        patcher.after<WidgetChatOverlay.TypingIndicatorViewHolder>(
            "getSlowmodeText",
            Int::class.javaPrimitiveType!!,
            Int::class.javaPrimitiveType!!,
            Boolean::class.javaPrimitiveType!!,
        ) { param ->
            // Empty only if there's no slow mode
            if (param.result == "") return@after

            val channel = StoreStream.getChannelsSelected().selectedChannel
            val permissions = StoreStream.getPermissions().permissionsByChannel[channel.id]
            if (PermissionUtils.INSTANCE.hasBypassSlowmodePermissions(permissions, StoreSlowMode.Type.MessageSend.INSTANCE)) {
                param.result = context.resources.getString(R.h.channel_slowmode_desc_immune)
            }
        }
    }
}
