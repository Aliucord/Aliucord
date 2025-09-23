@file:Suppress("MISSING_DEPENDENCY_CLASS", "MISSING_DEPENDENCY_SUPERCLASS")

package com.aliucord.coreplugins.polls.creation

import android.os.Bundle
import android.view.View
import android.widget.ScrollView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.Guideline
import com.aliucord.Utils
import com.aliucord.utils.DimenUtils
import com.aliucord.utils.ViewUtils.addTo
import com.aliucord.views.Divider
import com.aliucord.widgets.BottomSheet
import com.aliucord.widgets.LinearLayout
import com.discord.views.CheckedSetting
import com.lytefast.flexinput.R

internal class DurationSelectorSheet(
    private val current: Duration,
    private val onSelected: (Duration) -> Unit
) : BottomSheet() {
    override fun onViewCreated(view: View, bundle: Bundle?) {
        super.onViewCreated(view, bundle)

        val ctx = requireContext()
        linearLayout.apply {
            ConstraintLayout(ctx, null, 0, R.i.UiKit_Sheet_Header).addTo(this) {
                Guideline(ctx, null, 0, R.i.UiKit_Sheet_Guideline).addTo(this)
                TextView(ctx, null, 0, R.i.UiKit_Sheet_Header_Title).addTo(this) {
                    setPadding(DimenUtils.defaultPadding, 0, 0, 0)
                    text = "Duration"
                }
            }
            Divider(ctx).addTo(this)
            ScrollView(ctx, null, 0, R.i.UiKit_ViewGroup_ScrollView).addTo(this) {
                LinearLayout(ctx).addTo(this) {
                    for (duration in Duration.values()) {
                        Utils.createCheckedSetting(ctx, CheckedSetting.ViewType.RADIO, duration.text, null).addTo(this) {
                            isChecked = current == duration
                            setOnCheckedListener {
                                onSelected(duration)
                                dismiss()
                            }
                        }
                    }
                }
            }
        }
    }
}
