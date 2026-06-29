package com.aliucord.coreplugins.voice

import android.os.Bundle
import android.transition.TransitionManager
import android.view.Gravity
import android.view.View
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import android.widget.LinearLayout
import android.widget.SeekBar
import android.widget.TextView
import com.aliucord.Utils
import com.aliucord.api.SettingsAPI
import com.aliucord.settings.delegate
import com.aliucord.utils.DimenUtils
import com.aliucord.utils.DimenUtils.dp
import com.aliucord.utils.ViewUtils.addTo
import com.aliucord.views.DangerButton
import com.aliucord.widgets.BottomSheet
import com.discord.utilities.color.ColorCompat
import com.discord.views.CheckedSetting
import com.lytefast.flexinput.R

internal object VoiceChatFixSettings {
    const val MODE_AES256_GCM = "aead_aes256_gcm_rtpsize"
    const val MODE_XCHACHA20 = "aead_xchacha20_poly1305_rtpsize"

    const val DEFAULT_VIDEO_BITRATE_KBPS = 2500
    const val DEFAULT_VIDEO_FRAMERATE = 30
    const val DEFAULT_VIDEO_HEIGHT = 720
    const val DEFAULT_VIDEO_WIDTH = 1280
    const val FPS_MIN = 24
    const val FPS_MAX = 120
    const val DEFAULT_ENCODER_QUEUE_SIZE = 4

    private val settings = SettingsAPI("VoiceChatFix")

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
    private val encoderQueueSizeDelegate = settings.delegate("encoderQueueSize", DEFAULT_ENCODER_QUEUE_SIZE)
    val encoderQueueSize by encoderQueueSizeDelegate
    private val showConnInfoDelegate = settings.delegate("showConnInfo", false)
    val showConnInfo by showConnInfoDelegate
    private val iKnowWhatImDoingDelegate = settings.delegate("iKnowWhatImDoing", false)
    val iKnowWhatImDoing by iKnowWhatImDoingDelegate

    val transportEncryption: String get() = if (useAes256Gcm) MODE_AES256_GCM else MODE_XCHACHA20

    class Sheet : BottomSheet() {
        override fun onViewCreated(view: View, bundle: Bundle?) {
            super.onViewCreated(view, bundle)
            lateinit var settingsLayout: LinearLayout

            val ctx = requireContext()
            val p = DimenUtils.defaultPadding
            var allowSettings by iKnowWhatImDoingDelegate
            val builder = VoiceInputBuilder(this@Sheet)

            LinearLayout(ctx).addTo(linearLayout) warningLayout@{
                orientation = LinearLayout.VERTICAL
                visibility = if (!iKnowWhatImDoing) View.VISIBLE else View.GONE

                TextView(ctx, null, 0, R.i.UiKit_Settings_Item_Label).addTo(this) {
                    text = "Are you sure?"
                }

                TextView(ctx, null, 0, R.i.UiKit_Settings_Item_SubText).addTo(this) {
                    text = "Don't enable this unless you know what you're doing. Modifying these settings could break voice chats!"
                }

                DangerButton(ctx).addTo(this) {
                    text = "I know what I'm doing"
                    layoutParams = LinearLayout.LayoutParams(MATCH_PARENT, WRAP_CONTENT).apply {
                        marginStart = p
                        marginEnd = p
                    }

                    setOnClickListener {
                        allowSettings = true
                        this@warningLayout.visibility = View.GONE
                        TransitionManager.beginDelayedTransition(linearLayout)
                        settingsLayout.visibility = View.VISIBLE
                    }
                }
            }

            settingsLayout = with(builder) {
                LinearLayout(ctx).addTo(linearLayout) {
                    lateinit var fpsLabel: TextView

                    orientation = LinearLayout.VERTICAL
                    visibility = if (iKnowWhatImDoing) View.VISIBLE else View.GONE

                    Utils.createCheckedSetting(
                        ctx,
                        CheckedSetting.ViewType.SWITCH,
                        "Use AES-256-GCM transport encryption",
                        "Preferred transport encryption when the server supports it."
                    ).addTo(this) {
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
                    ).addTo(this) {
                        var setting by daveEnabledDelegate
                        isChecked = setting
                        setOnCheckedListener {
                            setting = !setting
                            Utils.promptRestart()
                        }
                    }

                    Utils.createCheckedSetting(
                        ctx,
                        CheckedSetting.ViewType.SWITCH,
                        "Show connection info overlay",
                        "Adds an info card to the voice bottom sheet. Takes effect on the next voice connection."
                    ).addTo(this) {
                        var setting by showConnInfoDelegate
                        isChecked = setting
                        setOnCheckedListener { setting = !setting }
                    }

                    TextView(ctx, null, 0, R.i.UiKit_Settings_Item_Header).addTo(this) {
                        text = "Video / Screenshare"
                    }

                    field(
                        "Bitrate (kbps)",
                        videoBitrateKbps,
                        DEFAULT_VIDEO_BITRATE_KBPS,
                        8..Int.MAX_VALUE,
                        videoBitrateKbpsDelegate,
                    )

                    LinearLayout(ctx).addTo(this) {
                        orientation = LinearLayout.HORIZONTAL
                        layoutParams = LinearLayout.LayoutParams(MATCH_PARENT, WRAP_CONTENT).apply {
                            marginStart = p
                            marginEnd = p
                        }

                        field(
                            "Width",
                            videoWidth,
                            DEFAULT_VIDEO_WIDTH,
                            64..4096,
                            videoWidthDelegate,
                            isWeighted = true,
                            isEven = true,
                        )
                        field(
                            "Height",
                            videoHeight,
                            DEFAULT_VIDEO_HEIGHT,
                            64..4096,
                            videoHeightDelegate,
                            isWeighted = true,
                            isEven = true,
                        )
                    }

                    TextView(ctx, null, 0, R.i.UiKit_Settings_Item_SubText).addTo(this) {
                        setPadding(p, p / 4, p, 4)
                        text = "Takes effect on the next voice connection."
                        setTextColor(ColorCompat.getThemedColor(ctx, R.b.colorTextMuted))
                    }

                    LinearLayout(ctx).addTo(this) {
                        layoutParams = LinearLayout.LayoutParams(MATCH_PARENT, WRAP_CONTENT)
                        gravity = Gravity.CENTER_VERTICAL
                        orientation = LinearLayout.HORIZONTAL

                        TextView(ctx, null, 0, R.i.UiKit_Settings_Item_Header).addTo(this) {
                            layoutParams = LinearLayout.LayoutParams(0, WRAP_CONTENT, 1f)
                            text = "Framerate"
                        }
                        fpsLabel = TextView(ctx, null, 0, R.i.UiKit_Settings_Item_Header).addTo(this) {
                            text = "$videoFramerate fps"
                        }
                    }

                    SeekBar(ctx).addTo(this) {
                        setPadding(p, 0, p, 0)
                        // SeekBar min is 0, so offset by FPS_MIN: value = FPS_MIN + progress.
                        max = FPS_MAX - FPS_MIN
                        progress = videoFramerate.coerceIn(FPS_MIN, FPS_MAX) - FPS_MIN
                        setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
                            override fun onProgressChanged(sb: SeekBar?, value: Int, fromUser: Boolean) {
                                fpsLabel.text = "${FPS_MIN + value} fps"
                            }
                            override fun onStartTrackingTouch(sb: SeekBar?) {}
                            override fun onStopTrackingTouch(sb: SeekBar?) {
                                var setting by videoFramerateDelegate
                                setting = FPS_MIN + (sb?.progress ?: return)
                            }
                        })
                    }

                    LinearLayout(ctx, null, 0, R.i.UiKit_Settings_Item_SubText).addTo(this) {
                        orientation = LinearLayout.HORIZONTAL
                        setPadding(p, 0, p, 8.dp)
                        TextView(ctx).addTo(this) {
                            layoutParams = LinearLayout.LayoutParams(0, WRAP_CONTENT, 1f)
                            text = "$FPS_MIN fps"
                            textSize = 12f
                            setTextColor(ColorCompat.getThemedColor(ctx, R.b.colorTextMuted))
                        }
                        TextView(ctx).addTo(this) {
                            text = "$FPS_MAX fps"
                            textSize = 12f
                            setTextColor(ColorCompat.getThemedColor(ctx, R.b.colorTextMuted))
                        }
                    }

                    field(
                        "Encoder queue size",
                        encoderQueueSize,
                        DEFAULT_ENCODER_QUEUE_SIZE,
                        2..16,
                        encoderQueueSizeDelegate,
                    )
                }
            }
        }
    }
}
