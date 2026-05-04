package com.aliucord.coreplugins

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.WindowInsetsAnimation
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.aliucord.Main
import com.aliucord.Utils
import com.aliucord.entities.CorePlugin
import com.aliucord.patcher.*
import com.aliucord.utils.DimenUtils.dp
import com.aliucord.utils.ReflectUtils
import com.aliucord.utils.ViewUtils.findViewById
import com.aliucord.utils.accessField
import com.aliucord.wrappers.ChannelWrapper.Companion.id
import com.aliucord.wrappers.embeds.MessageEmbedWrapper
import com.discord.api.channel.Channel
import com.discord.api.message.embed.EmbedField
import com.discord.api.permission.Permission
import com.discord.models.domain.emoji.ModelEmojiCustom
import com.discord.models.domain.emoji.ModelEmojiUnicode
import com.discord.rtcconnection.socket.io.Payloads.Protocol.ProtocolInfo
import com.discord.stores.StoreSlowMode
import com.discord.stores.StoreStream
import com.discord.utilities.drawable.DrawableCompat
import com.discord.utilities.embed.EmbedResourceUtils
import com.discord.utilities.guildautomod.AutoModUtils
import com.discord.utilities.lazy.memberlist.ChannelMemberList
import com.discord.utilities.lazy.memberlist.MemberListRow
import com.discord.utilities.permissions.PermissionUtils
import com.discord.widgets.channels.list.*
import com.discord.widgets.chat.input.SmoothKeyboardReactionHelper
import com.discord.widgets.chat.list.actions.`WidgetChatListActions$binding$2`
import com.discord.widgets.chat.list.adapter.WidgetChatListAdapterItemAutoModSystemMessageEmbed
import com.discord.widgets.chat.list.adapter.WidgetChatListAdapterItemThreadDraftForm
import com.discord.widgets.chat.list.entries.*
import com.discord.widgets.chat.overlay.WidgetChatOverlay
import com.discord.widgets.guilds.list.`WidgetGuildsListViewModel$createDirectMessageItems$1`
import com.linecorp.apng.decoder.Apng
import com.lytefast.flexinput.R
import de.robv.android.xposed.XC_MethodHook.MethodHookParam

private const val BYPASS_SLOWMODE_PERMISSION = 1L shl 52

/**
 * Contains various fixes for stock Discord that ensure "proper" behavior.
 */
internal class CoreFixes : CorePlugin(Manifest("CoreFixes")) {
    override val isHidden = true
    override val isRequired = true

    init {
        manifest.description = "Applies critical patches and fixes to ensure Aliucord functions correctly without errors"
    }

    override fun start(context: Context) {
        fixStockEmojis()
        fixAutoModEmbed()
        fixKeyboardCrash()
        fixWebpEmojis()
        fixGifPreviews()
        fixMemberListGroups()
        fixAppBar()
        fixStickerCrash()
        fixHidingMutedThreads()
        fixHidingMutedChannels()
        fixHidingMutedDMs()
        fixPrivateThreads()
        fixPrivateChannelScroll()
        fixVoiceCodec()
        fixThreadsIcon()
        fixSlowmode()
        fixExternalLinks()
    }

    private fun fixStockEmojis() = tryPatch("Fix built-in emojis") {
        // Patch to repair built-in emotes is needed because installer doesn't recompile resources,
        // so they stay in package com.discord instead of apk package name
        patcher.instead<ModelEmojiUnicode?>("getImageUri", String::class.java, Context::class.java) { param ->
            "res:///${Utils.getResId("emoji_${param.args[0]}", "raw")}"
        }
    }

    private fun fixAutoModEmbed() = tryPatch("Fix AutoMod embed crashes") {
        // Patch to fix crash when displaying newer AutoMod embed types like "Quarantined a member at username update"
        patcher.before<WidgetChatListAdapterItemAutoModSystemMessageEmbed>(
            "onConfigure",
            Int::class.javaPrimitiveType!!, ChatListEntry::class.java
        ) { (_, _: Any, autoModEntry: AutoModSystemMessageEmbedEntry) ->
            // If the channel_id embed field is missing, then just add one set to 0, it'll be displayed as null
            if (AutoModUtils.INSTANCE.getEmbedFieldValue(autoModEntry.embed, "channel_id").isNullOrEmpty()) {
                val fields = MessageEmbedWrapper(autoModEntry.embed).rawFields as ArrayList<EmbedField>

                val newField = ReflectUtils.allocateInstance(EmbedField::class.java)
                ReflectUtils.setField(newField, "name", "channel_id")
                ReflectUtils.setField(newField, "value", "0")

                fields += newField
            }
        }
    }

    private fun fixKeyboardCrash() {
        // not sure why this happens, reported on Android 15 Beta 4
        // java.lang.IllegalArgumentException: Animators cannot have negative duration: -1
        //   at android.view.ViewPropertyAnimator.setDuration(ViewPropertyAnimator.java:266)
        //   at com.discord.widgets.chat.input.SmoothKeyboardReactionHelper$Callback.onStart(SmoothKeyboardReactionHelper.kt:5)
        //   at android.view.View.dispatchWindowInsetsAnimationStart(View.java:12671)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            tryPatch("Fix keyboard animation crash on Android 15") {
                patcher.before<SmoothKeyboardReactionHelper.Callback>(
                    "onStart",
                    WindowInsetsAnimation::class.java,
                    WindowInsetsAnimation.Bounds::class.java
                ) { (param, animation: WindowInsetsAnimation) ->
                    if (animation.durationMillis < 0) param.result = param.args[1]
                }
            }
        }
    }

    private fun fixWebpEmojis() = tryPatch("Fix emoji formats") {
        // Support webp emojis by forcing every emoji to be webp
        patcher.instead<ModelEmojiCustom?>(
            "getImageUri",
            Long::class.javaPrimitiveType!!, Boolean::class.javaPrimitiveType!!, Int::class.javaPrimitiveType!!,
        ) { (_, id: Long, animated: Boolean, size: Int) ->
            "https://cdn.discordapp.com/emojis/$id.webp?size=$size&animated=$animated"
        }
    }

    private fun fixGifPreviews() = tryPatch("Fix GIF previews") {
        patcher.after<EmbedResourceUtils>(
            "getPreviewUrls",
            String::class.java, Int::class.java, Int::class.java, Boolean::class.java,
        ) { (params, _: String, _: Int, _: Int, animated: Boolean) ->
            if (!animated) return@after

            @Suppress("UNCHECKED_CAST")
            val urls = (params.result as List<String>).toMutableList()

            @SuppressLint("UseKtx")
            val uri = Uri.parse(urls[0].replace("&?", "&"))
                ?.takeIf { it.path?.endsWith(".gif") == true }
                ?: return@after

            val filteredQueryKeys = uri.queryParameterNames.filter { it != "format" }

            val newUri = uri.buildUpon()
                .clearQuery()
                .apply { filteredQueryKeys.forEach { appendQueryParameter(it, uri.getQueryParameter(it)) } }
                .build()

            urls[0] = newUri.toString()
            params.result = urls
        }
    }

    private val ChannelMemberList.groups by accessField<Map<String, MemberListRow>>("groups")

    private fun fixMemberListGroups() = tryPatch("Fix member list groups") {
        patcher.after<ChannelMemberList>("setGroups", List::class.java, Function1::class.java) {
            groupIndices.forEach { (idx, id) -> rows[idx] = groups[id] }
        }
    }

    // Fixes erratic AppBarLayout behavior by disabling 'smooth keyboard' animation
    private fun fixAppBar() = tryPatch("Fix erratic AppBarLayout behavior by disabling 'smooth keyboard' animation") {
        patcher.instead<SmoothKeyboardReactionHelper>("install", View::class.java) {}
    }

    private fun fixStickerCrash() = tryPatch("Fix sticker crash") {
        patcher.before<Apng>(
            Int::class.javaPrimitiveType!!,
            Int::class.javaPrimitiveType!!,
            Int::class.javaPrimitiveType!!,
            Int::class.javaPrimitiveType!!,
            IntArray::class.java,
            Int::class.javaPrimitiveType!!,
            Long::class.javaPrimitiveType!!,
        ) { param ->
            val durations = param.args[4] as IntArray
            durations.forEachIndexed { index, duration ->
                if (duration <= 10) durations[index] = 100
            }
        }
    }

    private fun fixHidingMutedThreads() = tryPatch("Fix hiding muted threads with mentions") {
        patcher.after<`WidgetChannelListModel$Companion$guildListBuilder$$inlined$forEach$lambda$1$1`>("invoke") { param ->
            val builder = this.`this$0`
            val threadId = this.`$textChannel`.id
            val mentionCount = builder.`$mentionCounts$inlined`[threadId] as? Int? ?: return@after

            if (mentionCount > 0) {
                builder.`$hiddenChannelsIds$inlined` -= threadId
                param.result = false
            }
        }
    }

    private fun fixHidingMutedChannels() = tryPatch("Fix hiding muted channels with mentions") {
        @Suppress("UNCHECKED_CAST")
        patcher.after<`WidgetChannelListModel$Companion$guildListBuilder$$inlined$forEach$lambda$1$2`>("invoke") { param ->
            val builder = `this$0`
            val mentionCountMap = builder.`$mentionCounts$inlined` as Map<Long, Int>
            val threadParentMap = builder.`$threadParentMap$inlined` as Map<Long, List<Channel>>
            val channelId = `$textChannelId`
            val childThreads = threadParentMap[channelId]
            val hasMentions = `$mentionCount` > 0
            val hasChildMentions = childThreads?.any { mentionCountMap.getOrDefault(it.id, 0) > 0 } == true

            if (hasMentions || hasChildMentions) {
                builder.`$hiddenChannelsIds$inlined` -= channelId
                param.result = false
            }
        }
    }

    private fun fixHidingMutedDMs() = tryPatch("Fix hiding muted DMs with mentions") {
        patcher.after<`WidgetGuildsListViewModel$createDirectMessageItems$1`>(
            "invoke",
            Channel::class.java,
        ) { (param, channel: Channel) ->
            val hasMentions = channel.id in this.`$mentionCounts`.keys

            if (hasMentions) {
                param.result = true
            }
        }
    }

    private fun fixPrivateThreads() = tryPatch("Fix private threads") {
        patcher.instead<ThreadDraftFormEntry>("getCanCreatePrivateThread") { true }
        patcher.after<WidgetChatListAdapterItemThreadDraftForm>("onConfigure", Int::class.javaPrimitiveType!!, ChatListEntry::class.java) {
            itemView.findViewById<TextView>("private_thread_toggle_badge").visibility = View.GONE
        }
    }

    private fun fixPrivateChannelScroll() = tryPatch("Fix private channel scroll") {
        var executed = false

        patcher.after<WidgetChannelsList>("configureUI", WidgetChannelListModel::class.java) { (_, model: WidgetChannelListModel) ->
            if (executed) return@after

            if (!model.isGuildSelected && model.items.size > 1) {
                val manager = WidgetChannelsList.`access$getBinding$p`(this).c.layoutManager as LinearLayoutManager
                if (manager.findFirstVisibleItemPosition() != 0) {
                    executed = true
                    manager.scrollToPosition(0)
                }
            }
        }
    }

    private fun fixVoiceCodec() = tryPatch("Fix VC codec") {
        patcher.before<ProtocolInfo>(
            String::class.java,
            Int::class.javaPrimitiveType!!,
            String::class.java,
        ) { (param, _: String, _: Int, mode: String) ->
            if (mode == "xsalsa20_poly1305") {
                param.args[2] = "xsalsa20_poly1305_lite_rtpsize"
            }
        }
    }

    private fun fixThreadsIcon() = tryPatch("Fix threads icon alignment in channel context menu") {
        fun adjustThreadIcon(rootView: View, textViewIdName: String, themed: Boolean) {
            val iconId = if (themed) {
                DrawableCompat.getThemedDrawableRes(rootView.context, R.b.ic_thread)
            } else {
                R.e.ic_thread
            }

            val icon = ContextCompat.getDrawable(rootView.context, iconId)!!.apply {
                val size = 24.dp
                setBounds(0, 0, size, size)
            }

            val textView = rootView.findViewById<TextView>(textViewIdName)
            textView.setCompoundDrawables(icon, null, null, null)
        }

        patcher.after<`WidgetChannelsListItemChannelActions$binding$2`>("invoke", View::class.java) { (_, view: View) ->
            adjustThreadIcon(
                rootView = view,
                textViewIdName = "text_action_thread_browser",
                themed = true,
            )
        }

        patcher.after<`WidgetChatListActions$binding$2`>("invoke", View::class.java) { (_, view: View) ->
            adjustThreadIcon(
                rootView = view,
                textViewIdName = "dialog_chat_actions_start_thread",
                themed = false,
            )
        }
    }

    private fun fixSlowmode() = tryPatch("Fix slowmode permissions") {
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
                param.result = Utils.appContext.resources.getString(R.h.channel_slowmode_desc_immune)
            }
        }
    }

    // Forces app links to always open in a separate window, except for custom tab. This addresses an issue where some links are opened internally on certain ROMs
    private fun fixExternalLinks() = tryPatch("Fixes app links not being handled by their respective app") {
        @Suppress("UnusedReceiverParameter")
        fun Activity.handleIntent(param: MethodHookParam) {
            val intent = param.args[0] as? Intent ?: return
            val pm = Utils.appContext.packageManager
            val handlers = pm.queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY)
            if (handlers.isEmpty()) intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }

        patcher.before<Activity>("startActivity", Intent::class.java, callback = Activity::handleIntent)
        patcher.before<Activity>(
            "startActivityForResult",
            Intent::class.java, Int::class.javaPrimitiveType!!, Bundle::class.java,
            callback = Activity::handleIntent
        )
    }

    private fun tryPatch(label: String, block: () -> Unit) {
        try {
            block()
        } catch (e: Throwable) {
            Main.logger.error("Failed to apply patch: \"$label\"", e)
        }
    }
}
