/*
 * This file is part of Aliucord, an Android Discord client mod.
 * Copyright (c) 2021 Juby210 & Vendicated
 * Licensed under the Open Software License version 3.0
 */
package com.aliucord.settings

import android.annotation.SuppressLint
import android.os.Bundle
import android.text.SpannableStringBuilder
import android.view.Gravity
import android.view.View
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import com.aliucord.*
import com.aliucord.fragments.SettingsPage
import com.aliucord.utils.DimenUtils
import com.aliucord.utils.MDUtils
import com.aliucord.views.DangerButton
import com.aliucord.widgets.BottomSheet
import com.discord.views.CheckedSetting
import com.lytefast.flexinput.R
import java.io.File

const val autoDisableKey = "autoDisableCrashingPlugins"

data class CrashLog(val timestamp: String, val stacktrace: String, var times: Int, val timestampmilis: Long)

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

class Crashes : SettingsPage() {
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

        addHeaderButton("Open Crashlog Folder", R.e.ic_open_in_new_white_24dp) {
            if (!dir.exists() && !dir.mkdir()) {
                Utils.showToast("Failed to create crashlogs directory!", true)
            } else {
                Utils.launchFileExplorer(dir)
            }
            true
        }
        headerBar.menu.add("Clear Crashes")
            .setIcon(ContextCompat.getDrawable(context, R.e.ic_delete_24dp))
            .setEnabled(hasCrashes)
            .setOnMenuItemClickListener {
                files?.forEach { it.delete() }
                reRender()
                true
            }
        addHeaderButton("Settings", R.e.ic_settings_24dp) {
            CrashSettings().show(parentFragmentManager, "Crash Settings")
            true
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

    companion object {
        fun getCrashes(): Map<Int, CrashLog>? {
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
                        times = 0,
                        timestampmilis = file.lastModified()
                    )
                }.times++
            }
            return res
        }
    }
}
