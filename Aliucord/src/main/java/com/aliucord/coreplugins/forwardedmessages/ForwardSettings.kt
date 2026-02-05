package com.aliucord.coreplugins.forwardedmessages

import android.os.Bundle
import android.view.View
import com.aliucord.Utils
import com.aliucord.api.SettingsAPI
import com.aliucord.settings.SettingsDelegate
import com.aliucord.settings.delegate
import com.aliucord.utils.ViewUtils.addTo
import com.aliucord.widgets.BottomSheet
import com.discord.views.CheckedSetting

internal object ForwardSettings {
    val settings = SettingsAPI("ForwardMessages")

    private val showToastDelegate = settings.delegate("showToast", true)
    val showToast by showToastDelegate

    class Sheet : BottomSheet() {
        override fun onViewCreated(view: View, bundle: Bundle?) {
            super.onViewCreated(view, bundle)

            createSetting("Show success toast", "Show a toast when messages are forwarded successfully.", showToastDelegate).addTo(linearLayout)
        }

        private fun createSetting(description: String, subtext: String, delegate: SettingsDelegate<Boolean>): CheckedSetting {
            return Utils.createCheckedSetting(
                requireContext(),
                CheckedSetting.ViewType.SWITCH,
                description,
                subtext
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
