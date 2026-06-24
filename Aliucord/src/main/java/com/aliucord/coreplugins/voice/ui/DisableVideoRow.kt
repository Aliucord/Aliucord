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

private val disableVideoRowId = View.generateViewId()
private val videoDisabled = HashMap<Long, Boolean>()

internal fun addDisableVideoRow(currentSocket: RtcControlSocket?, root: LinearLayout, userId: Long) {
    if (userId == 0L) return
    disableVideoRow(root, videoDisabled[userId] == true) { checked ->
        videoDisabled[userId] = checked
        currentSocket?.connections?.forEach { it.disableVideo(userId, checked) }
    }
}

private fun disableVideoRow(root: LinearLayout, checked: Boolean, onToggle: (Boolean) -> Unit) {
    val setting = root.findViewById(disableVideoRowId) ?: run {
        val container = rowContainer(root)
        Utils.createCheckedSetting(
            container.context,
            CheckedSetting.ViewType.SWITCH,
            "Disable Video",
            null,
        ).also {
            it.id = disableVideoRowId
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
