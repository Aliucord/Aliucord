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

/**
 * SettingsBuilder that auto generates a highly customisable Settings Page for you
 *
 * @property plugin Plugin to build page for
 * @see [Plugin.buildSettings]
 */
class SettingsBuilder(val plugin: Plugin, val type: Plugin.SettingsTab.Type) {
    private val items = ArrayList<BaseItem>()

    /**
     * Build this SettingsPage
     *
     * @return built [Plugin.SettingsTab] for use as [Plugin.settingsTab]
     */
    fun build(): Plugin.SettingsTab = Plugin.SettingsTab(
        when (type) {
            Plugin.SettingsTab.Type.BOTTOM_SHEET -> BuiltSheet::class.java
            Plugin.SettingsTab.Type.PAGE -> BuiltPage::class.java
        },
        type
    ).withArgs(plugin to items)

    /**
     * Add a String input
     *
     * @param settingsKey Key of this setting
     * @param defaultValue Default value
     * @param init Optional init method to customise the input
     */
    @JvmOverloads
    fun addStringInput(settingsKey: String, defaultValue: String, init: TextInputBuilder.() -> Unit = {}): SettingsBuilder {
        items.add(TextInputBuilder(settingsKey, defaultValue, init))
        return this
    }

    /**
     * Add a number input
     *
     * @param settingsKey Key of this setting
     * @param defaultValue Default value
     * @param init Optional init method to customise the input
     */
    @JvmOverloads
    fun addNumberInput(settingsKey: String, defaultValue: Int, init: NumberInputBuilder.() -> Unit = {}): SettingsBuilder {
        items.add(NumberInputBuilder(settingsKey, defaultValue, init))
        return this
    }

    /**
     * Add a Switch
     *
     * @param settingsKey Key of the setting this switch will toggle
     * @param defaultValue Default value
     * @param init Optional init method to customise the switch
     */
    @JvmOverloads
    fun addSwitch(settingsKey: String, defaultValue: Boolean, init: SwitchBuilder.() -> Unit = {}): SettingsBuilder {
        items.add(SwitchBuilder(settingsKey, defaultValue, init))
        return this
    }

    /**
     * Add a Header
     * @param lazyMakeText Function that returns the text this header should have
     */
    fun addHeader(lazyMakeText: (context: Context, settings: SettingsAPI) -> CharSequence): SettingsBuilder {
        items.add(BaseItem.Header(lazyMakeText))
        return this
    }

    /**
     * Add a Header
     * @param text The text of this header
     */
    fun addHeader(text: CharSequence): SettingsBuilder {
        addHeader() { _, _ -> text }
        return this
    }

    /**
     * Add a TextView
     * @param lazyMakeText Function that returns the text this TextView should have
     */
    fun addText(lazyMakeText: (context: Context, settings: SettingsAPI) -> CharSequence): SettingsBuilder {
        items.add(BaseItem.Text(lazyMakeText))
        return this
    }

    /**
     * Add a TextView
     * @param text The text of this textView
     */
    fun addText(text: CharSequence): SettingsBuilder {
        addText() { _, _ -> text }
        return this
    }

    /**
     * Add a [com.aliucord.views.Divider]
     */
    fun addDivider(): SettingsBuilder {
        items.add(BaseItem.DIVIDER)
        return this
    }

    /**
     * Add a custom component
     *
     * @param item Item to add
     */
    fun addCustom(item: BaseItem): SettingsBuilder {
        items.add(item)
        return this
    }

    /**
     * Build and add a custom component
     *
     * @param T Type parameter for the initial input. Usually String
     * @param TTransformed Type parameter for the transformed input.
     * If you need no transforming, simply make this the same as [T].
     * If this differs from [T], you must set the transform method to one
     * that returns a value of this type. For instance, if [T] is String and [TTransformed] is Int, you must set [BaseBuilder.transform] to something like
     * { it.toInt() }. You should also set [BaseBuilder.validator] to make sure the input is valid before transforming it
     * @param settingsKey The key of this setting
     * @param defaultValue The default value
     * @param build Method that builds the custom component
     *
     * Some fields you can use to make this component:
     * - [BaseBuilder.currentValue]: Refers to the current value
     * - [BaseBuilder.setValue]: Will transform the provided value using [BaseBuilder.transform], update [BaseBuilder.currentValue], save to settings and call [BaseBuilder.onChange]
     *
     * See some of the Builder implementations like [SwitchBuilder] or [TextInputBuilder] for examples
     */
    fun <T : Any, TTransformed : Any> addCustom(
        settingsKey: String,
        defaultValue: TTransformed,
        build: BaseBuilder<T, TTransformed>.(ctx: Context, settings: SettingsAPI) -> View,
    ): SettingsBuilder {
        addCustom(object : BaseBuilder<T, TTransformed>(settingsKey, defaultValue) {
            override fun buildView(ctx: Context, settings: SettingsAPI): View {
                return build(this, ctx, settings)
            }
        })
        return this
    }
}
