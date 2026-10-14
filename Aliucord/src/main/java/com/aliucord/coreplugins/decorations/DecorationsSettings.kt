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

internal object DecorationsSettings {
    private val settings = SettingsAPI("Decorations")

    val enableAvatarDecoration = settings.delegate("enableAvatarDecorations", true)
    val enableGuildTags = settings.delegate("enableGuildTags", true)
    val enableNameplates = settings.delegate("enableNameplates", true)

    class Sheet : BottomSheet() {
        override fun onViewCreated(view: View, bundle: Bundle?) {
            super.onViewCreated(view, bundle)

            addSwitch("Show avatar decorations", enableAvatarDecoration)
            addSwitch("Show nameplates", enableNameplates)
            addSwitch("Show server tags", enableGuildTags)
        }

        private fun addSwitch(description: String, delegate: SettingsDelegate<Boolean>) {
            Utils.createCheckedSetting(
                context = requireContext(),
                type = CheckedSetting.ViewType.SWITCH,
                text = description,
                subtext = null,
                delegate = delegate,
            ).addTo(linearLayout)
        }
    }
}
