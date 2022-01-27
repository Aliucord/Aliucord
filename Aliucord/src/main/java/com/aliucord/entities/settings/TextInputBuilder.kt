/*
 * This file is part of Aliucord, an Android Discord client mod.
 * Copyright (c) 2021 Juby210 & Vendicated
 * Licensed under the Open Software License version 3.0
 */

package com.aliucord.entities.settings

import android.content.Context
import android.text.Editable
import android.text.TextWatcher
import com.aliucord.api.SettingsAPI
import com.aliucord.views.TextInput

open class TextInputBuilder(key: String, default: String, init: TextInputBuilder.() -> Unit = {}) :
    BaseBuilder<String, Any /* Any to allow NumberInputBuilder*/ >(key, default) {

    init {
        @Suppress("LeakingThis")
        init(this)
    }

    override fun buildView(ctx: Context, settings: SettingsAPI) = TextInput(ctx, description, currentValue.toString()).apply {
        editText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            }

            override fun afterTextChanged(s: Editable) {
                val newVal = s.toString()
                if (validator(newVal)) {
                    setValue(settings, newVal)
                } else {
                    this@apply.setHint("$description ($validatorFailedText)")
                }
            }
        })
        setOnClickListener {
            onClick(it, currentValue)
        }
    }
}
