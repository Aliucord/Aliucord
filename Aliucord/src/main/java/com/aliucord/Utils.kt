/*
 * This file is part of Aliucord, an Android Discord client mod.
 * Copyright (c) 2021 Juby210 & Vendicated
 * Licensed under the Open Software License version 3.0
 */
package com.aliucord

import android.content.*
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Handler
import android.os.Looper
import android.view.View
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.Fragment
import c.a.d.j
import com.aliucord.fragments.AppFragmentProxy
import com.discord.api.commands.ApplicationCommandType
import com.discord.api.commands.CommandChoice
import com.discord.api.user.User
import com.discord.app.AppActivity
import com.discord.app.AppComponent
import com.discord.models.commands.ApplicationCommandOption
import com.discord.nullserializable.NullSerializable
import com.discord.stores.StoreStream
import com.discord.utilities.SnowflakeUtils
import com.discord.utilities.fcm.NotificationClient
import com.discord.views.CheckedSetting
import com.lytefast.flexinput.R
import java.util.*
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

/** Utility class that holds miscellaneous Utilities  */
object Utils {
    /** The main (UI) thread  */
    @JvmField
    val mainThread = Handler(Looper.getMainLooper())

    /**
     * ThreadPool. Please use this for asynchronous Tasks instead of creating Threads manually
     * as spinning up new Threads everytime is heavy on the CPU
     */
    @JvmField
    val threadPool = Executors.newCachedThreadPool() as ExecutorService

    @JvmStatic
    lateinit var appActivity: AppActivity

    private var mAppContext: Context? = null

    @JvmStatic
    val appContext: Context
        get() = mAppContext ?: NotificationClient.`access$getContext$p`(NotificationClient.INSTANCE)
            .also { mAppContext = it }


    /**
     * Launches an URL in the user's preferred Browser
     * @param url The url to launch
     */
    @JvmStatic
    fun launchUrl(url: String) =
        launchUrl(Uri.parse(url))

    /**
     * Launches an URL in the user's preferred Browser
     * @param url The url to launch
     */
    @JvmStatic
    fun launchUrl(url: Uri) = appActivity.startActivity(Intent(Intent.ACTION_VIEW).setData(url))

    /**
     * Sets the clipboard content
     * @param label User-visible label for the clip data
     * @param text The actual text
     */
    @JvmStatic
    fun setClipboard(label: CharSequence, text: CharSequence) {
        val clipboard = appContext.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        clipboard.setPrimaryClip(ClipData.newPlainText(label, text))
    }

    /**
     * Converts the singular term of the `noun` into plural.
     * @param amount Amount of the noun.
     * @param noun The noun
     * @return Pluralised `noun`
     */
    @JvmStatic
    fun pluralise(amount: Int, noun: String) = "$amount $noun${if (amount == 1) "s" else ""}"

    /**
     * Send a toast from any [Thread]
     * @param message Message to show.
     * @param showLonger Whether to show toast for an extended period of time.
     */
    @Suppress("deprecation")
    @JvmOverloads
    @JvmStatic
    fun showToast(message: String, showLonger: Boolean = false) = mainThread.post {
        Toast.makeText(
            appContext,
            message,
            if (showLonger) Toast.LENGTH_LONG else Toast.LENGTH_SHORT
        ).show()
    }

    /**
     * Send a toast from any [Thread]
     * @param ctx [Context]
     * @param message Message to show.
     * @param showLonger Whether to show toast for an extended period of time.
     */
    @Deprecated(
        "Use {@link #showToast(String, boolean)}", ReplaceWith(
            "showToast(message, showLonger)"
        )
    )
    @JvmOverloads
    @JvmStatic
    fun showToast(ctx: Context, message: String, showLonger: Boolean = false) =
        showToast(message, showLonger)

    private val resIdCache = HashMap<String, Int>()

    /**
     * Get resource id from discord package.
     * @param name Name of the resource.
     * @param type Type of the resource.
     * @return ID of the resource, or 0 if not found.
     */
    @JvmStatic
    fun getResId(name: String, type: String) = resIdCache.computeIfAbsent(name) { k ->
        appContext.resources.getIdentifier(
            k,
            type,
            "com.discord"
        )
    }

    @JvmStatic
    fun openPage(context: Context, clazz: Class<out AppComponent>, intent: Intent?) =
        j.d(context, clazz, intent)

    @JvmStatic
    fun openPage(context: Context, clazz: Class<out AppComponent>) = openPage(context, clazz, null)

    @JvmStatic
    fun openPageWithProxy(context: Context, fragment: Fragment) =
        SnowflakeUtils.fromTimestamp(System.currentTimeMillis() * 100).toString().let {
            AppFragmentProxy.fragments[it] = fragment
            openPage(context, AppFragmentProxy::class.java, Intent().putExtra("AC_FRAGMENT_ID", it))
        }

    /**
     * Creates a CommandChoice that can be used inside Command args
     * @param name The name of the choice
     * @param value The value representing this choice
     * @return CommandChoice
     */
    @JvmStatic
    fun createCommandChoice(name: String, value: String) = CommandChoice(name, value)


    /**
     * Creates a CommandOption that can be used for commands
     *
     * @param type The type of this argument
     * @param name The name of this argument
     * @param description The description of this argument
     * @param descriptionRes Optional ID of a string resource that will be used as description
     * @param required Whether this option is required
     * @param default Whether this option is the default selection (I think so at least I'm not 100% sure lol)
     * @param channelTypes Channel types this command is enabled in
     * @param choices List of choices the user may pick from
     * @param subCommandOptions List of command options if this argument is of [type] [ApplicationCommandType.SUBCOMMAND]
     * @param autocomplete Whether autocomplete is enabled
     */
    @JvmStatic
    @JvmOverloads
    fun createCommandOption(
        type: ApplicationCommandType = ApplicationCommandType.STRING,
        name: String,
        description: String? = null,
        descriptionRes: Int? = null,
        required: Boolean = false,
        default: Boolean = false,
        channelTypes: List<Int?> = emptyList(),
        choices: List<CommandChoice> = emptyList(),
        subCommandOptions: List<ApplicationCommandOption> = emptyList(),
        autocomplete: Boolean = false,
    ) = ApplicationCommandOption(
        type,
        name,
        description,
        descriptionRes,
        required,
        default,
        channelTypes,
        choices,
        subCommandOptions,
        autocomplete
    )

    /**
     * Builds Clyde User
     * @param name Name of user
     * @param avatarUrl Avatar URL of user
     * @return Built Clyde
     */
    @JvmStatic
    fun buildClyde(name: String?, avatarUrl: String?) =
        User(
            -1,
            name ?: "Clyde",
            NullSerializable.b(avatarUrl ?: Constants.Icons.CLYDE),
            NullSerializable.a(),
            "0000",
            0,
            null,
            true,
            false,
            null,
            null,
            false,
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            NullSerializable.a(),
            null
        )

    /**
     * Creates a checkable [View].
     * @param context [Context]
     * @param type [CheckedSetting.ViewType] of the checkable item.
     * @param text Title of the checkable item.
     * @param subtext Summary of the checkable item.
     * @return Checkable item.
     */
    @JvmStatic
    fun createCheckedSetting(
        context: Context,
        type: CheckedSetting.ViewType,
        text: CharSequence?,
        subtext: CharSequence?,
    ) = CheckedSetting(context, null).apply {
        if (type != CheckedSetting.ViewType.CHECK) {
            removeAllViews()
            f(type)
        }

        k.a().run {
            textSize = 16.0f
            typeface = ResourcesCompat.getFont(context, Constants.Fonts.whitney_medium)
            this.text = text
        }

        setSubtext(subtext)
        k.b().run {
            setPadding(0, paddingTop, paddingRight, paddingBottom)
        }
    }


    @Deprecated(
        "The old implementation didn't use the correct color and only tinted in dark mode. Use #tintToTheme(Context, Drawable)",
        ReplaceWith("tintToTheme(context, drawable)")
    )
    @JvmStatic
    fun tintToTheme(drawable: Drawable?) = drawable?.apply {
        if (StoreStream.getUserSettingsSystem().theme == "light") setTint(Color.BLACK)
    }

    /**
     * Tints a [Drawable] to match the user's current theme.
     * More specifically, tints the drawable to [R.c.primary_light_600] if the user is using light theme,
     * [R.c.primary_dark_300] otherwise
     *
     * Make sure you call [Drawable.mutate] first or the drawable will change in the entire app.
     * @param drawable Drawable
     * @return Drawable for chaining
     */
    @JvmStatic
    fun tintToTheme(context: Context, drawable: Drawable?) =
        drawable?.apply {
            // This should instead be setTint(ColorCompat.getThemedColor(context, R.b.colorInteractiveNormal)) but Themer plugin
            // doesn't support attributes. The below code is the equivalent
            val colorName =
                if (StoreStream.getUserSettingsSystem().theme == "light")
                    R.c.primary_light_600
                else
                    R.c.primary_dark_300
            setTint(ContextCompat.getColor(context, colorName))
        }

    /**
     * Logs a message on debug level.
     * @param msg Message to log.
     */
    @JvmStatic
    fun log(msg: String) = Main.logger.debug(msg)
}