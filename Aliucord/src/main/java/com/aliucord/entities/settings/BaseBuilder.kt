/*
 * This file is part of Aliucord, an Android Discord client mod.
 * Copyright (c) 2021 Juby210 & Vendicated
 * Licensed under the Open Software License version 3.0
 */

package com.aliucord.entities.settings

import android.content.Context
import android.view.View
import com.aliucord.api.SettingsAPI

@Suppress("UNCHECKED_CAST")
abstract class BaseBuilder<T : Any, TTransformed : Any>(val key: String, val default: TTransformed) :
    BaseItem() {
    @Volatile
    lateinit var currentValue: TTransformed
        internal set

    /** The description of this setting, used as hint in several views. */
    var description = key

    /** The secondary subscription of this setting. Used as subtext in some views */
    var subtext: String? = null

    /** Input validator. Should return true if valid, otherwise false */
    var validator: (newInput: TTransformed) -> Boolean = { true }

    /** The message that is displayed to the user if validation fails. Defaults to "Invalid Input" */
    var validatorFailedText = "Invalid Input"

    /** Listener that will be fired every time the user input changes */
    var onChange: (newInput: TTransformed) -> Unit = {}

    /** Listener that will be fired when the current View is clicked */
    var onClick: (clickedView: View, input: TTransformed) -> Unit = { _: View, _: TTransformed -> }

    /** Must override if T differs from TTransformed */
    var transform: (T) -> TTransformed = { it as TTransformed }

    @Synchronized
    fun setValue(settings: SettingsAPI, value: T) {
        currentValue = transform(value)
        settings.setUnknown(key, currentValue)
        onChange(currentValue)
    }

    override fun make(ctx: Context, settings: SettingsAPI): View {
        currentValue = settings.getUnknown(key, default) as TTransformed
        return super.make(ctx, settings)
    }
}
