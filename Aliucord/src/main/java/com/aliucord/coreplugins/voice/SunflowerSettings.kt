package com.aliucord.coreplugins.voice

import android.os.Bundle
import android.text.Editable
import android.text.InputType
import android.text.TextWatcher
import android.view.View
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import android.widget.LinearLayout
import android.widget.SeekBar
import android.widget.TextView
import com.aliucord.Utils
import com.aliucord.api.SettingsAPI
import com.aliucord.settings.SettingsDelegate
import com.aliucord.settings.delegate
import com.aliucord.utils.DimenUtils
import com.aliucord.utils.DimenUtils.dp
import com.aliucord.utils.ViewUtils.addTo
import com.aliucord.views.TextInput
import com.aliucord.widgets.BottomSheet
import com.discord.views.CheckedSetting
import com.lytefast.flexinput.R

internal object SunflowerSettings {
    const val MODE_AES256_GCM = "aead_aes256_gcm_rtpsize"
    const val MODE_XCHACHA20 = "aead_xchacha20_poly1305_rtpsize"

    const val DEFAULT_VIDEO_BITRATE_KBPS = 2500
    const val DEFAULT_VIDEO_FRAMERATE = 30
    const val DEFAULT_VIDEO_HEIGHT = 720
    const val DEFAULT_VIDEO_WIDTH = 1280
    const val FPS_MIN = 24
    const val FPS_MAX = 120

    private val settings = SettingsAPI("Sunflower")

    private val useAes256GcmDelegate = settings.delegate("useAes256Gcm", false)
    val useAes256Gcm by useAes256GcmDelegate

    private val videoBitrateKbpsDelegate = settings.delegate("videoBitrateKbps", DEFAULT_VIDEO_BITRATE_KBPS)
    val videoBitrateKbps by videoBitrateKbpsDelegate

    private val videoFramerateDelegate = settings.delegate("videoFramerate", DEFAULT_VIDEO_FRAMERATE)
    val videoFramerate by videoFramerateDelegate
    private val videoHeightDelegate = settings.delegate("videoHeight", DEFAULT_VIDEO_HEIGHT)
    val videoHeight by videoHeightDelegate
    private val videoWidthDelegate = settings.delegate("videoWidth", DEFAULT_VIDEO_WIDTH)
    val videoWidth by videoWidthDelegate
    private val daveEnabledDelegate = settings.delegate("daveEnabled", true)
    val daveEnabled by daveEnabledDelegate

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

            Utils.createCheckedSetting(
                ctx,
                CheckedSetting.ViewType.SWITCH,
                "Enable DAVE (end-to-end encryption)",
                "When off, streams use transport-only encryption (no MLS). Use to test whether viewers that can't do DAVE can see your screenshare/camera."
            ).addTo(linearLayout) {
                var setting by daveEnabledDelegate
                isChecked = setting
                setOnCheckedListener {
                    setting = !setting
                    Utils.promptRestart()
                }
            }

            val p = DimenUtils.defaultPadding

            TextView(ctx, null, 0, R.i.UiKit_Settings_Item_Header).addTo(linearLayout) {
                text = "Video / Screenshare"
            }

            TextInput(ctx, "Bitrate (kbps)", videoBitrateKbps.toString(), object : TextWatcher {
                override fun beforeTextChanged(s: CharSequence?, a: Int, b: Int, c: Int) {}
                override fun onTextChanged(s: CharSequence?, a: Int, b: Int, c: Int) {}
                override fun afterTextChanged(s: Editable?) {
                    val kbps = s?.toString()?.trim()?.toIntOrNull() ?: return
                    if (kbps < 8) return
                    var setting by videoBitrateKbpsDelegate
                    setting = kbps
                }
            }).addTo(linearLayout) {
                layoutParams = LinearLayout.LayoutParams(MATCH_PARENT, WRAP_CONTENT).apply {
                    marginStart = p
                    marginEnd = p
                }
                editText.inputType = InputType.TYPE_CLASS_NUMBER
                editText.hint = DEFAULT_VIDEO_BITRATE_KBPS.toString()
            }

            LinearLayout(ctx).addTo(linearLayout) {
                orientation = LinearLayout.HORIZONTAL
                layoutParams = LinearLayout.LayoutParams(MATCH_PARENT, WRAP_CONTENT).apply {
                    marginStart = p
                    marginEnd = p
                }

                fun resolutionInput(label: String, value: Int, default: Int, delegate: SettingsDelegate<Int>) =
                    TextInput(ctx, label, value.toString(), object : TextWatcher {
                        override fun beforeTextChanged(s: CharSequence?, a: Int, b: Int, c: Int) {}
                        override fun onTextChanged(s: CharSequence?, a: Int, b: Int, c: Int) {}
                        override fun afterTextChanged(s: Editable?) {
                            val px = s?.toString()?.trim()?.toIntOrNull() ?: return
                            if (px !in 64..4096) return
                            var setting by delegate
                            setting = px
                        }
                    }).addTo(this) {
                        layoutParams = LinearLayout.LayoutParams(0, WRAP_CONTENT, 1f)
                        editText.inputType = InputType.TYPE_CLASS_NUMBER
                        editText.hint = default.toString()
                    }

                resolutionInput("Width", videoWidth, DEFAULT_VIDEO_WIDTH, videoWidthDelegate)
                resolutionInput("Height", videoHeight, DEFAULT_VIDEO_HEIGHT, videoHeightDelegate)
            }

            val fpsLabel = TextView(ctx, null, 0, R.i.UiKit_Settings_Item_Header).addTo(linearLayout) {
                text = "Framerate: $videoFramerate fps"
            }
            SeekBar(ctx).addTo(linearLayout) {
                setPadding(p, 0, p, 0)
                // SeekBar min is 0, so offset by FPS_MIN: value = FPS_MIN + progress.
                max = FPS_MAX - FPS_MIN
                progress = videoFramerate.coerceIn(FPS_MIN, FPS_MAX) - FPS_MIN
                setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
                    override fun onProgressChanged(sb: SeekBar?, value: Int, fromUser: Boolean) {
                        val fps = FPS_MIN + value
                        fpsLabel.text = "Framerate: $fps fps"
                        var setting by videoFramerateDelegate
                        setting = fps
                    }
                    override fun onStartTrackingTouch(sb: SeekBar?) {}
                    override fun onStopTrackingTouch(sb: SeekBar?) {}
                })
            }
            LinearLayout(ctx).addTo(linearLayout) {
                orientation = LinearLayout.HORIZONTAL
                setPadding(p, 0, p, 8.dp)
                TextView(ctx).addTo(this) {
                    layoutParams = LinearLayout.LayoutParams(0, WRAP_CONTENT, 1f)
                    text = "$FPS_MIN fps"
                    textSize = 12f
                    alpha = 0.6f
                }
                TextView(ctx).addTo(this) {
                    text = "$FPS_MAX fps"
                    textSize = 12f
                    alpha = 0.6f
                }
            }
        }
    }
}
