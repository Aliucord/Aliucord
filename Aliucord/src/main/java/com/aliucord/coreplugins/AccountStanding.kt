/*
 * This file is part of Aliucord, an Android Discord client mod.
 * Copyright (c) 2025 Juby210 & Vendicated
 * Licensed under the Open Software License version 3.0
 */

package com.aliucord.coreplugins

import android.content.Context
import android.view.View
import android.widget.TextView
import com.aliucord.Utils
import com.aliucord.entities.CorePlugin
import com.aliucord.patcher.*
import androidx.appcompat.widget.LinearLayoutCompat
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.widget.NestedScrollView
import com.discord.widgets.settings.WidgetSettings
import com.lytefast.flexinput.R

internal class AccountStanding : CorePlugin(Manifest("AccountStanding")) {
    override val isHidden = true
    
    init {
        manifest.description = "Adds account standing to Aliucord"
    }

    override fun start(context: Context) {
       // Patches the settings menu for the authorized apps page
       patcher.after<WidgetSettings>("onViewBound", View::class.java) { (_, view: CoordinatorLayout) ->
            val layout = (view.getChildAt(1) as NestedScrollView).getChildAt(0) as LinearLayoutCompat
            val ctx = layout.context
            val baseIndex = layout.indexOfChild(layout.findViewById<TextView>(Utils.getResId("qr_scanner", "id")))
            TextView(ctx, null, 0, R.i.UiKit_Settings_Item_Icon).apply {
                text = "Account Standing"
                setCompoundDrawablesWithIntrinsicBounds(Utils.tintToTheme(ctx.getDrawable(R.e.ic_security_24dp)), null, null, null)
                setOnClickListener {
                    Utils.openPageWithProxy(ctx, AccountStandingPage())
                }
                layout.addView(this, baseIndex + 1)
            }
        }
    }

    override fun stop(context: Context) = patcher.unpatchAll()
}
