package com.aliucord.coreplugins.decorations

import android.os.Bundle
import android.view.View
import com.aliucord.Utils
import com.aliucord.api.SettingsAPI
import com.aliucord.settings.SettingsDelegate
import com.aliucord.settings.delegate
import com.aliucord.utils.ViewUtils.addTo
import com.aliucord.widgets.BottomSheet
import com.discord.views.CheckedSetting

internal object JumpToMessageSettings {
    private val settings = SettingsAPI("JumpToMessageFix")

    private val autoExpandBlockedMessagesDelegate = settings.delegate("autoExpandBlockedMessages", true)
    val autoExpandBlockedMessages by autoExpandBlockedMessagesDelegate

    class Sheet : BottomSheet() {
        override fun onViewCreated(view: View, bundle: Bundle?) {
            super.onViewCreated(view, bundle)

            createSetting("Expand blocked messages on jump", autoExpandBlockedMessagesDelegate).addTo(linearLayout)
        }

        @Suppress("AssignedValueIsNeverRead")
        private fun createSetting(description: String, delegate: SettingsDelegate<Boolean>): CheckedSetting {
            return Utils.createCheckedSetting(
                requireContext(),
                CheckedSetting.ViewType.SWITCH,
                description,
                null
            ).apply {
                var setting by delegate
                isChecked = setting
                setOnCheckedListener {
                    setting = !setting
                }
            }
        }
    }
}
