package com.aliucord.coreplugins.voice.ui

import android.content.Context
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import android.widget.LinearLayout
import android.widget.TextView
import androidx.cardview.widget.CardView
import com.aliucord.utils.DimenUtils.dp
import com.discord.utilities.color.ColorCompat
import com.lytefast.flexinput.R

internal fun cardTitle(ctx: Context, text: String): TextView =
    TextView(ctx, null, 0, R.i.UiKit_ListItem_Icon).apply {
        this.text = text
        setTextColor(ColorCompat.getThemedColor(ctx, R.b.colorHeaderPrimary))
    }

internal fun newCard(ctx: Context, cardId: Int): CardView = CardView(ctx).apply {
    setCardBackgroundColor(ColorCompat.getColor(this, R.c.white_alpha_24))
    radius = 8.dp.toFloat()
    elevation = 0f
    this.id = cardId
    layoutParams = LinearLayout.LayoutParams(MATCH_PARENT, WRAP_CONTENT).apply {
        bottomMargin = 16.dp
    }
}
