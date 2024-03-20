/*
 * This file is part of Aliucord, an Android Discord client mod.
 * Copyright (c) 2021 Juby210 & Vendicated
 * Licensed under the Open Software License version 3.0
 */

package com.aliucord.entities.settings

import android.content.Context
import android.view.View
import android.widget.TextView
import com.aliucord.api.SettingsAPI
import com.aliucord.views.Divider
import com.lytefast.flexinput.R

abstract class BaseItem {
    protected abstract fun buildView(ctx: Context, settings: SettingsAPI): View

    /**
     * Build this view
     */
    internal open fun make(ctx: Context, settings: SettingsAPI) = buildView(ctx, settings)

    class Text(private val lazyMakeText: (Context, SettingsAPI) -> CharSequence) : BaseItem() {
        override fun buildView(ctx: Context, settings: SettingsAPI) =
            TextView(ctx, null, 0, R.i.UiKit_Settings_Text).apply { text = lazyMakeText(ctx, settings) }
    }

    class Header(private val lazyMakeText: (Context, SettingsAPI) -> CharSequence) : BaseItem() {
        override fun buildView(ctx: Context, settings: SettingsAPI) =
            TextView(ctx, null, 0, R.i.UiKit_Settings_Item_Header).apply { text = lazyMakeText(ctx, settings) }
    }

    object DIVIDER : BaseItem() {
        override fun buildView(ctx: Context, settings: SettingsAPI) = Divider(ctx)
    }
}
