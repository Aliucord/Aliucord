package com.aliucord.coreplugins

import android.content.Context
import android.view.MenuItem
import androidx.core.content.ContextCompat
import com.aliucord.Constants
import com.aliucord.Utils
import com.aliucord.entities.CorePlugin
import com.aliucord.patcher.after
import com.discord.utilities.color.ColorCompat
import com.discord.widgets.settings.WidgetSettings
import com.lytefast.flexinput.R
import java.io.File
import java.io.InputStreamReader
import java.text.SimpleDateFormat
import java.util.*

internal class RestartButton : CorePlugin(Manifest("RestartButton")) {
    init {
        manifest.description = "Adds a restart button for Aliucord to the settings page"
    }

    var logs = ArrayDeque<String>(10000)
    override fun start(context: Context) {
        val dateFormat = SimpleDateFormat("yyyyMMdd_HHmmssSSS", Locale.getDefault())
        val time = dateFormat.format(Date())
        val process = Runtime.getRuntime().exec(arrayOf("logcat"))
        Utils.threadPool.submit {
            val reader = InputStreamReader(process.inputStream).buffered()
            while (true) {
                val nextLine = reader.readLine() ?: break
                logs.add(nextLine)
                if (logs.size > 10000) {
                    logs.removeFirst()
                }
            }
        }

        patcher.after<WidgetSettings>("configureToolbar") {
            val activity = this.requireAppActivity()
            val toolbar = activity.u

            val icon = ContextCompat.getDrawable(activity, com.yalantis.ucrop.R.c.ucrop_rotate)!!
                .mutate()
                .apply { setTint(ColorCompat.getThemedColor(activity, R.b.colorInteractiveNormal)) }

            toolbar.menu.add("Restart")
                .setIcon(icon)
                .setShowAsActionFlags(MenuItem.SHOW_AS_ACTION_ALWAYS)
                .setOnMenuItemClickListener {
                    Utils.restartAliucord(activity)
                    false
                }

            val logIcon = ContextCompat.getDrawable(activity, R.e.ic_audit_logs_24dp)!!
                .mutate()
                .apply { setTint(ColorCompat.getThemedColor(activity, R.b.colorInteractiveNormal)) }

            toolbar.menu.add("Dump logcat")
                .setIcon(logIcon)
                .setShowAsActionFlags(MenuItem.SHOW_AS_ACTION_ALWAYS)
                .setOnMenuItemClickListener {
                    val dir = File(Constants.BASE_PATH, "logs")
                    val target = File(dir, "${time}.log")

                    if (!dir.exists() && !dir.mkdir()) {
                        Utils.showToast("Failed to create logs directory!", true)
                    } else {
                        target.writeText(logs.joinToString("\n"))
                        Utils.showToast("Written to ${target.absolutePath}", true)
                    }

                    false
                }
        }
    }

    override fun stop(context: Context) = patcher.unpatchAll()
}
