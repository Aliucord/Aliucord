/*
 * Copyright (c) 2021 Juby210 & Vendicated
 * Licensed under the Open Software License version 3.0
 */

package com.aliucord.entities

import android.content.Context
import android.content.res.Resources
import android.view.View
import com.aliucord.Logger
import com.aliucord.annotations.AliucordPlugin
import com.aliucord.api.*
import com.discord.app.AppBottomSheet
import com.discord.app.AppFragment

/** The base class that all plugins must extend from for their entrypoint. */
@Suppress("unused")
abstract class Plugin internal constructor(manifest: Manifest? = null) {
    constructor() : this(null)

    /** Plugin Manifest */
    class Manifest {
        /** Plugin Author */
        class Author @JvmOverloads constructor(
            /** The name of the plugin author */
            @JvmField var name: String,
            /** The Discord ID of the plugin author */
            @JvmField var id: Long = 0,
            /** Whether to hyperlink the profile specified by [id] */
            @JvmField var hyperlink: Boolean = true,
        ) {
            override fun toString(): String = name
        }

        /** The name of this plugin as defined by the plugin's Gradle project's name */
        @JvmField
        var name: String? = null

        @JvmField
        internal var pluginClassName: String? = null

        /** The authors of this plugin */
        @JvmField
        var authors: Array<Author?> = arrayOf<Author?>()

        /** A short description of this plugin */
        @JvmField
        var description: String = ""

        /** The current version of this plugin */
        @JvmField
        var version: String = "1.0.0"

        /** The updater JSON url */
        @JvmField
        var updateUrl: String? = null

        /** Changelog featuring recent updates, written in markdown */
        @JvmField
        var changelog: String? = null

        /** Image or video link that will be displayed at the top of the changelog */
        @JvmField
        var changelogMedia: String? = null

        /**
         * This is only used by coreplugins.
         */
        internal constructor(name: String) {
            this.name = name
        }

        /**
         * This is only used by coreplugins.
         */
        internal constructor(name: String, description: String) {
            this.name = name
            this.description = description
        }

        internal constructor()
    }

    /** Plugin SettingsTab */
    class SettingsTab {
        /** The type of this SettingsTab. PAGE is a dedicated page, BOTTOM_SHEET is a popup at the bottom of the screen. */
        enum class Type {
            PAGE, BOTTOM_SHEET
        }

        interface SettingsPage {
            fun onViewBound(view: View)
        }

        /** The [Type] of this SettingsTab */
        @JvmField
        var type: Type

        /** The Page fragment */
        @JvmField
        var page: Class<out AppFragment>? = null

        /** The BottomSheet component */
        @JvmField
        var bottomSheet: Class<out AppBottomSheet>? = null

        /** The arguments that will be passed to the constructor of the component */
        @JvmField
        var args: Array<out Any?>? = null

        /**
         * Creates a SettingsTab with a dedicated page
         * @param settings The settings page fragment
         */
        constructor(settings: Class<out AppFragment>) {
            type = Type.PAGE
            page = settings
        }

        /**
         * Creates a SettingsTab of the specified type
         * @param settings The component to use for this SettingsTab
         * @param type The [Type] of this SettingsTab
         */
        @Suppress("UNCHECKED_CAST")
        constructor(settings: Class<*>, type: Type) {
            this.type = type
            when (type) {
                Type.PAGE -> {
                    page = settings as Class<out AppFragment>
                }
                Type.BOTTOM_SHEET -> {
                    bottomSheet = settings as Class<AppBottomSheet>
                }
            }
        }

        /**
         * Sets the constructor args that will be passed to this SettingsTab
         * @param args The arguments that should be passed
         */
        fun withArgs(vararg args: Any?): SettingsTab {
            this.args = args
            return this
        }
    }

    private var _manifest: Manifest? = null

    /** Method returning the [Manifest] of your Plugin */
    val manifest: Manifest
        @JvmName("getManifest")
        get() = _manifest!!

    /**
     * Returns whether the user will be prompted to restart after enabling/disabling.
     * This is toggleable through the `@AliucordPlugin` annotation.
     * You should not override this method yourself.
     * @return [AliucordPlugin.requiresRestart]
     */
    open fun requiresRestart(): Boolean {
        val annotation = this.annotation
        return annotation != null && annotation.requiresRestart
    }

    /**
     * Returns the `@AliucordPlugin` on this class annotation if it exists.
     */
    val annotation: AliucordPlugin?
        @JvmName("getAnnotation")
        get() = this.javaClass.getAnnotation(AliucordPlugin::class.java)

    /**
     * Called when your Plugin is loaded
     * @param context An activity Android context.
     */
    @Throws(Throwable::class)
    open fun load(context: Context) {
        annotation
    }

    /**
     * Called when your Plugin is unloaded
     * @param context An activity Android context
     */
    @Throws(Throwable::class)
    open fun unload(context: Context) {
    }

    /**
     * Called when your Plugin is started
     * @param context An activity Android context
     */
    @Throws(Throwable::class)
    open fun start(context: Context) {
    }

    /**
     * Called when your Plugin is stopped
     * @param context An activity Android context
     */
    @Throws(Throwable::class)
    open fun stop(context: Context) {
    }

    /**
     * The name of this plugin as known by the updater.
     * The name of this class does not necessarily have to match.
     */
    val name: String
        @JvmName("getName")
        get() = requireNotNull(manifest.name)

    /** The [Logger] of your plugin. Use this to log information */
    @JvmField
    val logger: Logger

    /** SettingsTab associated with this plugin. Set this to register a settings page */
    @JvmField
    var settingsTab: SettingsTab? = null

    /** The resources of your plugin. */
    @JvmField
    var resources: Resources? = null

    /** Whether your plugin has resources that need to be loaded */
    @JvmField
    @Deprecated(
        message = "Resources are now loaded automatically.",
        replaceWith = ReplaceWith(""),
        level = DeprecationLevel.WARNING,
    )
    var needsResources: Boolean = false

    /** The filename of the zip this plugin was loaded from */
    @JvmField
    @Suppress("PropertyName")
    var __filename: String? = null

    /** The [CommandsAPI] of your plugin. You can register/unregister commands here */
    @JvmField
    protected val commands: CommandsAPI

    /** The [PatcherAPI] of your plugin. You can add/remove patches here */
    @JvmField
    protected val patcher: PatcherAPI

    /** The [SettingsAPI] of your plugin. Use this to store persistent data */
    @JvmField
    val settings: SettingsAPI

    init {
        // For core plugins
        if (manifest != null) {
            _manifest = manifest
        }

        // PluginManager patches the constructor to set a manifest for external plugins
        checkNotNull(this.manifest) { "Manifest was null, this should never happen" }

        this.logger = Logger(this.name)
        this.settings = SettingsAPI(this.name)
        this.patcher = PatcherAPI(this.logger)
        this.commands = CommandsAPI(this.name)
    }
}
