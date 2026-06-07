package com.aliucord.coreplugins.voice

import android.os.Bundle
import android.text.Editable
import android.text.InputType
import android.text.TextWatcher
import android.view.View
import android.widget.EditText
import android.widget.TextView
import com.aliucord.Utils
import com.aliucord.api.SettingsAPI
import com.aliucord.settings.delegate
import com.aliucord.utils.DimenUtils.dp
import com.aliucord.utils.ViewUtils.addTo
import com.aliucord.widgets.BottomSheet
import com.discord.views.CheckedSetting

internal object SunflowerSettings {
    const val MODE_AES256_GCM = "aead_aes256_gcm_rtpsize"
    const val MODE_XCHACHA20 = "aead_xchacha20_poly1305_rtpsize"

    const val DEFAULT_VIDEO_BITRATE_KBPS = 2500

    private val settings = SettingsAPI("Sunflower")

    private val useAes256GcmDelegate = settings.delegate("useAes256Gcm", false)
    val useAes256Gcm by useAes256GcmDelegate

    private val videoBitrateKbpsDelegate = settings.delegate("videoBitrateKbps", DEFAULT_VIDEO_BITRATE_KBPS)
    val videoBitrateKbps by videoBitrateKbpsDelegate

    // todo: to wire this
    val transportEncryption: String get() = if (useAes256Gcm) MODE_AES256_GCM else MODE_XCHACHA20

    class Sheet : BottomSheet() {
        override fun onViewCreated(view: View, bundle: Bundle?) {
            super.onViewCreated(view, bundle)
            val ctx = requireContext()

            Utils.createCheckedSetting(
                ctx,
                CheckedSetting.ViewType.SWITCH,
                "Use AES-256-GCM transport encryption",
                "When off, falls back to XChaCha20-Poly1305. AES-256-GCM matches what the official Discord client prefers."
            ).addTo(linearLayout) {
                var setting by useAes256GcmDelegate
                isChecked = setting
                setOnCheckedListener {
                    setting = !setting
                    Utils.promptRestart()
                }
            }

            TextView(ctx).addTo(linearLayout) {
                text = "Video / screenshare bitrate (kbps)"
                setPadding(0, 12.dp, 0, 4.dp)
            }
            EditText(ctx).addTo(linearLayout) {
                inputType = InputType.TYPE_CLASS_NUMBER
                hint = DEFAULT_VIDEO_BITRATE_KBPS.toString()
                setText(videoBitrateKbps.toString())
                addTextChangedListener(object : TextWatcher {
                    override fun beforeTextChanged(s: CharSequence?, a: Int, b: Int, c: Int) {}
                    override fun onTextChanged(s: CharSequence?, a: Int, b: Int, c: Int) {}
                    override fun afterTextChanged(s: Editable?) {
                        val kbps = s?.toString()?.trim()?.toIntOrNull() ?: return
                        if (kbps < 8) return
                        var setting by videoBitrateKbpsDelegate
                        setting = kbps
                    }
                })
            }
        }
    }
}
