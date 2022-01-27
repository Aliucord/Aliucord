/*
 * This file is part of Aliucord, an Android Discord client mod.
 * Copyright (c) 2021 Juby210 & Vendicated
 * Licensed under the Open Software License version 3.0
 */

package com.aliucord.entities.settings

import android.content.Context
import android.view.View
import com.aliucord.api.SettingsAPI
import com.aliucord.entities.Plugin

class SettingsBuilder(val plugin: Plugin) {
    private val items = ArrayList<BaseItem>()

    fun build() = BuiltPage(plugin, items)

    /**
     * Add a String input
     *
     * @param settingsKey Key of this setting
     * @param defaultValue Default value
     * @param init Optional init method to customise the input
     */
    @JvmOverloads
    fun addStringInput(settingsKey: String, defaultValue: String, init: TextInputBuilder.() -> Unit = {}) {
        items.add(TextInputBuilder(settingsKey, defaultValue, init))
    }

    /**
     * Add a number input
     *
     * @param settingsKey Key of this setting
     * @param defaultValue Default value
     * @param init Optional init method to customise the input
     */
    @JvmOverloads
    fun addNumberInput(settingsKey: String, defaultValue: Int, init: NumberInputBuilder.() -> Unit = {}) {
        items.add(NumberInputBuilder(settingsKey, defaultValue, init))
    }

    /**
     * Add a Switch
     *
     * @param settingsKey Key of the setting this switch will toggle
     * @param defaultValue Default value
     * @param init Optional init method to customise the switch
     */
    @JvmOverloads
    fun addSwitch(settingsKey: String, defaultValue: Boolean, init: SwitchBuilder.() -> Unit = {}) {
        items.add(SwitchBuilder(settingsKey, defaultValue, init))
    }

    /**
     * Add a Header
     * @param lazyMakeText Function that returns the text this header should have
     */
    fun addHeader(lazyMakeText: (context: Context, settings: SettingsAPI) -> CharSequence) {
        items.add(BaseItem.Header(lazyMakeText))
    }

    /**
     * Add a Header
     * @param text The text of this header
     */
    fun addHeader(text: CharSequence) {
        addHeader() { _, _ -> text }
    }

    /**
     * Add a TextView
     * @param lazyMakeText Function that returns the text this TextView should have
     */
    fun addText(lazyMakeText: (context: Context, settings: SettingsAPI) -> CharSequence) {
        items.add(BaseItem.Text(lazyMakeText))
    }

    /**
     * Add a TextView
     * @param text The text of this textView
     */
    fun addText(text: CharSequence) {
        addText() { _, _ -> text }
    }

    /**
     * Add a [com.aliucord.views.Divider]
     */
    fun addDivider() {
        items.add(BaseItem.DIVIDER)
    }

    /**
     * Add a custom component
     *
     * @param item Item to add
     */
    fun addCustom(item: BaseItem) {
        items.add(item)
    }

    /**
     * Build and add a custom component
     *
     * @param T Type parameter for the initial input. Usually String
     * @param TTransformed Type parameter for the transformed input. If this differs from [T], you must set the transform method to one
     * that returns a value of this type. For instance, if [T] is String and [TTransformed] is Int, you must set [BaseBuilder.transform] to something like
     * { it.toInt() }. You should also set [BaseBuilder.validator] to make sure the input is valid before transforming it
     * @param settingsKey The key of this setting
     * @param defaultValue The default value
     * @param build Method that builds the custom component
     *
     * Some fields you can use to make this component:
     * - [BaseBuilder.currentValue]: Refers to the current value
     * - [BaseBuilder.setValue]: Will transform the provided value using [BaseBuilder.transform], update [BaseBuilder.currentValue], save to settings and call [BaseBuilder.onChange]
     */
    fun <T : Any, TTransformed : Any> addCustom(
        settingsKey: String,
        defaultValue: TTransformed,
        build: BaseBuilder<T, TTransformed>.(ctx: Context, settings: SettingsAPI) -> View,
    ) {
        addCustom(object : BaseBuilder<T, TTransformed>(settingsKey, defaultValue) {
            override fun buildView(ctx: Context, settings: SettingsAPI): View {
                return build(this, ctx, settings)
            }
        })
    }
}
