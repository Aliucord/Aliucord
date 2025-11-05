/*
 * This file is part of Aliucord, an Android Discord client mod.
 * Copyright (c) 2021 Juby210 & Vendicated
 * Licensed under the Open Software License version 3.0
 */

package com.aliucord.coreplugins.plugindownloader

import android.view.View
import com.aliucord.fragments.SettingsPage
import android.widget.TextView
import com.aliucord.Constants
import com.aliucord.Utils
import com.lytefast.flexinput.R
import java.io.PrintWriter
import java.io.StringWriter

import com.aliucord.Http
import com.aliucord.views.Button
import com.aliucord.views.DangerButton
import com.google.gson.reflect.TypeToken

internal class Modal(private val author: String, private val repo: String) : SettingsPage() {
    private val resType = TypeToken.getParameterized(MutableMap::class.java, String::class.java, PluginInfo::class.java).getType()

    private var throwable = null as Throwable?
    private var plugins = null as Map<String, PluginInfo>?

    override fun onViewBound(view: View) {
        super.onViewBound(view)

        setActionBarTitle("Plugin downloader")
        setActionBarSubtitle("$author/$repo")

        val ctx = view.context

        when {
            throwable != null -> {
                TextView(ctx, null, 0, R.i.UiKit_Settings_Item_SubText).run {
                    val sw = StringWriter()
                    val pw = PrintWriter(sw)
                    throwable!!.printStackTrace(pw)

                    text = "An error occurred:\n\n$sw"
                    setTextIsSelectable(true)
                    linearLayout.addView(this)
                }
            }
            plugins == null -> {
                Utils.threadPool.execute {
                    try {
                        plugins =
                            Http.simpleJsonGet("https://cdn.jsdelivr.net/gh/$author/$repo@refs/heads/builds/updater.json", resType)
                    } catch (th: Throwable) {
                        throwable = th
                    }
                    Utils.mainThread.post { onViewBound(view) }
                }
            }
            else -> {
                val outdatedDiscordWarning =
                    TextView(ctx, null, 0, R.i.UiKit_Settings_Item_SubText).apply {
                        text = "Note: Some buttons are disabled because they require a newer Discord version. Please update Aliucord using the installer first."
                        visibility = View.GONE
                        addView(this)
                    }

                plugins!!.mapNotNull { (name, info) ->
                    if (name == "default") return@mapNotNull null
                    val plugin = PluginFile(name)
                    val installed = plugin.isInstalled
                    val title = "${if (installed) "Uninstall" else "Install"} $name v${info.version}"
                    PluginCardInfo(plugin, title, installed, info.minimumDiscordVersion)
                }.sortedBy { it.title }.forEach { (file, title, exists, minimumDiscordVersion) ->
                    val btn = if (exists) DangerButton(ctx) else Button(ctx)
                    btn.text = title
                    if (!exists) {
                        if (Constants.DISCORD_VERSION < minimumDiscordVersion) {
                            btn.isEnabled = false
                            outdatedDiscordWarning.visibility = View.VISIBLE
                        }
                        btn.setOnClickListener {
                            file.install(author, repo, ::reRender)
                        }
                    } else btn.setOnClickListener {
                        file.uninstall(::reRender)
                    }
                    addView(btn)
                }
            }
        }
    }
}
