/*
 * This file is part of Aliucord, an Android Discord client mod.
 * Copyright (c) 2021 Juby210 & Vendicated
 * Licensed under the Open Software License version 3.0
 */

package com.aliucord.entities.settings

import android.os.Bundle
import android.view.View
import com.aliucord.entities.Plugin
import com.aliucord.fragments.SettingsPage
import com.aliucord.widgets.BottomSheet

class BuiltPage internal constructor(private val plugin: Plugin, private val items: List<BaseItem>) : SettingsPage() {
    override fun onViewBound(view: View) {
        super.onViewBound(view)

        setActionBarTitle(plugin.name)

        items.forEach {
            addView(it.make(view.context, plugin.settings))
        }
    }
}

class BuiltSheet internal constructor(private val plugin: Plugin, private val items: List<BaseItem>) : BottomSheet() {
    override fun onViewCreated(view: View, bundle: Bundle?) {
        super.onViewCreated(view, bundle)

        items.forEach {
            addView(it.make(view.context, plugin.settings))
        }
    }
}
