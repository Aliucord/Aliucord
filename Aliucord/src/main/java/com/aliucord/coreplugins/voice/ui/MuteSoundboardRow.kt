package com.aliucord.coreplugins.voice.ui

import android.view.View
import android.widget.LinearLayout
import androidx.core.content.res.ResourcesCompat
import com.aliucord.Constants
import com.aliucord.coreplugins.voice.connections
import com.discord.utilities.color.ColorCompat
import com.lytefast.flexinput.R
import kotlin.collections.set
import com.aliucord.Utils
import com.discord.views.CheckedSetting
import b.a.q.n0.a as RtcControlSocket

private val muteSoundboardRowId = View.generateViewId()
private val soundboardMuted = HashMap<Long, Boolean>()

internal fun isSoundboardMuted(userId: Long): Boolean = soundboardMuted[userId] == true

internal fun addMuteSoundboardRow(currentSocket: RtcControlSocket?, root: LinearLayout, userId: Long) {
    if (userId == 0L) return
    muteSoundboardRow(root, soundboardMuted[userId] == true) { checked ->
        soundboardMuted[userId] = checked
        currentSocket?.connections?.forEach { it.muteSoundboard(userId, checked) }
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
