package com.aliucord.screens

import android.graphics.Color
import android.os.Bundle
import android.view.*
import android.widget.TextView
import android.widget.Toast
import com.aliucord.Utils
import com.aliucord.fragments.SettingsPage
import com.aliucord.settings.AliucordPage
import com.aliucord.updater.CoreUpdater
import com.aliucord.updater.PluginUpdater
import com.aliucord.utils.DimenUtils
import com.aliucord.utils.ViewUtils.addTo
import com.aliucord.widgets.UpdaterPluginCard
import com.google.android.material.snackbar.Snackbar
import com.lytefast.flexinput.R

internal class UpdaterScreen : SettingsPage() {
    private val updates = mutableListOf<PluginUpdater.PluginUpdate>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (!CoreUpdater.isUpdaterDisabled()) {
            setActionBarSubtitle("Checking for updates...")
            refreshUpdates()
        }
    }

    override fun onViewBound(view: View) {
        val context = view.context

        super.onViewBound(view)
        setActionBarTitle("Updater")

        addHeaderButton("Refresh", R.e.ic_refresh_white_a60_24dp) { item ->
            item.isEnabled = false
            setActionBarSubtitle("Checking for updates...")
            refreshUpdates()
            true
        }

        addHeaderButton("Update All", R.e.ic_file_download_white_24dp) { item: MenuItem ->
            item.isEnabled = false
            setActionBarSubtitle("Updating...")
            updateAll()
            true
        }

        val noticeText = when {
            CoreUpdater.isUpdaterDisabled() -> "All update checks have been manually disabled."
            CoreUpdater.isCustomCoreLoaded() -> "Core updates are disabled due to using a custom Aliucord core."
            else -> null
        }
        if (noticeText != null) {
            Snackbar.make(getLinearLayout(), noticeText, Snackbar.LENGTH_INDEFINITE)
                .setAction("Settings") { Utils.openPage(it.context, AliucordPage::class.java) }
                .setBackgroundTint(context.getColor(android.R.color.holo_orange_light))
                .setTextColor(Color.BLACK)
                .setActionTextColor(Color.BLACK)
                .show()
        }

        if (updates.isEmpty()) {
            TextView(context, null, 0, R.i.UiKit_Settings_Item_SubText).apply {
                text = "No updates found!"
                setPadding(DimenUtils.defaultPadding)
                setGravity(Gravity.CENTER)
            }.addTo(linearLayout)
        } else {
            for (update in updates) {
                addView(UpdaterPluginCard(context, update, this::reRender))
            }
        }
    }

    private fun refreshUpdates() {
        Utils.threadPool.execute {
            // FIXME: notifications aren't shown on updater screen, make this a card instead
            CoreUpdater.checkForUpdates()
        }

        Utils.threadPool.execute {
            updates.clear()
            updates.addAll(PluginUpdater.fetchUpdates())

            val noticeText = if (updates.isNotEmpty()) {
                "Found ${updates.size} plugin updates!"
            } else {
                "No plugin updates found!"
            }

            Utils.mainThread.post {
                setActionBarSubtitle(null)
                Toast.makeText(context, noticeText, Toast.LENGTH_SHORT).show()
                this.reRender()
            }
        }
    }

    private fun updateAll() {
        val updates = updates.toList()
        if (updates.isEmpty()) {
            setActionBarSubtitle(null)
            Toast.makeText(context, "No plugin updates found!", Toast.LENGTH_LONG).show()
            return
        }

        Utils.threadPool.execute {
            val (succeeded, failed) = updates
                .filter { it.isUpdatePossible() }
                .partition(PluginUpdater::updatePlugin)

            val noticeText = if (failed.isEmpty()) {
                "Successfully updated ${succeeded.size} plugins!"
            } else {
                "Failed to update ${failed.size} plugins!"
            }

            Utils.mainThread.post {
                setActionBarSubtitle(null)
                Toast.makeText(context, noticeText, Toast.LENGTH_SHORT).show()
            }
            refreshUpdates()
        }
    }
}
