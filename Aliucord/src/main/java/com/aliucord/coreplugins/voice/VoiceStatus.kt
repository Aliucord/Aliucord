package com.aliucord.coreplugins.voice

import android.annotation.SuppressLint
import android.text.InputFilter
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.aliucord.Http
import com.aliucord.Logger
import com.aliucord.Utils
import com.aliucord.api.GatewayAPI
import com.aliucord.api.PatcherAPI
import com.aliucord.coreplugins.voice.VoiceChatTimers.requestChannelInfo
import com.aliucord.coreplugins.voice.model.VoiceChannelStatus
import com.aliucord.fragments.InputDialog
import com.aliucord.patcher.PreHook
import com.aliucord.patcher.after
import com.aliucord.patcher.component1
import com.aliucord.patcher.component2
import com.aliucord.patcher.component3
import com.aliucord.utils.DimenUtils.dp
import com.aliucord.utils.ViewUtils.addTo
import com.aliucord.wrappers.ChannelWrapper.Companion.guildId
import com.aliucord.wrappers.ChannelWrapper.Companion.id
import com.aliucord.wrappers.ChannelWrapper.Companion.name
import com.aliucord.wrappers.ChannelWrapper.Companion.type
import com.discord.api.channel.Channel
import com.discord.api.permission.Permission
import com.discord.app.AppFragment
import com.discord.models.domain.ModelPayload
import com.discord.stores.StoreGuildSelected
import com.discord.stores.StoreStream
import com.discord.utilities.color.ColorCompat
import com.discord.utilities.permissions.PermissionUtils
import com.discord.utilities.textprocessing.DiscordParser
import com.discord.utilities.textprocessing.MessagePreprocessor
import com.discord.utilities.textprocessing.MessageRenderContext
import com.discord.widgets.channels.list.WidgetChannelsListAdapter
import com.discord.widgets.channels.list.items.ChannelListItem
import com.discord.widgets.channels.list.items.ChannelListItemVoiceChannel
import com.discord.widgets.voice.fullscreen.WidgetCallFullscreen
import com.discord.widgets.voice.fullscreen.WidgetCallFullscreenViewModel
import com.discord.widgets.voice.fullscreen.WidgetCallPreviewFullscreen
import com.discord.widgets.voice.fullscreen.WidgetCallPreviewFullscreenViewModel
import com.discord.widgets.voice.fullscreen.`WidgetCallPreviewFullscreenViewModel$Companion$observeStoreState$1$1`
import com.discord.widgets.voice.model.CallModel
import com.discord.widgets.voice.settings.WidgetVoiceChannelSettings
import com.discord.widgets.voice.sheet.WidgetVoiceSettingsBottomSheet
import com.facebook.drawee.span.DraweeSpanStringBuilder
import com.lytefast.flexinput.R
import java.util.Collections
import java.util.WeakHashMap

internal object VoiceStatus {
    private val logger = Logger("VoiceChatFix")

    // Limit for /channels/{id}/voice-status
    private const val MAX_LENGTH = 500
    private const val SET_VOICE_CHANNEL_STATUS = 1L shl 48
    private val statuses = Collections.synchronizedMap(HashMap<Long, String>())
    private val topicViews = Collections.synchronizedMap(WeakHashMap<TextView, Long>())
    private val previewWidgets = Collections.synchronizedMap(WeakHashMap<WidgetCallPreviewFullscreen, Long>())
    private val callWidgets = Collections.synchronizedMap(WeakHashMap<WidgetCallFullscreen, Long>())
    private val builders = WeakHashMap<TextView, DraweeSpanStringBuilder>()
    private val settingsRowId = View.generateViewId()
    private val callSheetRowId = View.generateViewId()

    fun register(patcher: PatcherAPI) {
        GatewayAPI.onEvent<VoiceChannelStatus>("VOICE_CHANNEL_STATUS_UPDATE") { update ->
            logger.debug("GatewayEvent[VOICE_CHANNEL_STATUS_UPDATE]: $update")
            update.id?.let { track(it, update.status) }
        }

        patchChannelList(patcher)
        patchPreviewFallback(patcher)
        patchCallPreview(patcher)
        patchCallScreen(patcher)
        patchChannelSettings(patcher)
        seedOnGuildOpen(patcher)
    }

    private fun patchPreviewFallback(patcher: PatcherAPI) = runCatching {
        // call(MeUser, Channel selectedTextChannel, Boolean, Integer, Map, CallModel, Channel): StoreState
        // this sucks sooo much
        val call = `WidgetCallPreviewFullscreenViewModel$Companion$observeStoreState$1$1`::class.java
            .declaredMethods
            .first { it.name == "call" && it.parameterTypes.size == 7 && !it.isSynthetic }

        patcher.patch(call, PreHook { param ->
            if (param.args[1] == null) param.args[1] = (param.args[5] as? CallModel)?.channel
        })
    }.onFailure {
        logger.error("Failed to patch call preview text-channel fallback", it)
    }

    fun track(channelId: Long, status: String?) {
        statuses[channelId] = status.orEmpty()
        updateChannelList(channelId, status.orEmpty())
        updatePreviewSubtitle(channelId, status.orEmpty())
        updateCallSubtitle(channelId, status.orEmpty())
    }

    // Opens a dialog that sets a channel's voice status
    // Guild voice channels only and user needs SET_VOICE_CHANNEL_STATUS permission
    // Reached by "Set Voice Status" row in the channel's settings.
    fun showDialog(channelId: Long = StoreStream.getVoiceChannelSelected().selectedVoiceChannelId) {
        if (channelId <= 0L) return

        val channel = StoreStream.getChannels().getChannel(channelId)

        val guildId = channel?.guildId
        if (guildId == null || guildId == 0L) {
            Utils.showToast("Voice status is only available in server voice channels")
            return
        }

        InputDialog()
            .setTitle("What are we chatting about?")
            .setDescription("Let others know what you're up to in the voice channel!")
            .setPlaceholderText("Status for ${channel.name}").apply {
                isCancelable = false

                setOnDialogShownListener {
                    inputLayout.apply {
                        isCounterEnabled = true
                        counterMaxLength = MAX_LENGTH
                        editText?.filters = arrayOf(InputFilter.LengthFilter(MAX_LENGTH))
                        editText?.setText(statuses[channelId].orEmpty())
                    }
                }

                setOnOkListener {
                    val status = input.trim()
                    dismiss()

                    Utils.threadPool.execute {
                        runCatching {
                            Http.Request.newDiscordRequest("/channels/$channelId/voice-status", "PUT")
                                .executeWithJson(mapOf("status" to status))
                                .assertOk()
                        }.onSuccess {
                            track(channelId, status)
                            Utils.mainThread.post {
                                Utils.showToast(if (status.isEmpty()) "Voice status cleared" else "Voice status set")
                            }
                        }.onFailure {
                            logger.error("Failed to set voice status for channel $channelId", it)
                            Utils.mainThread.post {
                                Utils.showToast("Failed to set voice status (missing permission?)")
                            }
                        }
                    }
                }
            }.show(Utils.appActivity.supportFragmentManager, "SetVoiceStatus")
    }

    // Shows the channel voice status under its name
    // Updates are tracked via VOICE_CHANNEL_STATUS_UPDATE
    private fun patchChannelList(patcher: PatcherAPI) {
        val topicId = Utils.getResId("channels_item_voice_channel_event_topic", "id")

        patcher.after<WidgetChannelsListAdapter.ItemChannelVoice>(
            "onConfigure",
            Int::class.javaPrimitiveType!!,
            ChannelListItem::class.java,
        ) { (_, _: Int, data: ChannelListItem) ->
            val channel = (data as? ChannelListItemVoiceChannel)?.channel ?: return@after
            val topic = itemView.findViewById<TextView>(topicId) ?: return@after

            topicViews[topic] = channel.id

            val status = statuses[channel.id]?.takeIf { it.isNotEmpty() } ?: return@after

            @SuppressLint("UseKtx")
            if (topic.visibility == View.VISIBLE && !topic.text.isNullOrEmpty()) {
                logger.debug("Channel list status: event topic owns the slot for channel ${channel.id}, skipping")
                return@after
            }

            logger.debug("Channel list status: showing '$status' for channel ${channel.id}")
            topic.setStatus(status)
            topic.visibility = View.VISIBLE
        }
    }

    // Shows the voice status under the channel name in the pre-join call preview screen
    private fun patchCallPreview(patcher: PatcherAPI) {
        patcher.after<WidgetCallPreviewFullscreen>(
            "configureUI",
            WidgetCallPreviewFullscreenViewModel.ViewState::class.java,
        ) { (_, viewState: WidgetCallPreviewFullscreenViewModel.ViewState?) ->
            val channelId = viewState?.voiceChannel?.id ?: return@after

            previewWidgets[this] = channelId
            setStatusSubtitle(statuses[channelId].orEmpty())
        }
    }

    // Shows the voice status under the channel name on the fullscreen call screen (toolbar subtitle)
    // Also adds a "Change Voice Status" entry to its overflow menu
    private fun patchCallScreen(patcher: PatcherAPI) {
        patcher.after<WidgetCallFullscreen>(
            "configureActionBar",
            WidgetCallFullscreenViewModel.ViewState.Valid::class.java,
        ) { (_, viewState: WidgetCallFullscreenViewModel.ViewState.Valid) ->
            val channel = viewState.callModel.channel
            if (channel.guildId == 0L || channel.type != Channel.GUILD_VOICE) return@after

            callWidgets[this] = channel.id
            setStatusSubtitle(statuses[channel.id].orEmpty())
        }

        // "Change Voice Status" row in the call screen's three-dots bottom drawer
        // (the sheet with Invite / Voice Settings / Events)
        patcher.after<WidgetVoiceSettingsBottomSheet>(
            "configureUI",
            WidgetVoiceSettingsBottomSheet.ViewState::class.java,
        ) { (_, viewState: WidgetVoiceSettingsBottomSheet.ViewState) ->
            val channel = viewState.channel
            val root = view ?: return@after
            val existing = root.findViewById<TextView>(callSheetRowId)

            if (channel.guildId == 0L || channel.type != Channel.GUILD_VOICE) {
                existing?.visibility = View.GONE
                return@after
            }

            // Since base doesn't have the change voice status permission, we need to
            // manually calculate the fallback permissions
            val allowed: Boolean = listOf(
                SET_VOICE_CHANNEL_STATUS,
                Permission.MANAGE_CHANNELS,
                Permission.MODERATOR_PERMISSIONS,
                Permission.ADMINISTRATOR,
            ).map { permission ->
                PermissionUtils.can(
                    permission,
                    StoreStream.getPermissions().permissionsByChannel[channel.id]
                )
            }.any { it }

            if (existing != null) {
                existing.visibility = if (allowed) View.VISIBLE else View.GONE
                return@after
            }

            val container = root.findViewById(Utils.getResId("voice_settings_sheet_container", "id"))
                ?: root as? ViewGroup
                ?: return@after

            val parent = container.getChildAt(0) as? ViewGroup ?: return@after

            TextView(root.context, null, 0, R.i.UiKit_ListItem).addTo(parent) {
                id = callSheetRowId
                text = "Change Voice Status"
                visibility = if (allowed) View.VISIBLE else View.GONE
                compoundDrawablePadding = 16.dp
                ContextCompat.getDrawable(root.context, R.e.ic_edit_24dp)?.run {
                    mutate()
                    setTint(ColorCompat.getThemedColor(root.context, R.b.colorInteractiveNormal))
                    setCompoundDrawablesRelativeWithIntrinsicBounds(this, null, null, null)
                }
                setOnClickListener {
                    dismiss()
                    showDialog(channel.id)
                }
            }
        }
    }

    // Live-repaints the subtitle of any open call screen for this channel
    private fun updateCallSubtitle(channelId: Long, status: String) {
        Utils.mainThread.post {
            runCatching {
                synchronized(callWidgets) {
                    callWidgets.filterValues { it == channelId }.keys.toList()
                }.forEach { widget ->
                    widget.setStatusSubtitle(status)
                }
            }.onFailure {
                logger.error("Call subtitle: live update failed for channel $channelId", it)
            }
        }
    }

    // Live-repaints the subtitle of any open preview screen for this channel
    private fun updatePreviewSubtitle(channelId: Long, status: String) {
        Utils.mainThread.post {
            runCatching {
                synchronized(previewWidgets) {
                    previewWidgets.filterValues { it == channelId }.keys.toList()
                }.forEach { widget ->
                    widget.setStatusSubtitle(status)
                }
            }.onFailure {
                logger.error("Preview subtitle: live update failed for channel $channelId", it)
            }
        }
    }

    // Live-repaints the event-topic slot of every currently bound row for this channel
    private fun updateChannelList(channelId: Long, status: String) {
        Utils.mainThread.post {
            runCatching {
                val views = synchronized(topicViews) {
                    topicViews.filterValues { it == channelId }.keys.toList()
                }

                //logger.debug("Channel list status: live update for channel $channelId on ${views.size} bound row(s)")
                views.forEach { topic ->
                    topic.setStatus(status)
                    topic.visibility = if (status.isNotEmpty()) View.VISIBLE else View.GONE
                }
            }.onFailure {
                logger.error("Channel list status: live update failed for channel $channelId", it)
            }
        }
    }

    // Adds a "Set Voice Status" row to the voice channel settings screen
    // (reached by long-pressing the channel name), under the topic input
    private fun patchChannelSettings(patcher: PatcherAPI) {
        patcher.after<WidgetVoiceChannelSettings>(
            "configureUI",
            WidgetVoiceChannelSettings.Model::class.java,
        ) { (_, model: WidgetVoiceChannelSettings.Model?) ->
            val channel = model?.channel ?: return@after
            // Stages have topics instead of a voice status
            if (channel.type != Channel.GUILD_VOICE) return@after
            val root = view ?: return@after
            val topic = root.findViewById<View>(Utils.getResId("channel_settings_edit_topic", "id")) ?: return@after
            val parent = topic.parent as? ViewGroup ?: return@after
            if (parent.findViewById<View>(settingsRowId) != null) return@after

            TextView(root.context, null, 0, R.i.UiKit_Settings_Item).addTo(parent, parent.indexOfChild(topic) + 1) {
                id = settingsRowId
                text = "Set Voice Status"
                setOnClickListener { showDialog(channel.id) }
            }
        }
    }

    // Pre-existing statuses (before we were started listening) are never shown
    private fun seedOnGuildOpen(patcher: PatcherAPI) {
        patcher.after<StoreGuildSelected>(
            "handleGuildSelected",
            Long::class.javaPrimitiveType!!,
        ) { (_, guildId: Long) ->
            if (guildId > 0L) requestChannelInfo(guildId)
        }

        patcher.after<StoreGuildSelected>(
            "handleConnectionOpen",
            ModelPayload::class.java,
        ) {
            val guildId = `getSelectedGuildIdInternal$app_productionGoogleRelease`()
            if (guildId > 0L) requestChannelInfo(guildId)
        }
    }

    // TODO: Maybe use SimpleDraweeSpanTextView and add an emoji picker in the future
    private fun TextView.setStatus(status: String) {
        // `b` uses ON_HOLDER_DETACH
        builders.remove(this)?.b(this)

        if (status.trim().isEmpty()) {
            text = null
            return
        }

        val meId = StoreStream.getUsers().me.id

        val builder = DiscordParser.parseChannelMessage(
            context,
            status,
            MessageRenderContext(context, meId, true),
            MessagePreprocessor(meId, Collections.emptyList(), null, false, null),
            DiscordParser.ParserOptions.DEFAULT,
            false,
        )

        setText(builder, TextView.BufferType.SPANNABLE)

        builder.a(this)
        builders[this] = builder
    }

    // The toolbar subtitle is a plain TextView inside ToolbarTitleLayout
    private fun AppFragment.setStatusSubtitle(status: String) {
        view?.findViewById<TextView>(Utils.getResId("toolbar_title_subtext", "id"))?.apply {
            setStatus(status)
            visibility = if (status.trim().isEmpty()) View.GONE else View.VISIBLE
        }
    }
}
