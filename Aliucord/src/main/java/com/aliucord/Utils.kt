/*
 * This file is part of Aliucord, an Android Discord client mod.
 * Copyright (c) 2021 Juby210 & Vendicated
 * Licensed under the Open Software License version 3.0
 */

package com.aliucord

import android.annotation.SuppressLint
import android.content.*
import android.content.pm.ApplicationInfo
import android.content.res.Resources
import android.graphics.Typeface
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Handler
import android.os.Looper
import android.provider.DocumentsContract
import android.text.SpannableStringBuilder
import android.text.Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
import android.text.style.ClickableSpan
import android.text.style.StyleSpan
import android.view.*
import android.widget.FrameLayout
import android.widget.Toast
import androidx.annotation.AttrRes
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.Fragment
import b.a.d.j
import com.aliucord.fragments.AppFragmentProxy
import com.aliucord.fragments.ConfirmDialog
import com.aliucord.utils.DimenUtils.dp
import com.aliucord.utils.ReflectUtils
import com.discord.api.commands.ApplicationCommandType
import com.discord.api.commands.CommandChoice
import com.discord.api.message.attachment.MessageAttachment
import com.discord.api.user.User
import com.discord.app.AppActivity
import com.discord.app.AppComponent
import com.discord.models.commands.ApplicationCommandOption
import com.discord.nullserializable.NullSerializable
import com.discord.stores.StoreInviteSettings
import com.discord.stores.StoreStream
import com.discord.utilities.SnowflakeUtils
import com.discord.utilities.fcm.NotificationClient
import com.discord.views.CheckedSetting
import com.discord.widgets.chat.list.WidgetChatList
import com.discord.widgets.chat.list.adapter.WidgetChatListAdapterItemAttachment
import com.discord.widgets.guilds.invite.WidgetGuildInvite
import com.google.android.material.snackbar.Snackbar
import com.lytefast.flexinput.R
import java.io.File
import java.lang.reflect.Field
import java.util.*
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import kotlin.random.Random
import kotlin.system.exitProcess

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
     * Whether Aliucord is debuggable
     */
    @JvmStatic
    val isDebuggable
        get() = appContext.applicationInfo.flags and ApplicationInfo.FLAG_DEBUGGABLE != 0

    /**
     * Instance of WidgetChatList. Use this instead of patching it's constructor and storing it.
     */
    @JvmField
    var widgetChatList: WidgetChatList? = null

    /**
     * Launches an URL in the user's preferred Browser
     * @param url The url to launch
     */
    @JvmStatic
    fun launchUrl(url: String) {
        launchUrl(Uri.parse(url))
    }

    /**
     * Launches an URL in the user's preferred Browser
     * @param url The url to launch
     */
    @JvmStatic
    fun launchUrl(url: Uri) {
        appActivity.startActivity(Intent(Intent.ACTION_VIEW).setData(url))
    }

    /**
     * Prompt to join the Aliucord support server
     *
     * @param ctx Context
     */
    @JvmStatic
    fun joinSupportServer(ctx: Context) {
        WidgetGuildInvite.Companion!!.launch(ctx, StoreInviteSettings.InviteCode(Constants.ALIUCORD_SUPPORT, "", null))
    }

    /**
     * Get a drawable by attribute
     *
     * @param context Context
     * @param attr The attribute id, e.g. R.b.ic_navigate_next
     * @return Resolved drawable
     */
    @JvmStatic
    @Throws(Resources.NotFoundException::class)
    fun getDrawableByAttr(context: Context, @AttrRes attr: Int): Drawable {
        val attrs = context.theme.obtainStyledAttributes(intArrayOf(attr))
        val id = attrs.getResourceId(0, 0)
        attrs.recycle()
        return ContextCompat.getDrawable(context, id)
            ?: throw Resources.NotFoundException("Resource ID #0x" + attr.toString(16))
    }

    /**
     * Nested childAt. Used to turn nightmares like
     * ```kt
     * val layout = ((v.getChildAt(1) as ViewGroup).getChildAt(0) as ViewGroup).getChildAt(1) as LinearLayout
     * ```
     * into the much nicer
     * ```kt
     * val layout = Utils.nestedChildAt<LinearLayout>(v, 1, 0, 1)
     * ```
     * @param root The root that holds the children
     * @param indices Indices of the children. They will be done in order
     * @return Child at the specified nested index
     */
    @JvmStatic
    @Suppress("UNCHECKED_CAST")
    fun <R> nestedChildAt(root: ViewGroup, vararg indices: Int) = indices.fold(root as View) { last, curr ->
        (last as ViewGroup).getChildAt(curr) as View
    } as R

    /**
     * Launches the file explorer in the specified folder.
     * May not work on all Roms, will show an error with advice in that case.
     *
     * @param folder The folder to launch
     * @throws IllegalArgumentException If [folder] does not exist or is not a directory.
     */
    @JvmStatic
    @Throws(IllegalArgumentException::class)
    fun launchFileExplorer(folder: File) {
        val path = folder.absolutePath
        if (!folder.exists()) throw IllegalArgumentException("No such folder: $path")
        if (!folder.isDirectory) throw IllegalArgumentException("Not a folder: $path")

        val uri = Uri.parse(path)
        val intent = Intent(Intent.ACTION_VIEW)
            .setDataAndType(uri, "resource/folder")

        // TODO: Do we need to add query permission to AndroidManifest? I tried on Android 11 and it resolved MiXplorer correctly
        @SuppressLint("QueryPermissionsNeeded")
        if (intent.resolveActivityInfo(appActivity.packageManager, 0) == null) {
            val text =
                """
You don't have a file explorer installed that can handle this action.

Consider installing the MiXplorer file manager, or navigate to $path manually using your file explorer.
"""

            val ssb = SpannableStringBuilder(text).apply {
                val mixText = "the MiXplorer file manager"
                var start = text.indexOf(mixText)
                var end = start + mixText.length
                setSpan(object : ClickableSpan() {
                    override fun onClick(widget: View) {
                        launchUrl("https://forum.xda-developers.com/t/app-2-2-mixplorer-v6-x-released-fully-featured-file-manager.1523691/")
                    }
                }, start, end, SPAN_EXCLUSIVE_EXCLUSIVE)

                start = text.indexOf(path)
                end = start + path.length
                setSpan(StyleSpan(Typeface.BOLD_ITALIC), start, end, SPAN_EXCLUSIVE_EXCLUSIVE)

                val explorerText = "your file explorer"
                start = text.indexOf(explorerText)
                end = start + explorerText.length
                setSpan(object : ClickableSpan() {
                    override fun onClick(widget: View) {
                        Intent(Intent.ACTION_VIEW)
                            .setDataAndType(uri, DocumentsContract.Document.MIME_TYPE_DIR)
                            .let {
                                appActivity.startActivity(Intent.createChooser(it, "Open folder"))
                            }
                    }
                }, start, end, SPAN_EXCLUSIVE_EXCLUSIVE)
            }
            ConfirmDialog()
                .setTitle(":(")
                .setDescription(ssb)
                .show(appActivity.supportFragmentManager, "Open Folder")
        } else {
            appActivity.startActivity(Intent.createChooser(intent, "Open folder"))
        }
    }

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
    fun pluralise(amount: Int, noun: String) = "$amount $noun${if (amount != 1) "s" else ""}"

    /**
     * Send a toast from any [Thread]
     * @param message Message to show.
     * @param showLonger Whether to show toast for an extended period of time.
     */
    @Suppress("deprecation")
    @JvmOverloads
    @JvmStatic
    fun showToast(message: String, showLonger: Boolean = false) {
        mainThread.post {
            Toast.makeText(
                appContext,
                message,
                if (showLonger) Toast.LENGTH_LONG else Toast.LENGTH_SHORT
            ).show()
        }
    }

    /**
     * Send a toast from any [Thread]
     * @param ctx [Context]
     * @param message Message to show.
     * @param showLonger Whether to show toast for an extended period of time.
     */
    @Deprecated(
        "Use {@link #showToast(String, boolean)}",
        ReplaceWith("showToast(message, showLonger)")
    )
    @JvmOverloads
    @JvmStatic
    @Suppress("unused")
    fun showToast(_ctx: Context, message: String, showLonger: Boolean = false) {
        showToast(message, showLonger)
    }

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
    fun openPage(context: Context, clazz: Class<out AppComponent>, intent: Intent?) {
        j.d(context, clazz, intent)
    }

    @JvmStatic
    fun openPage(context: Context, clazz: Class<out AppComponent>) {
        openPage(context, clazz, null)
    }

    @JvmStatic
    fun openPageWithProxy(context: Context, fragment: Fragment) {
        SnowflakeUtils.fromTimestamp(System.currentTimeMillis() * 100).toString().let {
            AppFragmentProxy.fragments[it] = fragment
            openPage(context, AppFragmentProxy::class.java, Intent().putExtra("AC_FRAGMENT_ID", it))
        }
    }

    /**
     * Creates a CommandChoice that can be used inside Command args
     * @param name The name of the choice
     * @param value The value representing this choice
     * @return CommandChoice
     */
    @JvmStatic
    fun createCommandChoice(name: String, value: String) = CommandChoice(name, value)


    // kept for compatibility
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
    ) = createCommandOption(
        type,
        name,
        description,
        descriptionRes,
        required,
        default,
        channelTypes,
        choices,
        subCommandOptions,
        autocomplete,
        null
    )

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
     * @param minValue minValue for number type options
     * @param maxValue maxValue for number type options
     */
    @JvmStatic
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
        minValue: Number? = null,
        maxValue: Number? = null,
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
        autocomplete,
        minValue,
        maxValue
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
            null,
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
            null,
            null,
            0
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

        l.a().run {
            textSize = 16.0f
            typeface = ResourcesCompat.getFont(context, Constants.Fonts.whitney_medium)
            this.text = text
        }

        setSubtext(subtext)
        l.b().run {
            setPadding(0, paddingTop, paddingRight, paddingBottom)
        }
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
    fun tintToTheme(drawable: Drawable?) =
        drawable?.apply {
            // This should instead be setTint(ColorCompat.getThemedColor(context, R.b.colorInteractiveNormal)) but Themer plugin
            // doesn't support attributes. The below code is the equivalent
            val colorName =
                if (StoreStream.getUserSettingsSystem().theme == "light")
                    R.c.primary_light_600
                else
                    R.c.primary_dark_300
            setTint(ContextCompat.getColor(appContext, colorName))
        }

    /**
     * Logs a message on debug level.
     * @param msg Message to log.
     */
    @JvmStatic
    @Deprecated(
        "Please stop abusing this method to log random junk then forget about it. It does not tell which plugin is logging stuff, so it is very hard to debug. Make your own Logger instance"
    )
    fun log(msg: String) = Main.logger.debug(msg)

    private val fileNameField: Field = MessageAttachment::class.java.getDeclaredField("filename").apply { isAccessible = true }
    private val idField: Field = MessageAttachment::class.java.getDeclaredField("id").apply { isAccessible = true }
    private val urlField: Field = MessageAttachment::class.java.getDeclaredField("url").apply { isAccessible = true }
    private val proxyUrlField: Field = MessageAttachment::class.java.getDeclaredField("proxyUrl").apply { isAccessible = true }

    @JvmStatic
    fun openMediaViewer(url: String, filename: String) {
        val attachment = ReflectUtils.allocateInstance(MessageAttachment::class.java)

        try {
            fileNameField.set(attachment, filename)
            idField.set(attachment, SnowflakeUtils.fromTimestamp(System.currentTimeMillis()))
            urlField.set(attachment, url)
            proxyUrlField.set(attachment, url)
        } catch (th: Throwable) {
            error(th)
        }

        WidgetChatListAdapterItemAttachment.Companion.`access$navigateToAttachment`(
            WidgetChatListAdapterItemAttachment.Companion,
            appActivity,
            attachment
        )
    }

    /**
     * Prompts the user to restart Aliucord
     *
     * @param msg Message
     * @param position position, see [Gravity]
     */
    @SuppressLint("ShowToast", "InternalInsetResource")
    @JvmStatic
    @JvmOverloads
    fun promptRestart(msg: String = "Restart required. Restart now?", position: Int = Gravity.TOP) {
        val resources = appContext.resources
        val id = resources.getIdentifier("status_bar_height", "dimen", "android")
        val statusBarHeight = if (id > 0) resources.getDimensionPixelSize(id) else 0

        val view = appActivity.findViewById<View>(android.R.id.content)
        val bar = try {
            Snackbar.make(view, msg, Snackbar.LENGTH_INDEFINITE)
        } catch (e: Throwable) {
            Main.logger.errorToast("Failed to show SnackBar", e)
            return
        }

        bar.view.layoutParams = (bar.view.layoutParams as FrameLayout.LayoutParams).apply {
            topMargin = statusBarHeight + 4.dp
            gravity = position
        }

        bar.setAction("Restart") {
            val ctx = it.context
            val intent = ctx.packageManager.getLaunchIntentForPackage(ctx.packageName)
            appActivity.startActivity(Intent.makeRestartActivityTask(intent!!.component))
            exitProcess(0)
        }
        bar.show()
    }

    /**
     * Generates a current snowflake nonce similar to RN or Desktop. The default NonceGenerator uses time
     * in the future, which RN and Desktop do not do.
     *
     * @return Snowflake nonce with current timestamp.
     */
    @JvmStatic
    fun generateRNNonce() =
        SnowflakeUtils.fromTimestamp(System.currentTimeMillis()) + Random.nextBits(23)
}
