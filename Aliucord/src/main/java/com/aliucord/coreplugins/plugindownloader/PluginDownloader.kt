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
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import com.aliucord.*
import com.aliucord.Constants.*
import com.aliucord.entities.CorePlugin
import com.aliucord.patcher.*
import com.aliucord.utils.accessGetter
import com.aliucord.wrappers.messages.AttachmentWrapper.Companion.filename
import com.aliucord.wrappers.messages.AttachmentWrapper.Companion.url
import com.discord.app.AppBottomSheet
import com.discord.databinding.WidgetUrlActionsBinding
import com.discord.models.message.Message
import com.discord.stores.StoreStream
import com.discord.utilities.color.ColorCompat
import com.discord.utilities.viewbinding.FragmentViewBindingDelegate
import com.discord.widgets.chat.list.actions.WidgetChatListActions
import com.discord.widgets.chat.list.adapter.WidgetChatListAdapterItemMessage
import com.discord.widgets.chat.list.adapter.`WidgetChatListAdapterItemMessage$getMessageRenderContext$2`
import com.discord.widgets.chat.list.adapter.WidgetChatListAdapterEventsHandler
import com.discord.widgets.chat.list.entries.ChatListEntry
import com.discord.widgets.chat.list.entries.MessageEntry
import com.discord.widgets.chat.WidgetUrlActions
import com.lytefast.flexinput.R
import java.util.WeakHashMap

internal val logger = Logger("PluginDownloader")

private val viewId = View.generateViewId()
private val urlViewId = View.generateViewId()
private val repoPattern = Regex("https?://github\\.com/([A-Za-z0-9\\-_.]+)/([A-Za-z0-9\\-_.]+)")
private val zipPattern =
    Regex("https?://(?:github|raw\\.githubusercontent)\\.com/([A-Za-z0-9\\-_.]+)/([A-Za-z0-9\\-_.]+)/(?:raw|blob)?/?(\\w+)/(\\w+).zip")
private val mUrlSource = WeakHashMap<WidgetChatListAdapterItemMessage, Message?>()
private val mUrlSource2 = WeakHashMap<WidgetUrlActions, Message?>()

internal class PluginDownloader : CorePlugin(Manifest("PluginDownloader")) {
    override val isRequired = true

    init {
        manifest.description = "Utility for installing plugins directly from the Aliucord server's plugins channels"

        PluginFile("PluginDownloader").takeIf { it.exists() }?.let {
            if (it.delete())
                Utils.showToast("PluginDownloader has been merged into Aliucord, so I deleted the plugin for you.", true)
            else
                Utils.showToast("PluginDownloader has been merged into Aliucord. Please delete the plugin.", true)
        }
    }

    val WidgetUrlActions.binding by accessGetter<WidgetUrlActionsBinding>("getBinding")

    //allow passing URL's source message for context
    fun sourcedLaunch(fragmentManager: FragmentManager, str: String, source: Message) {
        val widgetUrlActions = WidgetUrlActions()
        mUrlSource2[widgetUrlActions] = source
        val bundle = Bundle()
        bundle.putString("INTENT_URL", str)
        widgetUrlActions.setArguments(bundle)
        widgetUrlActions.show(fragmentManager, WidgetUrlActions::class.java.getName())
    }
    fun WidgetChatListAdapterEventsHandler.onSourcedUrlLongClicked(str: String, source: Message) {
        sourcedLaunch(WidgetChatListAdapterEventsHandler.`access$getFragmentManager$p`(this), str, source)
    }

    override fun start(context: Context) {
        patcher.patch(
            WidgetChatListActions::class.java.getDeclaredMethod("configureUI", WidgetChatListActions.Model::class.java),
            Hook { (param, model: WidgetChatListActions.Model) ->
                val actions = param.thisObject as WidgetChatListActions
                val msg = model.message

                addPluginDownloadOptions(msg, actions)
            }
        )

        //also for link context menu
        patcher.patch(
            WidgetChatListAdapterItemMessage::class.java.getDeclaredMethod("onConfigure", Int::class.java, ChatListEntry::class.java),
            Hook { (param, _: Int, chatListEntry: ChatListEntry) ->
                val messageEntry = chatListEntry as MessageEntry
                val message = messageEntry.message as Message
                mUrlSource[(param.thisObject as WidgetChatListAdapterItemMessage)] = message
            }
        )
        patcher.patch(
            `WidgetChatListAdapterItemMessage$getMessageRenderContext$2`::class.java.getDeclaredMethod("invoke", String::class.java),
            InsteadHook { (param, str: String) ->
                val t = (param.thisObject as `WidgetChatListAdapterItemMessage$getMessageRenderContext$2`).`this$0` as WidgetChatListAdapterItemMessage
                val urlSource = mUrlSource[t] as Message
                val eventHandler = WidgetChatListAdapterItemMessage.`access$getAdapter$p`(t).getEventHandler() as WidgetChatListAdapterEventsHandler
                eventHandler.onSourcedUrlLongClicked(str, urlSource)
            }
        )
        patcher.patch(
            WidgetUrlActions::class.java.getDeclaredMethod("onViewCreated", View::class.java, Bundle::class.java),
            Hook { param ->
                val actions = param.thisObject as WidgetUrlActions
                val msg = mUrlSource2[actions] as Message

                addPluginDownloadOptions(msg, actions)
            }
        )
    }

    override fun stop(context: Context) {}

    fun addPluginDownloadOptions(msg: Message, actions: AppBottomSheet) {
        var layout: ViewGroup
        var targetId: String
        var str: String

        when(actions) {
            is WidgetChatListActions -> {
                layout = (actions.requireView() as ViewGroup).getChildAt(0) as ViewGroup
                targetId = "dialog_chat_actions_edit"
                str = msg.content ?: return
 
                if (layout.findViewById<View>(viewId) != null) return
            }

            is WidgetUrlActions -> {
                layout = actions.binding.getRoot() as ViewGroup
                targetId = "dialog_url_actions_copy"
                str = WidgetUrlActions.`access$getUrl$p`(actions)

                if (layout.findViewById<View>(urlViewId) != null) return
            }

            else -> return
        }

        when (msg.channelId) {
            PLUGIN_LINKS_UPDATES_CHANNEL_ID, PLUGIN_DEVELOPMENT_CHANNEL_ID -> {
                handlePluginMessage(str, layout, actions, targetId)
                handlePluginAttachments(msg, layout, actions, targetId)
            }

            SUPPORT_CHANNEL_ID, PLUGIN_SUPPORT_CHANNEL_ID -> {
                val member = StoreStream.getGuilds().getMember(ALIUCORD_GUILD_ID, msg.author.id)
                val isTrusted = member?.roles?.any { it in arrayOf(SUPPORT_HELPER_ROLE_ID, PLUGIN_DEVELOPER_ROLE_ID) } ?: false

                if (isTrusted) {
                    handlePluginMessage(str, layout, actions, targetId)
                    handlePluginAttachments(msg, layout, actions, targetId)
                }
            }

            PLUGIN_LINKS_CHANNEL_ID -> {
                handlePluginRepoMessage(str, layout, actions, targetId)
            }
        }
    }

    fun handlePluginRepoMessage(str: String, layout: ViewGroup, actions: AppBottomSheet, targetId: String) {
        if (repoPattern.containsMatchIn(str)) {
            val (author, repo) = repoPattern.find(str, 0)!!.groups.drop(1).map { it.value }

            addEntryBefore(layout, "Open Plugin Downloader", targetId) {
                Utils.openPageWithProxy(it.context, Modal(author, repo))
                actions.dismiss()
            }
        }
    }

    fun handlePluginMessage(str: String, layout: ViewGroup, actions: AppBottomSheet, targetId: String) {
        if (zipPattern.containsMatchIn(str)) {
            for (match in zipPattern.findAll(str, 0)) {
                val (author, repo, commit, name) = match.groups.drop(1).map { it.value }

                // Don't accidentally install core as a plugin
                if (name == "Aliucord") continue

                val plugin = PluginFile(name)
                addEntryBefore(layout, "${if (plugin.isInstalled) "Reinstall" else "Install"} $name", targetId) {
                    plugin.install("https://github.com/$author/$repo/raw/$commit/$name.zip")
                    actions.dismiss()
                }
            }
        }
    }

    fun handlePluginAttachments(msg: Message, layout: ViewGroup, actions: AppBottomSheet, targetId: String) {
        for (attachment in msg.attachments) {
            if (attachment.filename.run { !endsWith(".zip") || equals("Aliucord.zip") }) continue

            val name = attachment.filename.removeSuffix(".zip")
            val isInstalled = PluginManager.plugins.containsKey(name)

            addEntryBefore(layout, "${if (isInstalled) "Reinstall" else "Install"} $name", targetId) {
                PluginFile(name).install(
                    url = attachment.url,
                    callback = actions::dismiss,
                )
            }
        }
    }

    fun addEntryBefore(layout: ViewGroup, text: String, targetId: String, onClick: View.OnClickListener) {
        val targetView =
            layout.findViewById<View>(Utils.getResId(targetId, "id")) ?: return
        val idx = layout.indexOfChild(targetView)

        TextView(layout.context, null, 0, R.i.UiKit_Settings_Item_Icon).run {
            id = viewId
            setText(text)
            setOnClickListener(onClick)
            ContextCompat.getDrawable(layout.context, R.e.ic_file_download_white_24dp)?.run {
                mutate()
                setTint(ColorCompat.getThemedColor(layout.context, R.b.colorInteractiveNormal))
                setCompoundDrawablesRelativeWithIntrinsicBounds(this, null, null, null)
            }

            layout.addView(this, idx)
        }
    }
}
