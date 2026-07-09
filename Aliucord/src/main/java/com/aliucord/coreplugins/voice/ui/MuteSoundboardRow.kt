package com.aliucord.coreplugins.voice.ui

import android.view.View
import android.widget.LinearLayout
import androidx.core.content.res.ResourcesCompat
import com.aliucord.Constants
import com.aliucord.coreplugins.voice.VoiceChatFixSettings
import com.discord.utilities.color.ColorCompat
import com.lytefast.flexinput.R
import com.aliucord.Utils
import com.discord.views.CheckedSetting

private val muteSoundboardRowId = View.generateViewId()

internal fun isSoundboardMuted(userId: Long): Boolean = userId in VoiceChatFixSettings.mutedSoundboardUsers

internal fun addMuteSoundboardRow(root: LinearLayout, userId: Long) {
    if (userId == 0L) return
    muteSoundboardRow(root, isSoundboardMuted(userId)) { checked ->
        VoiceChatFixSettings.mutedSoundboardUsers.set(userId, checked)
    }
}

private fun muteSoundboardRow(root: LinearLayout, checked: Boolean, onToggle: (Boolean) -> Unit) {
    val setting = root.findViewById(muteSoundboardRowId) ?: run {
        val container = rowContainer(root)
        Utils.createCheckedSetting(
            container.context,
            CheckedSetting.ViewType.SWITCH,
            "Mute Soundboard",
            null,
        ).also {
            it.id = muteSoundboardRowId
            it.l.a().typeface = ResourcesCompat.getFont(it.context, Constants.Fonts.whitney_semibold)
            it.setTextColor(ColorCompat.getThemedColor(it.context, R.b.colorHeaderPrimary))
            it.l.b().apply {
                background = null
                foreground = null
            }
            container.addView(it)
        }
    }

    setting.setOnCheckedListener(null)
    setting.isChecked = checked
    setting.setOnCheckedListener { onToggle(it) }
}
