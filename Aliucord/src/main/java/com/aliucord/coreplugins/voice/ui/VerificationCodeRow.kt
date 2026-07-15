package com.aliucord.coreplugins.voice.ui

import android.view.View
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.res.ResourcesCompat
import com.aliucord.Constants
import com.aliucord.Utils
import com.aliucord.coreplugins.voice.pairwiseCode
import com.aliucord.utils.DimenUtils.dp
import com.discord.utilities.color.ColorCompat
import com.lytefast.flexinput.R
import b.a.q.n0.a as RtcControlSocket

private val verificationRowId = View.generateViewId()

internal fun addVerificationRow(currentSocket: RtcControlSocket?, root: LinearLayout, userId: Long) {
    if (userId == 0L) return
    val codeView = verificationRow(root) { text ->
        if (text.isNotEmpty() && text != "Unavailable" && text != "…") {
            Utils.setClipboard("Verification Code", text)
            Utils.showToast("Copied to clipboard")
        }
    }
    codeView.text = "…"
    currentSocket?.pairwiseCode(userId.toString()) { c -> codeView.text = c ?: "Unavailable" }
}

private fun verificationRow(root: LinearLayout, onCopy: (String) -> Unit): TextView {
    (root.findViewById<View?>(verificationRowId) as? LinearLayout)
        ?.let { return it.getChildAt(1) as TextView }

    val container = rowContainer(root)
    val ctx = container.context
    val codeView = TextView(ctx).apply {
        setTextColor(ColorCompat.getThemedColor(ctx, R.b.colorTextMuted))
        textSize = 14f
        isSingleLine = true
        typeface = ResourcesCompat.getFont(ctx, Constants.Fonts.sourcecodepro_semibold)
        setPadding(0.dp, 4.dp, 0.dp, 0.dp)
    }

    LinearLayout(ctx).apply {
        this.id = verificationRowId
        orientation = LinearLayout.VERTICAL
        isClickable = true
        isFocusable = true
        setPadding(16.dp, 10.dp, 16.dp, 10.dp)
        layoutParams = LinearLayout.LayoutParams(MATCH_PARENT, WRAP_CONTENT)
        addView(rowTitle(ctx, "Verification Code"))
        addView(codeView)
        setOnClickListener { onCopy(codeView.text?.toString().orEmpty()) }
        container.addView(this)
    }

    return codeView
}
