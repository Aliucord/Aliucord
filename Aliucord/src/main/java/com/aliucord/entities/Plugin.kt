/*
 * Copyright (c) 2021 Juby210 & Vendicated
 * Licensed under the Open Software License version 3.0
 */
package com.aliucord.entities

import android.content.Context
import android.content.res.Resources
import android.view.View
import androidx.fragment.app.Fragment
import com.aliucord.Logger
import com.aliucord.annotations.AliucordPlugin
import com.aliucord.api.*
import com.discord.app.AppBottomSheet
import com.discord.app.AppFragment

/** Base Plugin class all plugins must extend  */
@Suppress("unused")
abstract class Plugin {
    companion object {
        /** Name of this plugin. Defaults to the class name  */
        @Deprecated("Use the getName() method instead")
        @JvmField
        var name: String = this::class.java.simpleName
    }

    /** The [SettingsAPI] of your plugin. Use this to store persistent data  */
    @JvmField
    val settings = SettingsAPI(name)

    /** SettingsTab associated with this plugin. Set this to register a settings page  */
    @JvmField
    var settingsTab: SettingsTab? = null

    /** The resources of your plugin. You need to set [.needsResources] to true to use this  */
    @JvmField
    var resources: Resources? = null

    /** Whether your plugin has resources that need to be loaded  */
    @JvmField
    var needsResources = false

    /** The filename of your plugin  */
    @JvmField
    var __filename: String? = null

    /** The [CommandsAPI] of your plugin. You can register/unregister commands here  */
    @JvmField
    protected var commands = CommandsAPI(name)

    /** The [Logger] of your plugin. Use this to log information  */
    @JvmField
    var logger: Logger? = null

    /** The [PatcherAPI] of your plugin. You can add/remove patches here  */
    @JvmField
    protected var patcher = PatcherAPI(name)
    private var manifest: Manifest? = null

    /** Method returning the [Manifest] of your Plugin  */
    open fun getManifest() = manifest

    /**
     * Initializes the plugin with a manifest, you shouldn't be calling this manually
     * @throws IllegalStateException If the method was called more than once
     */
    fun initialize(manifest: Manifest) {
        check(this.manifest == null) { "This plugin was already initialized" }
        this.manifest = manifest
        logger = Logger(manifest.name)
    }

    /**
     * Returns whether the user will be prompted to restart after enabling/disabling.
     * @return [AliucordPlugin.requiresRestart]
     */
    fun requiresRestart(): Boolean {
        val annotation = annotation
        return annotation != null && annotation.requiresRestart
    }

    /** Returns the [AliucordPlugin] annotation if exists */
    val annotation: AliucordPlugin?
        get() = javaClass.getAnnotation(AliucordPlugin::class.java)

    /**
     * Called when your Plugin is loaded
     * @param context Context
     */
    @Throws(Throwable::class)
    open fun load(context: Context) { }

    /**
     * Called when your Plugin is unloaded
     * @param context Context
     */
    @Throws(Throwable::class)
    open fun unload(context: Context) { } // not used now

    /**
     * Called when your Plugin is started
     * @param context Context
     */
    @Throws(Throwable::class)
    open fun start(context: Context) { }

    /**
     * Called when your Plugin is stopped
     * @param context Context
     */
    @Throws(Throwable::class)
    open fun stop(context: Context) { }

    fun getName() = manifest!!.name!!

    /** Plugin Manifest  */
    class Manifest {
        @JvmField
        var name: String? = null

        @JvmField
        var pluginClassName: String? = null

        /** The authors of this plugin  */
        @JvmField
        var authors = arrayOf<Author>()

        /** A short description of this plugin  */
        @JvmField
        var description = ""

        /** The current version of this plugin  */
        @JvmField
        var version = "1.0.0"

        /** The updater JSON url  */
        @JvmField
        var updateUrl: String? = null
        // TODO: public String discord;
        /** Changelog featuring recent updates, written in markdown  */
        @JvmField
        var changelog: String? = null

        /** Image or video link that will be displayed at the top of the changelog  */
        @JvmField
        var changelogMedia: String? = null

        /** Plugin Author
         * @property[name] The name of the author
         * @property[id]   The id of the author
         * @constructor Constructs an Author with the specified name and ID
         */
        class Author(
            @JvmField
            val name: String,
            @JvmField
            val id: Long
        ) {

            /**
             * Constructs an Author with the specified name and an ID of 0
             * @param name The name of the author
             */
            constructor(name: String) : this(name, 0)

            override fun toString() = name
        }
    }

    /** Plugin SettingsTab  */
    class SettingsTab {
        /** The [Type] of this SettingsTab  */
        @JvmField
        var type: Type

        /** The Page fragment  */
        @JvmField
        var page: Class<AppFragment>? = null

        /** The BottomSheet component  */
        @JvmField
        var bottomSheet: Class<AppBottomSheet>? = null

        /** The arguments that will be passed to the constructor of the component  */
        @JvmField
        var args: Array<out Any>? = null

        /**
         * Creates a SettingsTab with a dedicated page
         * @param settings The settings page fragment
         */
        constructor(settings: Class<AppFragment>) {
            type = Type.PAGE
            page = settings
        }

        /**
         * Creates a SettingsTab of the specified type
         *
         * @param settings The component to use for this SettingsTab
         * @param type     The [Type] of this SettingsTab
         */
        @Suppress("UNCHECKED_CAST")
        constructor(settings: Class<Fragment>, type: Type) {
            this.type = type
            if (type == Type.PAGE) page = settings as Class<AppFragment>
            else bottomSheet = settings as Class<AppBottomSheet>
        }
        // TODO: public boolean addTab = false;
        /**
         * Sets the constructor args that will be passed to this SettingsTab
         *
         * @param args The arguments that should be passed
         */
        fun withArgs(vararg args: Any): SettingsTab {
            this.args = args
            return this
        }

        /** The type of this SettingsTab. PAGE is a dedicated page, BOTTOM_SHEET is a popup at the bottom of the screen.  */
        enum class Type {
            PAGE, BOTTOM_SHEET
        }

        interface SettingsPage {
            fun onViewBound(view: View?)
        }
    }
}
