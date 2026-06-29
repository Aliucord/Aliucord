package com.aliucord.coreplugins.voice

import android.text.Editable
import android.text.InputType
import android.text.TextWatcher
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import android.widget.LinearLayout
import androidx.fragment.app.DialogFragment
import com.aliucord.settings.SettingsDelegate
import com.aliucord.utils.DimenUtils
import com.aliucord.utils.ViewUtils.addTo
import com.aliucord.views.TextInput

private const val GROUP_SIZE = 5
private const val DESIRED_LEN = 30
private const val GROUP_MODULUS = 100000UL // 10.0.pow(groupSize).toULong()

internal fun formatFingerprint(b64: String): String {
    if (b64.isEmpty()) return ""

    val data = b64.decodeBase64ToArray() ?: return ""

    if (data.size < DESIRED_LEN) return ""

    val sb = StringBuilder(DESIRED_LEN + DESIRED_LEN / GROUP_SIZE)

    for (group in 0 until DESIRED_LEN / GROUP_SIZE) {
        val start = group * GROUP_SIZE
        var value = 0UL

        for (index in 0 until GROUP_SIZE) {
            value = (value shl 8) or data[start + index].toUByte().toULong()
        }

        if (group > 0) sb.append(' ')

        sb.append((value % GROUP_MODULUS).toString().padStart(GROUP_SIZE, '0'))
    }

    return sb.toString()
}

internal fun LinearLayout.validate(
    fragment: DialogFragment,
    inputs: MutableList<TextInput>,
    label: String,
    initial: Int,
    hint: Int,
    range: IntRange,
    delegate: SettingsDelegate<Int>,
    isWeighted: Boolean,
    isEven: Boolean,
) {
    lateinit var input: TextInput

    val p = DimenUtils.defaultPadding
    val error = if (range.last == Int.MAX_VALUE) {
        "Value must be ${range.first} or higher."
    } else {
        "Value must be between ${range.first} and ${range.last}."
    }

    input = TextInput(context, label, initial.toString(), object : TextWatcher {
        override fun beforeTextChanged(s: CharSequence?, a: Int, b: Int, c: Int) {}
        override fun onTextChanged(s: CharSequence?, a: Int, b: Int, c: Int) {}
        override fun afterTextChanged(s: Editable?) {
            val value = s?.toString()?.trim()?.toIntOrNull()

            // 'value !in range' causes some weird compilation error
            // java.lang.AssertionError: Assertion failed
            if (value == null || value < range.first || value > range.last) {
                input.editText.error = error
            } else if (isEven && value % 2 != 0) {
                input.editText.error = "Value must be even."
            } else {
                input.editText.error = null
                var setting by delegate
                setting = value
            }

            fragment.isCancelable = inputs.all { it.editText.error == null }
        }
    }).addTo(this) {
        layoutParams = if (isWeighted) {
            LinearLayout.LayoutParams(0, WRAP_CONTENT, 1f)
        } else LinearLayout.LayoutParams(MATCH_PARENT, WRAP_CONTENT).apply {
            marginStart = p
            marginEnd = p
        }
        editText.inputType = InputType.TYPE_CLASS_NUMBER
        editText.hint = hint.toString()
    }

    inputs.add(input)
}
