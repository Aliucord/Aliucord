package com.aliucord.coreplugins.voice

import android.graphics.Typeface
import android.os.Bundle
import android.text.SpannableStringBuilder
import android.text.Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.text.style.StyleSpan
import android.transition.TransitionManager
import android.view.View
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import android.widget.LinearLayout
import android.widget.TextView
import com.aliucord.Utils
import com.aliucord.api.SettingsAPI
import com.aliucord.coreplugins.voice.model.TransportModes
import com.aliucord.entities.Plugin
import com.aliucord.settings.delegate
import com.aliucord.utils.DimenUtils
import com.aliucord.utils.ViewUtils.addTo
import com.aliucord.views.DangerButton
import com.aliucord.widgets.BottomSheet
import com.discord.utilities.color.ColorCompat
import com.discord.views.CheckedSetting
import com.discord.widgets.user.usersheet.WidgetUserSheet
import com.google.gson.reflect.TypeToken
import com.lytefast.flexinput.R
import java.util.Collections

internal object VoiceChatFixSettings {
    const val DEFAULT_VIDEO_BITRATE_KBPS = 2500
    const val DEFAULT_VIDEO_FRAMERATE = 30
    const val DEFAULT_VIDEO_HEIGHT = 720
    const val DEFAULT_VIDEO_WIDTH = 1280
    const val FPS_MIN = 24
    const val FPS_MAX = 120
    const val DEFAULT_ENCODER_QUEUE_SIZE = 4
    const val DEFAULT_SOUNDBOARD_VOLUME = 100
    const val STREAM_MODE_DEFAULT = 0
    const val STREAM_MODE_PERFORMANCE = 1
    const val STREAM_MODE_HIGH_QUALITY = 2
    const val STREAM_MODE_CUSTOM = 3

    private val settings = SettingsAPI("VoiceChatFix")

    // Server only offers it when the hardware supports it, if not, XChaCha20 will be used
    internal val useAes256GcmDelegate = settings.delegate("useAes256Gcm", true)
    val useAes256Gcm by useAes256GcmDelegate
    internal val videoBitrateKbpsDelegate = settings.delegate("videoBitrateKbps", DEFAULT_VIDEO_BITRATE_KBPS)
    val videoBitrateKbps by videoBitrateKbpsDelegate
    internal val videoFramerateDelegate = settings.delegate("videoFramerate", DEFAULT_VIDEO_FRAMERATE)
    val videoFramerate by videoFramerateDelegate
    internal val videoHeightDelegate = settings.delegate("videoHeight", DEFAULT_VIDEO_HEIGHT)
    val videoHeight by videoHeightDelegate
    internal val videoWidthDelegate = settings.delegate("videoWidth", DEFAULT_VIDEO_WIDTH)
    val videoWidth by videoWidthDelegate
    internal val daveEnabledDelegate = settings.delegate("daveEnabled", true)
    val daveEnabled by daveEnabledDelegate
    private val encoderQueueSizeDelegate = settings.delegate("encoderQueueSize", DEFAULT_ENCODER_QUEUE_SIZE)
    val encoderQueueSize by encoderQueueSizeDelegate
    private val showConnInfoDelegate = settings.delegate("showConnInfo", false)
    val showConnInfo by showConnInfoDelegate
    private val effectNotificationsDelegate = settings.delegate("effectNotifications", true)
    val effectNotifications by effectNotificationsDelegate
    private val hqBluetoothDelegate = settings.delegate("hqBluetooth", false)
    val hqBluetooth by hqBluetoothDelegate
    private val hqBluetoothCompatDelegate = settings.delegate("hqBluetoothCompat", false)
    val hqBluetoothCompat by hqBluetoothCompatDelegate
    private val sidechainCompressionDelegate = settings.delegate("sidechainCompression", false)
    val sidechainCompression by sidechainCompressionDelegate
    private val autoAcceptSpeakInviteDelegate = settings.delegate("autoAcceptSpeakInvite", false)
    val autoAcceptSpeakInvite by autoAcceptSpeakInviteDelegate
    private val iKnowWhatImDoingDelegate = settings.delegate("iKnowWhatImDoing", false)
    val iKnowWhatImDoing by iKnowWhatImDoingDelegate
    internal val simulcastDelegate = settings.delegate("simulcast", false)
    val simulcast by simulcastDelegate
    internal val prioritySpeakerDelegate = settings.delegate("prioritySpeaker", false)
    val prioritySpeaker by prioritySpeakerDelegate
    internal val pingIntervalMsDelegate = settings.delegate("pingIntervalMs", 0)
    val pingIntervalMs by pingIntervalMsDelegate
    internal val minOutputDelayMsDelegate = settings.delegate("minOutputDelayMs", 0)
    val minOutputDelayMs by minOutputDelayMsDelegate
    internal val soundboardVolumeDelegate = settings.delegate("soundboardVolume", DEFAULT_SOUNDBOARD_VOLUME)
    val soundboardVolume by soundboardVolumeDelegate
    val mutedSoundboardUsers = PersistedIdSet(settings, "mutedSoundboardUsers")
    val disabledVideoUsers = PersistedIdSet(settings, "disabledVideoUsers")
    private val streamModeDelegate = settings.delegate("streamMode", STREAM_MODE_DEFAULT)
    val streamMode by streamModeDelegate
    private val customQualityDelegate = settings.delegate("customQuality", false)
    val customQualityEnabled by customQualityDelegate
    val transportEncryption: String get() = if (useAes256Gcm) TransportModes.AES256_GCM else TransportModes.XCHACHA20

    fun applyStreamMode(mode: Int) {
        var selected by streamModeDelegate
        var width by videoWidthDelegate
        var height by videoHeightDelegate
        var fps by videoFramerateDelegate
        var bitrate by videoBitrateKbpsDelegate

        selected = mode
        when (mode) {
            // Custom keeps whatever the settings page holds, so nothing is written over it
            STREAM_MODE_CUSTOM -> return
            STREAM_MODE_PERFORMANCE -> { width = 854; height = 480; fps = 30; bitrate = 1000 }
            STREAM_MODE_HIGH_QUALITY -> { width = 1920; height = 1080; fps = 60; bitrate = 6000 }
            else -> { width = 1280; height = 720; fps = 30; bitrate = DEFAULT_VIDEO_BITRATE_KBPS }
        }
    }

    class Sheet : BottomSheet() {
        private val fixBtAuthor = Plugin.Manifest.Author("oSumAtrIX", 737323631117598811L, false)

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
                        "Simulcast",
                        "Advertise a second low-quality video layer so weak-connection viewers get a smooth downscaled stream instead of a choppy full-quality one. Turn off if your outgoing video breaks."
                    ).addTo(this) {
                        var setting by simulcastDelegate
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

                    Utils.createCheckedSetting(
                        ctx,
                        CheckedSetting.ViewType.SWITCH,
                        "Voice effect notifications",
                        "Shows a toast when someone plays a soundboard sound or sends an emoji reaction in your voice channel."
                    ).addTo(this) {
                        var setting by effectNotificationsDelegate
                        isChecked = setting
                        setOnCheckedListener { setting = !setting }
                    }

                    Utils.createCheckedSetting(
                        ctx,
                        CheckedSetting.ViewType.SWITCH,
                        "Auto-accept invite to speak",
                        "Automatically accepts a moderator's invite to speak in stage channels."
                    ).addTo(this) {
                        var setting by autoAcceptSpeakInviteDelegate
                        isChecked = setting
                        setOnCheckedListener { setting = !setting }
                    }

                    Utils.createCheckedSetting(
                        ctx,
                        CheckedSetting.ViewType.SWITCH,
                        "Priority speaker",
                        "Lower everyone else's volume while you talk. Only works if you have Priority Speaker permissions."
                    ).addTo(this) {
                        var setting by prioritySpeakerDelegate
                        isChecked = setting
                        setOnCheckedListener { setting = !setting }
                    }

                    TextView(ctx, null, 0, R.i.UiKit_Settings_Item_Header).addTo(this) {
                        setPadding(p, p, p, 0)
                        text = "Video / Screenshare"
                    }

                    Utils.createCheckedSetting(
                        ctx,
                        CheckedSetting.ViewType.SWITCH,
                        "Enable custom quality",
                        "Adds a Custom option to the stream quality picker when starting a screenshare."
                    ).addTo(this) {
                        var setting by customQualityDelegate
                        isChecked = setting
                        setOnCheckedListener { setting = !setting }
                    }

                    field(
                        "Encoder queue size",
                        encoderQueueSize,
                        DEFAULT_ENCODER_QUEUE_SIZE,
                        2..16,
                        encoderQueueSizeDelegate,
                    )

                    TextView(ctx, null, 0, R.i.UiKit_Settings_Item_Header).addTo(this) {
                        text = "Connection tuning"
                    }

                    LinearLayout(ctx).addTo(this) {
                        orientation = LinearLayout.HORIZONTAL
                        layoutParams = LinearLayout.LayoutParams(MATCH_PARENT, WRAP_CONTENT).apply {
                            marginStart = p
                            marginEnd = p
                        }

                        field(
                            "Ping interval (ms)",
                            pingIntervalMs,
                            0,
                            0..60_000,
                            pingIntervalMsDelegate,
                            isWeighted = true,
                            isEven = true,
                        )
                        field(
                            "Min output delay (ms)",
                            minOutputDelayMs,
                            0,
                            0..1_000,
                            minOutputDelayMsDelegate,
                            isWeighted = true,
                            isEven = true,
                        )
                    }

                    TextView(ctx, null, 0, R.i.UiKit_Settings_Item_SubText).addTo(this) {
                        setPadding(p, p / 4, p, 4)
                        text = "0 keeps the native defaults. Takes effect on the next voice connection."
                        setTextColor(ColorCompat.getThemedColor(ctx, R.b.colorTextMuted))
                    }

                    TextView(ctx, null, 0, R.i.UiKit_Settings_Item_Header).addTo(this) {
                        setPadding(p, p * 2, p, 0)
                        text = "Fix Bluetooth Audio Quality"
                    }

                    TextView(ctx, null, 0, R.i.UiKit_Settings_Item_SubText).addTo(this) {
                        setPadding(p, 0, p, 0)
                        text = SpannableStringBuilder().apply {
                            append("brought to you by ")
                            val start = length
                            append(fixBtAuthor.name, StyleSpan(Typeface.BOLD), SPAN_EXCLUSIVE_EXCLUSIVE)

                            // and yes, the 'hyperlink' param is implemented just because
                            // which is very useless because the default value for hyperlink==true
                            if (fixBtAuthor.hyperlink) {
                                setSpan(object : ClickableSpan() {
                                    override fun onClick(widget: View) {
                                        this@Sheet.dismiss()
                                        WidgetUserSheet.show(fixBtAuthor.id, parentFragmentManager)
                                    }
                                }, start, length, SPAN_EXCLUSIVE_EXCLUSIVE)
                            }
                        }
                        movementMethod = LinkMovementMethod.getInstance()
                    }

                    Utils.createCheckedSetting(
                        ctx,
                        CheckedSetting.ViewType.SWITCH,
                        "High quality (A2DP)",
                        "Keeps Bluetooth audio on A2DP instead of the low-quality call protocol (SCO). Adds slight delay and uses the phone's mic instead of the headset's."
                    ).addTo(this) {
                        var setting by hqBluetoothDelegate
                        isChecked = setting
                        setOnCheckedListener { setting = !setting }
                    }

                    Utils.createCheckedSetting(
                        ctx,
                        CheckedSetting.ViewType.SWITCH,
                        "Enable compatibility mode",
                        "Use if HQ Bluetooth has no effect on your device. Breaks the phone speaker during calls."
                    ).addTo(this) {
                        var setting by hqBluetoothCompatDelegate
                        isChecked = setting
                        setOnCheckedListener { setting = !setting }
                    }

                    TextView(ctx, null, 0, R.i.UiKit_Settings_Item_Header).addTo(this) {
                        setPadding(p, p, p, 0)
                        text = "Experimental"
                    }

                    Utils.createCheckedSetting(
                        ctx,
                        CheckedSetting.ViewType.SWITCH,
                        "Disable sidechain compression",
                        "This is off for everyone by default. An automatic ducking effect in the native voice engine. This can add a slight audio delay."
                    ).addTo(this) {
                        var setting by sidechainCompressionDelegate
                        isChecked = setting
                        setOnCheckedListener {
                            setting = !setting
                            Utils.promptRestart()
                        }
                    }
                }
            }
        }
    }
}

internal class PersistedIdSet(
    private val settings: SettingsAPI,
    private val key: String
) {
    private val ids: MutableSet<Long> = Collections.synchronizedSet(
        settings.getObject(
            key,
            hashSetOf(),
            TypeToken.getParameterized(HashSet::class.java, Long::class.javaObjectType).type
        )
    )

    operator fun contains(userId: Long): Boolean = userId in ids

    fun set(userId: Long, present: Boolean) {
        val changed = if (present) ids.add(userId) else ids.remove(userId)
        if (changed) settings.setObject(key, ids)
    }

    val size: Int get() = ids.size
}
