package com.aliucord.coreplugins

import android.content.Context
import android.graphics.Typeface
import android.text.*
import android.text.style.AbsoluteSizeSpan
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.annotation.DrawableRes
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.Fragment
import com.aliucord.*
import com.aliucord.PluginManager.isSafeModeEnabled
import com.aliucord.Utils.getResId
import com.aliucord.Utils.isDebuggable
import com.aliucord.Utils.joinSupportServer
import com.aliucord.Utils.launchUrl
import com.aliucord.Utils.openPage
import com.aliucord.Utils.restartAliucord
import com.aliucord.entities.CorePlugin
import com.aliucord.fragments.ConfirmDialog
import com.aliucord.patcher.*
import com.aliucord.settings.*
import com.aliucord.utils.ChangelogUtils.FooterAction
import com.aliucord.utils.ReflectUtils
import com.aliucord.views.Divider
import com.aliucord.views.ToolbarButton
import com.discord.app.AppComponent
import com.discord.app.AppLog.LoggedItem
import com.discord.databinding.WidgetDebuggingAdapterItemBinding
import com.discord.databinding.WidgetGlobalStatusIndicatorBinding
import com.discord.models.domain.ModelPayload
import com.discord.stores.*
import com.discord.utilities.color.ColorCompat
import com.discord.utilities.user.UserUtils
import com.discord.widgets.changelog.WidgetChangeLog
import com.discord.widgets.debugging.WidgetDebugging
import com.discord.widgets.guilds.profile.WidgetChangeGuildIdentity
import com.discord.widgets.guilds.profile.`WidgetGuildProfileSheet$configureGuildActions$$inlined$apply$lambda$4`
import com.discord.widgets.home.WidgetHome
import com.discord.widgets.settings.WidgetSettings
import com.discord.widgets.settings.profile.WidgetEditUserOrGuildMemberProfile
import com.discord.widgets.status.*
import com.lytefast.flexinput.R

// Contains core features like the Aliucord section in settings, and other small features
internal class CoreBase : CorePlugin(Manifest("CoreBase")) {
    override val isHidden = true
    override val isRequired = true

    override fun start(context: Context) {
        // Adds Aliucord section in settings with links to plugins, settings, etc
        patcher.after<WidgetSettings>("onViewBound", View::class.java) { (param, root: ViewGroup) ->
            val layout = Utils.nestedChildAt<ViewGroup>(root, 1, 0)
            var baseIndex = layout.indexOfChild(layout.findViewById(getResId("developer_options_divider", "id")))
            val context = layout.context

            layout.addView(Divider(context), baseIndex++)

            val header = TextView(context, null, 0, R.i.UiKit_Settings_Item_Header).apply {
                text = "Aliucord"
                setTypeface(ResourcesCompat.getFont(context, Constants.Fonts.whitney_semibold))
            }
            layout.addView(header, baseIndex++)

            val font = ResourcesCompat.getFont(context, Constants.Fonts.whitney_medium)!!

            layout.addView(
                makeSettingsEntry(context, font, "Settings", R.e.ic_behavior_24dp, AliucordPage::class.java),
                baseIndex++
            )
            layout.addView(
                makeSettingsEntry(context, font, "Plugins", R.e.ic_clear_all_white_24dp, Plugins::class.java),
                baseIndex++
            )
            layout.addView(
                makeSettingsEntry(context, font, "Updater", R.e.ic_file_download_white_24dp, Updater::class.java),
                baseIndex++
            )
            layout.addView(
                makeSettingsEntry(context, font, "Crashes", R.e.ic_history_white_24dp, Crashes::class.java),
                baseIndex++
            )
            layout.addView(
                makeSettingsEntry(context, font, "Open Debug Log", R.e.ic_audit_logs_24dp, WidgetDebugging::class.java),
                baseIndex
            )

            val versionView = layout.findViewById<TextView>(getResId("app_info_header", "id"))!!

            versionView.text = buildString {
                append(versionView.text.toString() + " | Aliucord " + BuildConfig.VERSION)
                if (!BuildConfig.RELEASE) append(" (Custom)")
                if (isDebuggable) append(" [DEBUGGABLE]")
            }

            layout.findViewById<TextView>(getResId("upload_debug_logs", "id"))!!.apply {
                text = "Aliucord Support Server"
                setOnClickListener { v -> joinSupportServer(v.context) }
            }

            // Remove Discord changelog button
            layout.findViewById<TextView>(getResId("changelog", "id")).apply {
                visibility = View.GONE
            }
        }

        // Add permanent indicator if safe mode is enabled.
        patcher.before<WidgetGlobalStatusIndicator>(
            "configureUI",
            WidgetGlobalStatusIndicatorViewModel.ViewState::class.java,
        ) { param ->
            if (!isSafeModeEnabled()) return@before

            val indicator = param.thisObject as WidgetGlobalStatusIndicator
            val context = indicator.requireContext()

            val binding = ReflectUtils.invokeMethod(indicator, "getBinding") as WidgetGlobalStatusIndicatorBinding
            val indicatorState = ReflectUtils.getField(indicator, "indicatorState") as WidgetGlobalStatusIndicatorState

            val backgroundColor = getResId("colorBackgroundTertiary", "attr")
            val textColor = getResId("colorHeaderPrimary", "attr")

            // Layout
            binding.c.apply {
                setBackgroundColor(ColorCompat.getThemedColor(context, backgroundColor))
                setOnClickListener { safeModeDialog(indicator) }
                visibility = View.VISIBLE
            }

            // Indicator text
            binding.i.apply {
                setTextColor(ColorCompat.getThemedColor(context, textColor))
                text = "Safe mode enabled"
            }

            Utils.mainThread.post { indicatorState.updateState(true, false, false) }

            param.result = null
        }

        // Patch to allow changelogs without media
        patcher.before<WidgetChangeLog>("configureMedia", String::class.java) { param ->
            val media = mostRecentIntent.getStringExtra("INTENT_EXTRA_VIDEO")

            if (media != null) return@before

            val binding = WidgetChangeLog.`access$getBinding$p`(this)
            binding.i.visibility = View.GONE // changeLogVideoOverlay
            binding.h.visibility = View.GONE // changeLogVideo

            param.result = null
        }

        // Patch for custom footer actions
        patcher.before<WidgetChangeLog>("configureFooter") { param ->
            @Suppress("deprecation")
            val actions = mostRecentIntent.getParcelableArrayExtra("INTENT_EXTRA_FOOTER_ACTIONS") ?: return@before

            val binding = WidgetChangeLog.`access$getBinding$p`(this)
            val twitterButton = binding.g
            val parent = twitterButton.parent as LinearLayout

            parent.removeAllViewsInLayout()

            actions.forEach { action ->
                val button = ToolbarButton(parent.context).apply {
                    val action = action as FooterAction

                    setImageDrawable(ContextCompat.getDrawable(parent.context, action.drawableResourceId), false)
                    setPadding(twitterButton.paddingLeft, twitterButton.paddingTop, twitterButton.paddingRight, twitterButton.paddingBottom)
                    setLayoutParams(twitterButton.layoutParams)
                    setOnClickListener { launchUrl(action.url) }
                }

                parent.addView(button)
            }

            param.result = null
        }

        // add stacktraces in debug logs page
        val c = WidgetDebugging.Adapter.Item::class.java
        val debugItemBinding = c.getDeclaredField("binding").apply {
            isAccessible = true
        }

        patcher.after<WidgetDebugging.Adapter.Item>(
            "onConfigure",
            Int::class.javaPrimitiveType!!,
            LoggedItem::class.java
        ) { (param, _: Any, loggedItem: LoggedItem) ->
            val th = loggedItem.m ?: return@after

            val logMessage = (debugItemBinding[param.thisObject] as WidgetDebuggingAdapterItemBinding).b
            val sb = SpannableStringBuilder("\n  at ").apply {
                val s = th.stackTrace
                append(TextUtils.join("\n  at ", if (s.size > 12) s.copyOfRange(0, 12) else s))
                setSpan(AbsoluteSizeSpan(12, true), 0, length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
            }
            logMessage.append(sb)
        }

        // use new member profile editor for nitro users
        patcher.instead<`WidgetGuildProfileSheet$configureGuildActions$$inlined$apply$lambda$4`>("invoke", View::class.java) { (param, layout: View) ->
            val ctx = layout.context
            val guildId = (param.thisObject as `WidgetGuildProfileSheet$configureGuildActions$$inlined$apply$lambda$4`).`$guildId$inlined`

            if (UserUtils.INSTANCE.isPremiumTier2(StoreStream.getUsers().me)) {
                (WidgetEditUserOrGuildMemberProfile.Companion)!!.launch(ctx, null, guildId)
            } else {
                (WidgetChangeGuildIdentity.Companion)!!.launch(guildId, "Guild Bottom Sheet", ctx)
            }

            null
        }

        // Disable the Discord changelog page
        patcher.instead<StoreChangeLog>(
            "shouldShowChangelog",
            Context::class.java, Long::class.javaPrimitiveType!!, String::class.java, Int::class.javaObjectType
        ) { false }

        // Disable the google play rating request dialog
        patcher.instead<StoreReviewRequest>("handleConnectionOpen", ModelPayload::class.java) { null }

        // Disable school hubs dialog upon login
        patcher.before<StoreNotices>("hasBeenSeen", String::class.java) { param ->
            if (param.args[0] == "WidgetHubEmailFlow") param.result = true
        }

        // Disable email upsell
        patcher.instead<WidgetHome>("maybeShowHubEmailUpsell") { null }
    }

    private fun makeSettingsEntry(
        context: Context,
        font: Typeface,
        text: String,
        @DrawableRes resId: Int,
        component: Class<out AppComponent>
    ) = TextView(context, null, 0, R.i.UiKit_Settings_Item_Icon).apply {
        this.text = text
        this.typeface = font

        val icon = ContextCompat.getDrawable(context, resId)?.also { icon ->
            icon.mutate().setTint(ColorCompat.getThemedColor(context, R.b.colorInteractiveNormal))
        }

        setCompoundDrawablesRelativeWithIntrinsicBounds(icon, null, null, null)
        setOnClickListener { v -> openPage(v.context, component) }
    }

    private fun safeModeDialog(fragment: Fragment) {
        val desc = """
            You are currently in safe mode. Plugins are disabled.

            Press OK to exit safe mode and restart Aliucord.

            """.trimIndent()
        ConfirmDialog()
            .setTitle("Safe Mode")
            .setDescription(desc)
            .setOnOkListener {
                Main.settings.setBool(ALIUCORD_SAFE_MODE_KEY, false)
                restartAliucord(fragment.requireContext())
            }
            .show(fragment.getParentFragmentManager(), "Disable Safe Mode")
    }
}
