/*
 * This file is part of Aliucord, an Android Discord client mod.
 * Copyright (c) 2021 Juby210 & Vendicated
 * Licensed under the Open Software License version 3.0
 */

package com.aliucord.coreplugins.plugindownloader

import android.content.Context
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.aliucord.*
import com.aliucord.entities.CorePlugin
import com.aliucord.patcher.*
import com.aliucord.utils.ReflectUtils
import com.aliucord.utils.ViewUtils.findViewById
import com.aliucord.utils.accessGetter
import com.aliucord.wrappers.messages.AttachmentWrapper.Companion.filename
import com.aliucord.wrappers.messages.AttachmentWrapper.Companion.url
import com.discord.api.message.attachment.MessageAttachment
import com.discord.app.AppBottomSheet
import com.discord.databinding.WidgetUrlActionsBinding
import com.discord.models.member.GuildMember
import com.discord.models.message.Message
import com.discord.stores.StoreStream
import com.discord.utilities.color.ColorCompat
import com.discord.utilities.textprocessing.MessageRenderContext
import com.discord.widgets.chat.WidgetUrlActions
import com.discord.widgets.chat.list.actions.WidgetChatListActions
import com.discord.widgets.chat.list.adapter.WidgetChatListAdapterItemMessage
import com.discord.widgets.chat.list.entries.MessageEntry
import com.lytefast.flexinput.R
import java.util.regex.Pattern

private val viewId = View.generateViewId()
private val repoPattern = Pattern.compile(
    """https?://github\.com/([A-Za-z0-9\-_.]+)/([A-Za-z0-9\-_.]+)""")
private val zipPattern = Pattern.compile(
    """https?://(?:github|raw\.githubusercontent)\.com/([A-Za-z0-9\-_.]+)/([A-Za-z0-9\-_.]+)/(?:raw|blob)?/?(\w+)/(\w+).zip""")

private val WidgetUrlActions.binding by accessGetter<WidgetUrlActionsBinding>("getBinding")

internal class PluginDownloader : CorePlugin(Manifest("PluginDownloader")) {
    override val isRequired = true // TODO: make this optional once PluginRepo is core

    init {
        manifest.description = "Utility for installing plugins directly from the Aliucord server's plugins channels"

        PluginFile("PluginDownloader").takeIf { it.exists() }?.let {
            if (it.delete())
                Utils.showToast("PluginDownloader has been merged into Aliucord, so I deleted the plugin for you.", true)
            else
                Utils.showToast("PluginDownloader has been merged into Aliucord. Please delete the plugin.", true)
        }
    }

    override fun start(context: Context) {
        // Add items to message context menu
        patcher.after<WidgetChatListActions>(
            "configureUI",
            WidgetChatListActions.Model::class.java,
        ) { (_, model: WidgetChatListActions.Model) ->
            val message = model.message
            val layout = (this.requireView() as ViewGroup)
                .findViewById<LinearLayout>("dialog_chat_actions_container")

            if (layout.findViewById<View>(viewId) != null) return@after
            if (message.content.isNullOrEmpty()) return@after

            val member = StoreStream.getGuilds().getMember(Constants.ALIUCORD_GUILD_ID, message.author.id)
            if (!shouldScanForPlugins(message, member)) return@after

            val entries = getEntriesForPluginsListing(
                messageContent = message.content,
                messageAttachments = message.attachments,
                sheet = this,
            )

            val replyView = layout.findViewById<View?>("dialog_chat_actions_edit") ?: return@after
            val replyViewIdx = layout.indexOfChild(replyView)
            for ((entryIdx, entry) in entries.withIndex()) {
                layout.addView(entry, replyViewIdx + entryIdx)
            }
        }

        // Replace link click handlers so that message data can be smuggled through intent before opening link context menu
        patcher.after<WidgetChatListAdapterItemMessage>(
            "getMessageRenderContext",
            Context::class.java,
            MessageEntry::class.java,
            Function1::class.java,
        ) { (param, _: Context, messageEntry: MessageEntry) ->
            if (!shouldScanForPlugins(messageEntry.message, messageEntry.author)) return@after

            val renderContext = param.result as MessageRenderContext

            val newLongClickHandler: Function1<String, Unit> = { url ->
                val urlActions = WidgetUrlActions().apply {
                    arguments = Bundle().apply {
                        putString("INTENT_URL", url)
                        putString("INTENT_PLUGIN_DOWNLOADER_CONTENT", messageEntry.message.content)
                    }
                }
                urlActions.show(
                    /* p0 = */ this.adapter.fragmentManager,
                    /* p1 = */ WidgetUrlActions::class.java.getName(),
                )
            }
            // For future reference: replacing onClickUrl callback for short presses is possible here too
            ReflectUtils.setFinalField(renderContext, "onLongPressUrl", newLongClickHandler)
        }

        // Add items to links context menu
        patcher.after<WidgetUrlActions>(
            "onViewCreated",
            View::class.java,
            Bundle::class.java,
        ) {
            val content = this.arguments?.getString("INTENT_PLUGIN_DOWNLOADER_CONTENT") ?: return@after
            val layout = this.binding.root as ViewGroup

            val entries = getEntriesForPluginsListing(
                messageContent = content,
                messageAttachments = null,
                sheet = this,
            )

            val copyView = this.binding.b
            val copyViewIdx = layout.indexOfChild(copyView)
            for ((entryIdx, entry) in entries.withIndex()) {
                layout.addView(entry, copyViewIdx + entryIdx)
            }
        }
    }

    override fun stop(context: Context) {}

    /**
     * Checks whether a message should be scanned for plugin links to be added to context menus.
     * This implies that the message comes from a **trusted** source.
     */
    private fun shouldScanForPlugins(message: Message, authorMember: GuildMember?): Boolean {
        return when (message.channelId) {
            Constants.PLUGIN_LINKS_CHANNEL_ID,
            Constants.PLUGIN_LINKS_UPDATES_CHANNEL_ID,
            Constants.PLUGIN_DEVELOPMENT_CHANNEL_ID -> true

            Constants.SUPPORT_CHANNEL_ID,
            Constants.PLUGIN_SUPPORT_CHANNEL_ID,
            Constants.BOT_SPAM_CHANNEL_ID -> {
                val isTrusted = authorMember?.roles
                    ?.any { it == Constants.SUPPORT_HELPER_ROLE_ID || it == Constants.PLUGIN_DEVELOPER_ROLE_ID }
                    ?: false

                return isTrusted
            }

            else -> false
        }
    }

    /**
     * Scans message content & attachments and generates view entries to be added to a context menu.
     */
    private fun getEntriesForPluginsListing(
        messageContent: String,
        messageAttachments: List<MessageAttachment>?,
        sheet: AppBottomSheet,
    ): List<View> {
        val entries = mutableListOf<View>()

        repoPattern.matcher(messageContent).takeIf { it.find() }?.run {
            val author = group(1)!!
            val repo = group(2)!!

            entries += makeContextMenuEntry(
                ctx = sheet.requireContext(),
                text = "View Available Plugins",
                onClick = {
                    Utils.openPageWithProxy(it.context, PluginRepoModal(author, repo))
                    sheet.dismiss()
                },
            )
        }

        zipPattern.matcher(messageContent).run {
            while (find()) {
                val author = group(1)!!
                val repo = group(2)!!
                val commit = group(3)!!
                val name = group(4)!!

                // Don't accidentally install core as a plugin
                if (name == "Aliucord") continue

                val plugin = PluginFile(name)
                entries += makeContextMenuEntry(
                    ctx = sheet.requireContext(),
                    text = "${if (plugin.isInstalled) "Reinstall" else "Install"} $name",
                    onClick = {
                        plugin.install("https://cdn.jsdelivr.net/gh/$author/$repo@$commit/$name.zip")
                        sheet.dismiss()
                    },
                )
            }
        }

        for (attachment in messageAttachments ?: emptyList()) {
            if (attachment.filename.run { !endsWith(".zip") || equals("Aliucord.zip") }) continue

            val name = attachment.filename.removeSuffix(".zip")
            val isInstalled = PluginManager.plugins.containsKey(name)

            entries += makeContextMenuEntry(
                ctx = sheet.requireContext(),
                text = "${if (isInstalled) "Reinstall" else "Install"} $name",
                onClick = {
                    PluginFile(name).install(
                        url = attachment.url,
                        callback = sheet::dismiss,
                    )
                },
            )
        }

        return entries
    }

    /**
     * Makes a generic context menu entry for plugin downloader items with the download icon.
     */
    fun makeContextMenuEntry(ctx: Context, text: String, onClick: View.OnClickListener): View {
        return TextView(ctx, null, 0, R.i.UiKit_Settings_Item_Icon).apply {
            id = viewId
            setText(text)
            setOnClickListener(onClick)
            ContextCompat.getDrawable(ctx, R.e.ic_file_download_white_24dp)?.run {
                mutate()
                setTint(ColorCompat.getThemedColor(ctx, R.b.colorInteractiveNormal))
                setCompoundDrawablesRelativeWithIntrinsicBounds(this, null, null, null)
            }
        }
    }
}
