package com.aliucord.coreplugins.voice.ui

import android.content.Context
import android.view.View
import android.widget.TextView

internal fun collapsibleTitle(ctx: Context, label: String, body: View, expanded: Boolean = false): TextView {
    body.visibility = if (expanded) View.VISIBLE else View.GONE

    fun titleText(open: Boolean) = (if (open) "▾ " else "▸ ") + label

    return cardTitle(ctx, titleText(expanded)).apply {
        setOnClickListener {
            val show = body.visibility != View.VISIBLE
            body.visibility = if (show) View.VISIBLE else View.GONE
            text = titleText(show)
        }
    }
}
