package com.aliucord.coreplugins.voice.ui

import android.content.Context
import android.text.SpannableStringBuilder
import android.widget.TextView
import com.aliucord.utils.MDUtils

internal fun codeBlock(ctx: Context): TextView = TextView(ctx).apply {
    isSingleLine = false
    maxLines = 400
}

internal fun TextView.setCodeBlock(text: CharSequence) {
    val render = MDUtils.renderCodeBlock(context, SpannableStringBuilder(), null, text.toString())
    this.text = render.delete(render.length - 1, render.length)
}
