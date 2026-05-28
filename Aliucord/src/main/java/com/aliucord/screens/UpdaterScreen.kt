package com.aliucord.screens

import android.graphics.Color
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.widget.*
import com.aliucord.Utils
import com.aliucord.fragments.SettingsPage
import com.aliucord.settings.AliucordPage
import com.aliucord.updater.*
import com.aliucord.utils.DimenUtils
import com.aliucord.utils.ViewUtils.addTo
import com.aliucord.widgets.UpdaterPluginCard
import com.google.android.material.snackbar.Snackbar
import com.lytefast.flexinput.R

internal class UpdaterScreen : SettingsPage() {
    companion object {
        var updates = mutableListOf<PluginUpdater.PluginUpdate>()
    }

    private val updateSource = PluginUpdaterSource()
    private var isRefreshing = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (!CoreUpdater.isUpdaterDisabled()) {
            setActionBarSubtitle("Checking for updates...")

            Utils.threadPool.execute {
                // FIXME: notifications aren't shown on updater screen, make this a card instead
                CoreUpdater.checkForUpdates()
            }

            if (updates.isEmpty()) refreshUpdates()
        }
    }

    override fun onViewBound(view: View) {
        val context = view.context

        super.onViewBound(view)
        setActionBarTitle("Updater")
        linearLayout.removeAllViews()

        if (updates.isNotEmpty()) {
            addHeaderButton("Update All", R.e.ic_file_download_white_24dp) { item ->
                item.isEnabled = false
                setActionBarSubtitle("Updating...")
                updateAll()
                true
            }
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

        if (isRefreshing) {
            ProgressBar(context).addTo(linearLayout)
            return
        }

        if (updates.isEmpty()) {
            TextView(context, null, 0, R.i.UiKit_Settings_Item_SubText).addTo(linearLayout) {
                text = "No updates found!"
                setPadding(DimenUtils.defaultPadding)
                setGravity(Gravity.CENTER)
            }
        } else {
            for (update in updates) {
                UpdaterPluginCard(context, update, this::refreshUpdates).addTo(linearLayout)
            }
        }
    }

    private fun refreshUpdates() {
        if (isRefreshing) return
        isRefreshing = true

        Utils.threadPool.execute {
            try {
                updates.clear()
                updates.addAll(PluginUpdater.fetchUpdates(updateSource))

                val noticeText = if (updates.isNotEmpty()) {
                    "Found ${Utils.pluralise(updates.size, "plugin update")}!"
                } else {
                    "No plugin updates found!"
                }

                Utils.mainThread.post {
                    setActionBarSubtitle(null)
                    Toast.makeText(context, noticeText, Toast.LENGTH_SHORT).show()
                    reRender()
                }
            } finally {
                isRefreshing = false
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

            Utils.mainThread.post {
                setActionBarSubtitle(null)

                val noticeText = buildString {
                    append("Updated ")
                    append(Utils.pluralise(succeeded.size, "plugin"))
                    if (failed.isEmpty()) {
                        append('!')
                    } else {
                        append(", but ")
                        append(failed.size)
                        append(" failed!")
                    }
                }
                Toast.makeText(context, noticeText, Toast.LENGTH_SHORT).show()
                reRender()
            }
            refreshUpdates()
        }
    }
}
