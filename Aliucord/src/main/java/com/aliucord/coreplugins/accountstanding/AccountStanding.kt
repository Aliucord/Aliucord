/*
 * This file is part of Aliucord, an Android Discord client mod.
 * Copyright (c) 2025 Juby210 & Vendicated
 * Licensed under the Open Software License version 3.0
 */

package com.aliucord.coreplugins.accountstanding

import android.content.Context
import android.view.View
import android.widget.TextView
import androidx.appcompat.widget.LinearLayoutCompat
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.widget.NestedScrollView
import com.aliucord.Utils
import com.aliucord.Utils.openPage
import com.aliucord.api.NotificationsAPI
import com.aliucord.entities.CorePlugin
import com.aliucord.entities.NotificationData
import com.aliucord.patcher.*
import com.aliucord.utils.MDUtils
import com.aliucord.utils.ViewUtils.addTo
import com.aliucord.utils.ViewUtils.findViewById
import com.discord.stores.StoreStream
import com.discord.widgets.settings.WidgetSettings
import com.lytefast.flexinput.R

internal class AccountStanding : CorePlugin(Manifest("AccountStanding")) {
    override val isHidden = true

    init {
        manifest.description = "Adds account standing to Aliucord"
    }

    override fun start(context: Context) {
        patcher.after<WidgetSettings>("onViewBound", View::class.java) { (_, view: CoordinatorLayout) ->
            val layout = (view.getChildAt(1) as NestedScrollView).getChildAt(0) as LinearLayoutCompat
            val baseIndex = layout.indexOfChild(layout.findViewById<TextView>("qr_scanner"))
            val ctx = layout.context

            TextView(ctx, null, 0, R.i.UiKit_Settings_Item_Icon).apply {
                text = "Account Standing"
                setCompoundDrawablesWithIntrinsicBounds(Utils.tintToTheme(ctx.getDrawable(R.e.ic_security_24dp)), null, null, null)
                setOnClickListener {
                    Utils.openPageWithProxy(ctx, AccountStandingPage())
                }
            }.addTo(layout, baseIndex + 1)
        }

        val notificationData = NotificationData()
            .setTitle("Account Standing")
            .setAutoDismissPeriodSecs(10)
            .setOnClick { _ ->
                openPage(Utils.appActivity, AccountStandingPage::class.java)
            }
            .setBody(MDUtils.render("You broke Discord's rules, Please check Account Standing for more info."))

        if (StoreStream.getUsers().me.flags == 8192) NotificationsAPI.display(notificationData)
    }

    override fun stop(context: Context) = patcher.unpatchAll()
}
