/*
 * This file is part of Aliucord, an Android Discord client mod.
 * Copyright (c) 2021 Juby210 & Vendicated
 * Licensed under the Open Software License version 3.0
 */

package com.aliucord.entities.settings

import android.content.Context
import android.view.inputmethod.EditorInfo
import com.aliucord.api.SettingsAPI
import com.aliucord.views.TextInput

class NumberInputBuilder(key: String, default: Int, init: NumberInputBuilder.() -> Unit = {}) : TextInputBuilder(key, default.toString(), {
    transform = {
        it.toInt()
    }
    validator = {
        try {
            it.toString().toInt()
            true
        } catch (th: NumberFormatException) {
            false
        }
    }
}) {
    init {
        init(this)
    }

    override fun buildView(ctx: Context, settings: SettingsAPI): TextInput {
        return super.buildView(ctx, settings).apply {
            editText.inputType = EditorInfo.TYPE_CLASS_NUMBER
        }
    }
}
