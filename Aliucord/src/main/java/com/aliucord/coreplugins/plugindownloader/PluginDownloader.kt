/*
 * This file is part of Aliucord, an Android Discord client mod.
 * Copyright (c) 2021 Juby210 & Vendicated
 * Licensed under the Open Software License version 3.0
 */

package com.aliucord.coreplugins.plugindownloader

import android.annotation.SuppressLint
import android.content.Context
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.aliucord.Constants.*
import com.aliucord.Logger
import com.aliucord.Utils
import com.aliucord.entities.CorePlugin
import com.aliucord.patcher.Hook
import com.aliucord.patcher.component1
import com.aliucord.patcher.component2
import com.aliucord.wrappers.messages.AttachmentWrapper.Companion.filename
import com.aliucord.wrappers.messages.AttachmentWrapper.Companion.url
import com.discord.utilities.color.ColorCompat
import com.discord.widgets.chat.list.actions.WidgetChatListActions
import com.lytefast.flexinput.R
import java.util.regex.Pattern

internal val logger = Logger("PluginDownloader")

private val viewId = View.generateViewId()
private val repoPattern = Pattern.compile("https?://github\\.com/([A-Za-z0-9\\-_.]+)/([A-Za-z0-9\\-_.]+)")
private val zipPattern =
    Pattern.compile("https?://(?:github|raw\\.githubusercontent)\\.com/([A-Za-z0-9\\-_.]+)/([A-Za-z0-9\\-_.]+)/(?:raw|blob)?/?\\w+/(\\w+).zip")

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

    @SuppressLint("SetTextI18n")
    override fun start(context: Context) {
        patcher.patch(
            WidgetChatListActions::class.java.getDeclaredMethod("configureUI", WidgetChatListActions.Model::class.java),
            Hook { (param, model: WidgetChatListActions.Model) ->
                val actions = param.thisObject as WidgetChatListActions
                val layout = (actions.requireView() as ViewGroup).getChildAt(0) as ViewGroup

                if (layout.findViewById<View>(viewId) != null) return@Hook

                val msg = model.message
                val content = msg?.content ?: return@Hook

                if (msg.channelId == PLUGIN_DEVELOPMENT_CHANNEL_ID && msg.hasAttachments()) {
                    msg.attachments.forEach { attachment ->
                        val parts = attachment.filename.split('.')
                        if (parts.size == 2 && parts[1] == "zip" && parts[0] != "Aliucord") {
                            val plugin = PluginFile(parts[0])
                            addEntry(layout, "${if (plugin.isInstalled) "Reinstall" else "Install"} ${plugin.name}") {
                                plugin.install(attachment.url)
                                actions.dismiss()
                            }
                        }
                    }
                }

                when (msg.channelId) {
                    PLUGIN_LINKS_UPDATES_CHANNEL_ID, PLUGIN_SUPPORT_CHANNEL_ID, PLUGIN_DEVELOPMENT_CHANNEL_ID -> {
                        zipPattern.matcher(content).run {
                            while (find()) {
                                val author = group(1)!!
                                val repo = group(2)!!
                                val name = group(3)!!
                                val plugin = PluginFile(name)
                                addEntry(layout, "${if (plugin.isInstalled) "Reinstall" else "Install"} $name") {
                                    plugin.install(author, repo)
                                    actions.dismiss()
                                }
                            }
                        }
                    }
                    PLUGIN_LINKS_CHANNEL_ID -> {
                        repoPattern.matcher(content).takeIf { it.find() }?.run {
                            val author = group(1)!!
                            val repo = group(2)!!

                            addEntry(layout, "Open PluginDownloader") {
                                Utils.openPageWithProxy(it.context, Modal(author, repo))
                                actions.dismiss()
                            }
                        }
                    }
                }
            }
        )
    }

    override fun stop(context: Context) {}

    private fun addEntry(layout: ViewGroup, text: String, onClick: View.OnClickListener) {
        val replyView =
            layout.findViewById<View>(Utils.getResId("dialog_chat_actions_edit", "id")) ?: return
        val idx = layout.indexOfChild(replyView)

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
