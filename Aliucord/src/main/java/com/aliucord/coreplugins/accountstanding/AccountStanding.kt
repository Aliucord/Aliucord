/*
 * This file is part of Aliucord, an Android Discord client mod.
 * Copyright (c) 2025 Juby210 & Vendicated
 * Licensed under the Open Software License version 3.0
 */

package com.aliucord.coreplugins.accountstanding

import android.content.Context
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.widget.NestedScrollView
import com.aliucord.Http
import com.aliucord.Utils
import com.aliucord.Utils.openPage
import com.aliucord.api.NotificationsAPI
import com.aliucord.entities.CorePlugin
import com.aliucord.entities.NotificationData
import com.aliucord.patcher.*
import com.aliucord.settings.delegate
import com.aliucord.utils.MDUtils
import com.aliucord.utils.ViewUtils.addTo
import com.aliucord.utils.ViewUtils.findViewById
import com.discord.api.user.UserFlags
import com.discord.stores.StoreStream
import com.lytefast.flexinput.R
import com.aliucord.api.SettingsAPI
import com.aliucord.utils.GsonUtils
import com.discord.widgets.settings.account.WidgetSettingsAccount

internal class AccountStanding : CorePlugin(Manifest("AccountStanding")) {
    private var SettingsAPI.classifications by settings.delegate(HashMap<Long, List<PluginResponse.Classifications>>())
    private var SettingsAPI.isFetched by settings.delegate(false)

    init {
        manifest.description = "Adds account standing to Aliucord"
    }

    private fun fetchClassifications() {
        Utils.threadPool.execute {
            try {
                logger.info("Fetching classifications...")
                val json = Http.Request.newDiscordRNRequest("/safety-hub/@me", "GET").execute()
                    .json(GsonUtils.gsonRestApi, PluginResponse::class.java)

                addClassifications(json)

                logger.info("Fetched classifications! Classifications stored: ${settings.classifications}")
            } catch (e: Exception) {
                logger.error("Failed to fetch data!", e)
            }
        }
    }

    private fun addClassifications(json: PluginResponse) {
        val newMap = HashMap<Long, List<PluginResponse.Classifications>>()
        val me = StoreStream.getUsers().me

        newMap[me.id] = json.classifications!!

        if (newMap[me.id] != settings.classifications[me.id] && settings.classifications.isNotEmpty() && (me.flags and UserFlags.HAS_UNREAD_URGENT_MESSAGES) != 0) {
            sendNotification()
        }

        settings.classifications = newMap
        settings.isFetched = true
    }

    private fun sendNotification() {
        val notificationData = NotificationData()
            .setTitle("Account Standing")
            .setAutoDismissPeriodSecs(10)
            .setOnClick { _ ->
                openPage(Utils.appActivity, AccountStandingPage::class.java)
            }
            .setBody(MDUtils.render("You broke Discord's rules, Please check Account Standing for more info."))

        NotificationsAPI.display(notificationData)
    }

    override fun start(context: Context) {
        patcher.after<WidgetSettingsAccount>("onViewBound", View::class.java) { (_, view: CoordinatorLayout) ->
            val layout = (view.getChildAt(1) as NestedScrollView).getChildAt(0) as LinearLayout
            val baseIndex = layout.indexOfChild(layout.findViewById<TextView>("settings_account_information_header"))
            val ctx = layout.context

            TextView(ctx, null, 0, R.i.UiKit_Settings_Item).apply {
                text = "Account Standing"
                setCompoundDrawablesWithIntrinsicBounds(
                    null,
                    null,
                    ctx.getDrawable(R.e.ic_navigate_next_white_a60_24dp),
                    null
                )
                setOnClickListener {
                    Utils.openPageWithProxy(ctx, AccountStandingPage())
                }
            }.addTo(layout, baseIndex + 1)
        }

        if ((StoreStream.getUsers().me.flags and UserFlags.HAS_UNREAD_URGENT_MESSAGES) != 0 || !settings.isFetched) {
            fetchClassifications()
        } else {
            logger.info("Classifications has already been fetched, Not fetching.")
        }
    }

    override fun stop(context: Context) = patcher.unpatchAll()
}
