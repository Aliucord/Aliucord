/*
 * This file is part of Aliucord, an Android Discord client mod.
 * Copyright (c) 2021 Juby210 & Vendicated
 * Licensed under the Open Software License version 3.0
 */

package com.aliucord.settings

import android.annotation.SuppressLint
import android.content.Context
import android.view.View
import android.widget.TextView
import androidx.annotation.DrawableRes
import androidx.core.content.ContextCompat
import com.aliucord.*
import com.aliucord.fragments.SettingsPage
import com.discord.stores.StoreStream
import com.discord.views.CheckedSetting
import com.lytefast.flexinput.R

const val AUTO_DISABLE_ON_CRASH_KEY = "autoDisableCrashingPlugins"
const val AUTO_UPDATE_PLUGINS_KEY = "AC_plugins_auto_update_enabled"
const val AUTO_UPDATE_ALIUCORD_KEY = "AC_aliucord_auto_update_enabled"
const val ALIUCORD_FROM_STORAGE_KEY = "AC_from_storage"

class AliucordPage : SettingsPage() {
    @SuppressLint("SetTextI18n")
    override fun onViewBound(view: View) {
        super.onViewBound(view)

        setActionBarTitle("Aliucord")
        setActionBarSubtitle("Aliucord Settings")

        val ctx = view.context

        addHeader(ctx, "Aliucord Settings")
        addSwitch(ctx,
            AUTO_DISABLE_ON_CRASH_KEY,
            "Automatically disable plugins on crash",
            "When a plugin is found to be causing crashes, it will automatically be deleted",
            true
        )
        addSwitch(ctx, AUTO_UPDATE_ALIUCORD_KEY, "Automatically update Aliucord", null)
        addSwitch(ctx, AUTO_UPDATE_PLUGINS_KEY, "Automatically update plugins", null)

        if (StoreStream.getUserSettings().isDeveloperMode) {
            addDivider(ctx)
            addHeader(ctx, "Developer Settings")
            addSwitch(
                ctx,
                ALIUCORD_FROM_STORAGE_KEY,
                "Use Aliucord from storage",
                "Meant for developers. Do not enable unless you know what you're doing. If someone else is telling you to do this, you are likely being scammed."
            )
        }

        addDivider(ctx)
        addHeader(ctx, "Links")
        addLink(ctx, "Source Code", R.e.ic_account_github_white_24dp) {
            Utils.launchUrl(Constants.ALIUCORD_GITHUB_REPO)
        }
        addLink(ctx, "Support Server", R.e.ic_help_24dp) {
            Utils.joinSupportServer(it.context)
        }
        addLink(ctx, "Support us on Patreon", R.e.ic_heart_24dp) {
            Utils.launchUrl(Constants.ALIUCORD_PATREON)
        }
        addLink(ctx, "... or on OnlyFans", R.e.ic_heart_24dp) {
            Utils.launchUrl("https://www.youtube.com/watch?v=dQw4w9WgXcQ")
        }
    }

    private fun addSwitch(ctx: Context, setting: String, title: String, subtitle: String?, default: Boolean = false) {
        Utils.createCheckedSetting(ctx, CheckedSetting.ViewType.SWITCH, title, subtitle).run {
            isChecked = SettingsUtils.getBool(setting, default)
            setOnCheckedListener {
                SettingsUtils.setBool(setting, it)
            }
            linearLayout.addView(this)
        }
    }

    private fun addLink(ctx: Context, text: String, @DrawableRes drawable: Int, action: View.OnClickListener) {
        TextView(ctx, null, 0, R.i.UiKit_Settings_Item_Icon).run {
            this.text = text
            val drawableEnd = ContextCompat.getDrawable(ctx, R.e.ic_open_in_new_white_24dp)?.run {
                mutate()
                Utils.tintToTheme(this)
            }
            val drawableStart = ContextCompat.getDrawable(ctx, drawable)?.run {
                mutate()
                Utils.tintToTheme(this)
            }
            setCompoundDrawablesRelativeWithIntrinsicBounds(drawableStart, null, drawableEnd, null)
            setOnClickListener(action)
            linearLayout.addView(this)
        }
    }
}
