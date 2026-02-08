package com.aliucord.coreplugins

import android.content.Context
import android.net.Uri
import android.os.Build
import android.view.View
import android.view.WindowInsetsAnimation
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import com.aliucord.Main
import com.aliucord.Utils
import com.aliucord.Utils.getResId
import com.aliucord.entities.CorePlugin
import com.aliucord.patcher.*
import com.aliucord.utils.ReflectUtils
import com.aliucord.wrappers.ChannelWrapper.Companion.id
import com.aliucord.wrappers.embeds.MessageEmbedWrapper
import com.discord.api.channel.Channel
import com.discord.api.message.embed.EmbedField
import com.discord.models.domain.Model
import com.discord.models.domain.ModelUserSettings
import com.discord.models.domain.emoji.ModelEmojiCustom
import com.discord.models.domain.emoji.ModelEmojiUnicode
import com.discord.rtcconnection.socket.io.Payloads.Protocol.ProtocolInfo
import com.discord.utilities.embed.EmbedResourceUtils
import com.discord.utilities.guildautomod.AutoModUtils
import com.discord.utilities.lazy.memberlist.ChannelMemberList
import com.discord.utilities.lazy.memberlist.MemberListRow
import com.discord.widgets.channels.list.*
import com.discord.widgets.chat.input.SmoothKeyboardReactionHelper
import com.discord.widgets.chat.list.adapter.WidgetChatListAdapterItemAutoModSystemMessageEmbed
import com.discord.widgets.chat.list.adapter.WidgetChatListAdapterItemThreadDraftForm
import com.discord.widgets.chat.list.entries.*
import com.linecorp.apng.decoder.Apng

// Contains various small fixes for Discord
internal class CoreFixes : CorePlugin(Manifest("CoreFixes")) {
    override val isHidden = true
    override val isRequired = true

    @Suppress("UNCHECKED_CAST")
    override fun start(context: Context) {
        // Fix 2025-04-03 gateway change that ported visual refresh theme names over the legacy user settings
        // Theme entries like "darker" and "midnight" are unsupported
        patcher.after<ModelUserSettings>("assignField", Model.JsonReader::class.java) {
            if (
                theme == null ||
                theme == ModelUserSettings.THEME_DARK ||
                theme == ModelUserSettings.THEME_LIGHT ||
                theme == ModelUserSettings.THEME_PURE_EVIL
            ) return@after

            try {
                ReflectUtils.setField(this, "theme", "dark")
            } catch (e: Exception) {
                Main.logger.error("Failed to fix ModelUserSettings theme", e)
            }
        }

        // Patch to repair built-in emotes is needed because installer doesn't recompile resources,
        // so they stay in package com.discord instead of apk package name
        Patcher.addPatch(ModelEmojiUnicode::class.java, "getImageUri", arrayOf(String::class.java, Context::class.java),
            InsteadHook { param -> "res:///${getResId("emoji_${param.args[0]}", "raw")}" }
        )

        // Patch to fix crash when displaying newer AutoMod embed types like "Quarantined a member at username update"
        Patcher.addPatch(WidgetChatListAdapterItemAutoModSystemMessageEmbed::class.java,
            "onConfigure",
            arrayOf(Int::class.javaPrimitiveType!!, ChatListEntry::class.java),
            PreHook { (_, _: Any, autoModEntry: AutoModSystemMessageEmbedEntry) ->
                // If the channel_id embed field is missing, then just add one set to 0, it'll be displayed as null
                if (AutoModUtils.INSTANCE.getEmbedFieldValue(autoModEntry.embed, "channel_id").isNullOrEmpty()) {
                    val fields = MessageEmbedWrapper(autoModEntry.embed).rawFields as ArrayList<EmbedField>

                    val newField = ReflectUtils.allocateInstance(EmbedField::class.java)
                    ReflectUtils.setField(newField!!, "name", "channel_id")
                    ReflectUtils.setField(newField, "value", "0")

                    fields += newField
                }
            }
        )

        // not sure why this happens, reported on Android 15 Beta 4
        // java.lang.IllegalArgumentException: Animators cannot have negative duration: -1
        //   at android.view.ViewPropertyAnimator.setDuration(ViewPropertyAnimator.java:266)
        //   at com.discord.widgets.chat.input.SmoothKeyboardReactionHelper$Callback.onStart(SmoothKeyboardReactionHelper.kt:5)
        //   at android.view.View.dispatchWindowInsetsAnimationStart(View.java:12671)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            try {
                Patcher.addPatch(
                    SmoothKeyboardReactionHelper.Callback::class.java.getDeclaredMethod("onStart",
                        WindowInsetsAnimation::class.java,
                        WindowInsetsAnimation.Bounds::class.java),
                    PreHook { (param, animation: WindowInsetsAnimation) ->
                        if (animation.durationMillis < 0) param.result = param.args[1]
                    }
                )
            } catch (e: Throwable) {
                Main.logger.error("Couldn't patch possible Android 15 (?) crash", e)
            }
        }

        // Support webp emojis by forcing every emoji to be webp
        patcher.patch(
            ModelEmojiCustom::class.java,
            "getImageUri",
            arrayOf(Long::class.javaPrimitiveType!!, Boolean::class.javaPrimitiveType!!, Int::class.javaPrimitiveType!!),
            InsteadHook { param ->
                "https://cdn.discordapp.com/emojis/${param.args[0]}.webp?size=${param.args[2]}&animated=${param.args[1]}"
            }
        )

        // Fix GIF previews
        patcher.after<EmbedResourceUtils>(
            "getPreviewUrls",
            String::class.java, Int::class.java, Int::class.java, Boolean::class.java,
        ) { (params, _: String, _: Int, _: Int, animated: Boolean) ->
            if (!animated) return@after

            @Suppress("UNCHECKED_CAST")
            val urls = (params.result as List<String>).toMutableList()

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

        // Fixes member list groups
        val memberListGroupsField = ChannelMemberList::class.java.getDeclaredField("groups").apply {
            isAccessible = true
        }
        patcher.after<ChannelMemberList>("setGroups", List::class.java, Function1::class.java) {
            val groupsMap = memberListGroupsField[this] as Map<String, MemberListRow>
            groupIndices.forEach { (idx, id) -> rows[idx] = groupsMap[id] }
        }

        // Fixes erratic AppBarLayout behavior by disabling 'smooth keyboard' animation
        patcher.instead<SmoothKeyboardReactionHelper>("install", View::class.java) {}

        // Sticker crash fix
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

        // Fix hiding muted threads
        patcher.after<`WidgetChannelListModel$Companion$guildListBuilder$$inlined$forEach$lambda$1$1`>("invoke") { param ->
            val builder = this.`this$0`
            val threadId = this.`$textChannel`.id
            val mentionCount = builder.`$mentionCounts$inlined`[threadId] as Int? ?: return@after
            if (mentionCount > 0) {
                builder.`$hiddenChannelsIds$inlined`.remove(threadId)
                param.result = false
            }
        }

        // Fix hiding muted channels
        @Suppress("UNCHECKED_CAST")
        patcher.after<`WidgetChannelListModel$Companion$guildListBuilder$$inlined$forEach$lambda$1$2`>("invoke") { param ->
            val builder = this.`this$0`
            val channelId = this.`$textChannelId`
            val mentionCountMap = builder.`$mentionCounts$inlined` as Map<Long, Int>
            val threadParentMap = builder.`$threadParentMap$inlined` as Map<Long, List<Channel>>
            val childThreads = threadParentMap[channelId]

            val hasMentions = this.`$mentionCount` > 0
            val hasChildMentions = childThreads?.any { mentionCountMap.getOrDefault(it.id, 0) > 0 } == true
            if (hasMentions || hasChildMentions) {
                builder.`$hiddenChannelsIds$inlined`.remove(this.`$textChannelId`)
                param.result = false
            }
        }

        // Fix private threads
        patcher.instead<ThreadDraftFormEntry>("getCanCreatePrivateThread") { true }
        patcher.after<WidgetChatListAdapterItemThreadDraftForm>("onConfigure", Int::class.javaPrimitiveType!!, ChatListEntry::class.java) {
            itemView.findViewById<TextView>(Utils.getResId("private_thread_toggle_badge", "id")).visibility = View.GONE
        }

        // Fixes scrolling in private channels
        patcher.after<WidgetChannelsList>("configureUI", WidgetChannelListModel::class.java) { (_, model: WidgetChannelListModel) ->
            if (!model.isGuildSelected && model.items.size > 1) {
                val manager = WidgetChannelsList.`access$getBinding$p`(this).c.layoutManager!! as LinearLayoutManager
                if (manager.findFirstVisibleItemPosition() != 0) {
                    manager.scrollToPosition(0)
                    patcher.unpatchAll()
                }
            }
        }

        // Fixes VC not working properly.
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
}
