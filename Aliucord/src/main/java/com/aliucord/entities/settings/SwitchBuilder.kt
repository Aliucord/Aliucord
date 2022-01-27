/*
 * This file is part of Aliucord, an Android Discord client mod.
 * Copyright (c) 2021 Juby210 & Vendicated
 * Licensed under the Open Software License version 3.0
 */

package com.aliucord.entities.settings

import android.annotation.SuppressLint
import android.content.Context
import android.view.View
import com.aliucord.Utils
import com.aliucord.api.SettingsAPI
import com.discord.views.CheckedSetting

class SwitchBuilder(key: String, default: Boolean, init: SwitchBuilder.() -> Unit) : BaseBuilder<Boolean, Boolean>(key, default) {
    init {
        init(this)
    }

    /** The type of this switch. Defaults to [CheckedSetting.ViewType.SWITCH] */
    var switchType = CheckedSetting.ViewType.SWITCH

    @SuppressLint("SetTextI18n")
    override fun buildView(ctx: Context, settings: SettingsAPI) =
        Utils.createCheckedSetting(ctx, switchType, description, subtext).apply {
            setOnCheckedListener {
                if (validator(it)) {
                    setValue(settings, it)
                } else {
                    l.a().text = "$description ($validatorFailedText)"
                }
            }
            setOnClickListener {
                onClick(it, currentValue)
            }
        }
}
