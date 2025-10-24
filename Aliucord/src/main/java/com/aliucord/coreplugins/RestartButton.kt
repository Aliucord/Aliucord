package com.aliucord.coreplugins

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.view.MenuItem
import androidx.core.content.ContextCompat
import com.aliucord.entities.CorePlugin
import com.aliucord.patcher.after
import com.discord.utilities.color.ColorCompat
import com.discord.widgets.settings.WidgetSettings
import com.lytefast.flexinput.R

internal class RestartButton : CorePlugin(Manifest("RestartButton")) {
    init {
        manifest.description = "Adds a restart button for Aliucord to the settings page"
    }

    override fun start(context: Context) {
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
                    restartApp(activity)
                    false
                }
        }
    }

    override fun stop(context: Context) = patcher.unpatchAll()

    private fun restartApp(activity: Activity) {
        val intent = activity.packageManager.getLaunchIntentForPackage(activity.packageName)
            .let { Intent.makeRestartActivityTask(it?.component) }

        activity.startActivity(intent)
        Runtime.getRuntime().exit(0)
    }
}
