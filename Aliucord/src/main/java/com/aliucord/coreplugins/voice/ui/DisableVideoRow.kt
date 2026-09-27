package com.aliucord.coreplugins.voice.ui

import android.view.View
import android.widget.LinearLayout
import androidx.core.content.res.ResourcesCompat
import com.aliucord.Constants
import com.aliucord.coreplugins.voice.VideoSinkOverrides
import com.aliucord.coreplugins.voice.VoiceChatFixSettings
import com.aliucord.coreplugins.voice.sendSinkWants
import com.discord.rtcconnection.EncodeQuality
import com.discord.utilities.color.ColorCompat
import com.lytefast.flexinput.R
import com.aliucord.Utils
import com.discord.views.CheckedSetting
import b.a.q.n0.a as RtcControlSocket

private val disableVideoRowId = View.generateViewId()

internal fun isVideoDisabled(userId: Long): Boolean = userId in VoiceChatFixSettings.disabledVideoUsers

internal fun addDisableVideoRow(currentSocket: RtcControlSocket?, root: LinearLayout, userId: Long) {
    if (userId == 0L) return
    disableVideoRow(root, isVideoDisabled(userId)) { checked ->
        VoiceChatFixSettings.disabledVideoUsers.set(userId, checked)
        // quality 0 tells the server to stop routing their video to us, null lifts it
        if (VideoSinkOverrides.setUserQuality(userId, if (checked) EncodeQuality.Zero else null)) {
            currentSocket?.let { socket -> VideoSinkOverrides.resend(socket::sendSinkWants) }
        }
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
