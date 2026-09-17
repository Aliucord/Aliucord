package com.aliucord.coreplugins.voice

import android.widget.LinearLayout
import androidx.fragment.app.DialogFragment
import com.aliucord.settings.SettingsDelegate
import com.aliucord.views.TextInput

internal class VoiceInputBuilder(
    private val fragment: DialogFragment
) {
    val inputs = mutableListOf<TextInput>()

    fun LinearLayout.field(
        label: String,
        value: Int,
        hint: Int,
        range: IntRange,
        delegate: SettingsDelegate<Int>,
        isWeighted: Boolean = false,
        isEven: Boolean = false,
    ) = validate(fragment, inputs, label, value, hint, range, delegate, isWeighted, isEven)
}
