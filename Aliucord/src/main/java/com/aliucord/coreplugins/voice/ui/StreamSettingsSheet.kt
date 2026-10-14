package com.aliucord.coreplugins.voice.ui

import android.content.Context
import android.content.res.ColorStateList
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.res.ResourcesCompat
import com.aliucord.Constants
import com.aliucord.Utils
import com.aliucord.coreplugins.voice.VoiceChatFixSettings
import com.aliucord.coreplugins.voice.VoiceInputBuilder
import com.aliucord.utils.DimenUtils
import com.aliucord.utils.ViewUtils.setDefaultMargins
import com.aliucord.views.Button
import com.aliucord.widgets.BottomSheet
import com.discord.utilities.color.ColorCompat
import com.discord.views.CheckedSetting
import com.lytefast.flexinput.R

// Shown before the screen capture prompt, style based off discord rn
internal class StreamSettingsSheet : BottomSheet() {
    var onConfirm: (() -> Unit)? = null

    // Own type rather than Triple, I love kotlin stdlib
    private class Mode(val id: Int, val title: String, val subtitle: String, val icon: String)

    // Custom option only appears when it's switched on in the plugin settings, then its values are entered here
    private val modes = mutableListOf(
        Mode(VoiceChatFixSettings.STREAM_MODE_DEFAULT, "Default", "Balanced quality and performance (720p, 30fps)", "ic_mobile_screenshare_24dp"),
        Mode(VoiceChatFixSettings.STREAM_MODE_PERFORMANCE, "Performance", "Optimized for slower devices (480p, 30fps)", "exo_ic_speed"),
        Mode(VoiceChatFixSettings.STREAM_MODE_HIGH_QUALITY, "High Quality", "For video and gaming (1080p, 60fps)", "ic_premium_marketing_stream_quality_24dp"),
    ).apply {
        if (VoiceChatFixSettings.customQualityEnabled) add(Mode(
            VoiceChatFixSettings.STREAM_MODE_CUSTOM,
            "Custom",
            "Choose your own resolution, framerate and bitrate",
            "ic_settings_24dp",
        ))
    }

    override fun onViewCreated(view: View, bundle: Bundle?) {
        super.onViewCreated(view, bundle)
        // Recreated by the system, so the callback is gone and Start Streaming would do nothing
        if (onConfirm == null) return dismiss()

        // Fallback from Custom if it gets switched off after getting picked
        var selected = VoiceChatFixSettings.streamMode
            .takeIf { saved -> modes.any { it.id == saved } }
            ?: VoiceChatFixSettings.STREAM_MODE_DEFAULT

        val radios = ArrayList<CheckedSetting>(modes.size)

        // Every CheckedSetting uses this view, stays hidden until something gives it a drawable
        val iconId = Utils.getResId("setting_drawable_left", "id")

        // Assigning isChecked fires the listener again, so the group update has to be re-entrant safe
        var updating = false

        val ctx = requireContext()
        val p = DimenUtils.defaultPadding
        val headerPrimary = ColorCompat.getThemedColor(ctx, R.b.colorHeaderPrimary)
        setPadding(p, p, p, p)

        // Built up front so picking Custom only has to flip its visibility
        val customInputs = buildCustomInputs(ctx)
        fun syncCustom() {
            customInputs?.visibility =
                if (selected == VoiceChatFixSettings.STREAM_MODE_CUSTOM) View.VISIBLE else View.GONE
        }

        addView(TextView(ctx, null, 0, R.i.UiKit_TextView_Semibold).apply {
            text = "Stream Settings"
            textSize = 18f
            gravity = Gravity.CENTER
            setTextColor(headerPrimary)
            setPadding(0, 0, 0, p)
        })

        addView(TextView(ctx, null, 0, R.i.UiKit_Settings_Item_Header).apply {
            text = "Stream Mode"
            typeface = ResourcesCompat.getFont(ctx, Constants.Fonts.whitney_semibold)
            setTextColor(ColorCompat.getThemedColor(ctx, R.b.colorHeaderSecondary))
            setPadding(0, p / 4, 0, p / 4)
        })

        modes.forEach { mode ->
            val radio = Utils.createCheckedSetting(ctx, CheckedSetting.ViewType.RADIO, mode.title, mode.subtitle).apply {
                isChecked = mode.id == selected
                setTextColor(headerPrimary)
                // Carries which mode the row stands for, so the group never depends on list order
                tag = mode.id
                findViewById<ImageView>(iconId).apply {
                    setImageResource(Utils.getResId(mode.icon, "drawable"))
                    imageTintList = ColorStateList.valueOf(headerPrimary)
                    setPadding(0, 0, p / 4, 0)
                }
            }
            radios.add(radio)

            radio.setOnCheckedListener {
                if (updating) return@setOnCheckedListener

                updating = true
                selected = mode.id

                // Radios don't group themselves, so clear the others by hand
                radios.forEach { other -> other.isChecked = other.tag == selected }

                updating = false
                syncCustom()
            }
            addView(radio)
        }

        customInputs?.let {
            syncCustom()
            addView(it)
        }

        addView(Button(ctx).apply {
            text = "Start Streaming"
            layoutParams = LinearLayout.LayoutParams(MATCH_PARENT, WRAP_CONTENT).apply { topMargin = p }
            setOnClickListener {
                VoiceChatFixSettings.applyStreamMode(selected)
                dismiss()
                onConfirm?.invoke()
            }
        })
    }

    private fun buildCustomInputs(ctx: Context): LinearLayout? {
        if (!VoiceChatFixSettings.customQualityEnabled) return null

        val p = DimenUtils.defaultPadding
        val builder = VoiceInputBuilder(this)

        fun row(orientation: Int) = LinearLayout(ctx).apply {
            this.orientation = orientation
            layoutParams = LinearLayout.LayoutParams(MATCH_PARENT, WRAP_CONTENT)
        }

        val container = row(LinearLayout.VERTICAL)
        val size = row(LinearLayout.HORIZONTAL)
        val rate = row(LinearLayout.HORIZONTAL)

        with(builder) {
            size.field(
                "Width",
                VoiceChatFixSettings.videoWidth,
                VoiceChatFixSettings.DEFAULT_VIDEO_WIDTH,
                64..4096,
                VoiceChatFixSettings.videoWidthDelegate,
                isWeighted = true,
                isEven = true,
            )
            size.field(
                "Height",
                VoiceChatFixSettings.videoHeight,
                VoiceChatFixSettings.DEFAULT_VIDEO_HEIGHT,
                64..4096,
                VoiceChatFixSettings.videoHeightDelegate,
                isWeighted = true,
                isEven = true,
            )
            rate.field(
                "FPS",
                VoiceChatFixSettings.videoFramerate,
                VoiceChatFixSettings.DEFAULT_VIDEO_FRAMERATE,
                VoiceChatFixSettings.FPS_MIN..VoiceChatFixSettings.FPS_MAX,
                VoiceChatFixSettings.videoFramerateDelegate,
                isWeighted = true,
            )
            rate.field(
                "Bitrate (kbps)",
                VoiceChatFixSettings.videoBitrateKbps,
                VoiceChatFixSettings.DEFAULT_VIDEO_BITRATE_KBPS,
                8..Int.MAX_VALUE,
                VoiceChatFixSettings.videoBitrateKbpsDelegate,
                isWeighted = true,
            )
        }

        container.addView(size)
        container.addView(rate)
        container.addView(TextView(ctx, null, 0, R.i.UiKit_Form_Notice_Warning).apply {
            setPadding(p, p / 2, p, p / 2)
            setDefaultMargins(left = true, top = true, right = true, bottom = false)
            text = "Higher values need a stronger device and connection."
            setTextColor(ColorCompat.getThemedColor(ctx, R.b.colorTextMuted))
        })

        return container
    }
}
