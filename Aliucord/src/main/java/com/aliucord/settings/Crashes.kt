/*
 * This file is part of Aliucord, an Android Discord client mod.
 * Copyright (c) 2021 Juby210 & Vendicated
 * Licensed under the Open Software License version 3.0
 */
package com.aliucord.settings

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.text.SpannableStringBuilder
import android.view.Gravity
import android.view.View
import android.widget.TextView
import androidx.annotation.DrawableRes
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import com.aliucord.*
import com.aliucord.fragments.Page
import com.aliucord.utils.DimenUtils
import com.aliucord.utils.MDUtils
import com.aliucord.views.DangerButton
import com.aliucord.views.ToolbarButton
import com.aliucord.widgets.BottomSheet
import com.discord.views.CheckedSetting
import com.lytefast.flexinput.R
import java.io.File
import java.util.*

const val autoDisableKey = "autoDisableCrashingPlugins"
private data class CrashLog(val timestamp: String, val stacktrace: String, var times: Int)

private val uniqueId = View.generateViewId()

class CrashSettings : BottomSheet() {
    override fun onViewCreated(view: View, bundle: Bundle?) {
        super.onViewCreated(view, bundle)

        Utils.createCheckedSetting(
                view.context,
                CheckedSetting.ViewType.SWITCH,
                "Auto Disable Plugins",
                "Automatically disable plugins when they cause a crash"
        ).run {
            isChecked = SettingsUtils.getBool(autoDisableKey, true)
            setOnCheckedListener {
                SettingsUtils.setBool(autoDisableKey, it)
            }

            linearLayout.addView(this)
        }
    }
}

class Crashes : Page() {
    private fun createToolbarButton(ctx: Context, @DrawableRes drawableId: Int, marginEnd: Int, onClickListener: View.OnClickListener) =
        ToolbarButton(ctx).apply {
            layoutParams = Toolbar.LayoutParams(Toolbar.LayoutParams.WRAP_CONTENT, Toolbar.LayoutParams.WRAP_CONTENT).apply {
                gravity = Gravity.END
                val m = DimenUtils.defaultPadding / 2
                setMargins(m, m, m + marginEnd, m)
            }

            setImageDrawable(ContextCompat.getDrawable(ctx, drawableId))
            setOnClickListener(onClickListener)
        }

    @SuppressLint("SetTextI18n")
    override fun onViewBound(view: View) {
        super.onViewBound(view)
        setActionBarTitle("Crash Logs")

        val context = view.context
        val padding = DimenUtils.defaultPadding
        val p = padding / 2

        val dir = File(Constants.CRASHLOGS_PATH)
        val files = dir.listFiles()
        val hasCrashes = files != null && files.isNotEmpty()

        if (headerBar.findViewById<View?>(uniqueId) == null) {
            createToolbarButton(context, R.e.ic_settings_24dp, p) {
                CrashSettings().show(parentFragmentManager, "Crash Settings")
            }.run {
                id = uniqueId
                addHeaderButton(this)
            }

            createToolbarButton(context, R.e.ic_open_in_new_white_24dp, 0) {
                if (!dir.exists() && !dir.mkdir()) {
                    Utils.showToast("Failed to create crashlogs directory!", true)
                } else {
                    Utils.launchFileExplorer(dir)
                }
            }.run {
                addHeaderButton(this)
            }

            createToolbarButton(context, R.e.ic_delete_white_24dp, 0) { v ->
                files?.forEach { it.delete() }
                v.alpha = 0.5f
                v.isClickable = false
                reRender()
            }.run {
                alpha = if (hasCrashes) 1f else 0.5f
                isClickable = hasCrashes
                addHeaderButton(this)
            }
        }

        val crashes = getCrashes()
        if (crashes == null || crashes.isEmpty()) {
            TextView(context, null, 0, R.i.UiKit_Settings_Item_Header).run {
                isAllCaps = false
                text = "Woah, no crashes :O"
                typeface = ResourcesCompat.getFont(context, Constants.Fonts.whitney_semibold)
                gravity = Gravity.CENTER

                linearLayout.addView(this)
            }
            DangerButton(context).run {
                text = "LET'S CHANGE THAT"
                setPadding(p, p, p, p)
                setOnClickListener {
                    throw RuntimeException("You fool...")
                }
                linearLayout.addView(this)
            }
        } else {
            TextView(context, null, 0, R.i.UiKit_Settings_Item_SubText).run {
                text = "Hint: Crashlogs are accesible via your file explorer at Aliucord/crashlogs"
                typeface = ResourcesCompat.getFont(context, Constants.Fonts.whitney_medium)
                gravity = Gravity.CENTER
                linearLayout.addView(this)
            }
            crashes.values.forEach { (timestamp, stacktrace, times) ->
                TextView(context, null, 0, R.i.UiKit_Settings_Item_Header).run {
                    var title = timestamp
                    if (times > 1)
                        title += " ($times)"
                    text = title
                    typeface = ResourcesCompat.getFont(context, Constants.Fonts.whitney_semibold)
                    linearLayout.addView(this)
                }
                TextView(context).run {
                    text = MDUtils.renderCodeBlock(context, SpannableStringBuilder(), null, stacktrace)
                    setOnClickListener {
                        Utils.setClipboard("CrashLog-$timestamp", stacktrace)
                        Utils.showToast("Copied to clipboard")
                    }
                    linearLayout.addView(this)
                }
            }
        }
    }

    private fun getCrashes(): Map<Int, CrashLog>? {
        val folder = File(Constants.BASE_PATH, "crashlogs")
        val files = folder.listFiles()?.apply {
            sortByDescending { it.lastModified() }
        } ?: return null

        val res = LinkedHashMap<Int, CrashLog>()
        for (file in files) {
            if (!file.isFile) continue
            val content = file.readText()
            val hashCode = content.hashCode()
            res.computeIfAbsent(hashCode) {
                CrashLog(
                    timestamp = file.name.replace(".txt", "").replace("_".toRegex(), ":"),
                    stacktrace = content,
                    times = 0
                )
            }.times++
        }
        return res
    }
}
