package com.aliucord.coreplugins.decorations.guildtags

import android.content.Context
import android.content.ContextWrapper
import android.graphics.Typeface
import android.util.TypedValue
import android.view.Gravity
import android.view.View
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import android.widget.LinearLayout
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintLayout.LayoutParams.PARENT_ID
import androidx.fragment.app.FragmentActivity
import com.aliucord.Logger
import com.aliucord.utils.DimenUtils.dp
import com.aliucord.utils.ViewUtils.addTo
import com.discord.api.user.PrimaryGuild
import com.discord.utilities.color.ColorCompat
import com.discord.utilities.icon.IconUtils
import com.facebook.drawee.view.SimpleDraweeView
import com.lytefast.flexinput.R

private val logger = Logger("Decorations/GuildTag")
private val tagViewId = View.generateViewId()

internal class GuildTagView(ctx: Context) : CardView(ctx) {
    private lateinit var badge: SimpleDraweeView
    private lateinit var text: TextView

    fun setSize(sp: Float) {
        val badgeSize = (TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, sp + 2, context.resources.displayMetrics) + 0.5f).toInt()
        badge.layoutParams = LinearLayout.LayoutParams(badgeSize, badgeSize)
        text.textSize = sp
    }


    init {
        visibility = GONE
        radius = 4.dp.toFloat()
        elevation = 0f
        setCardBackgroundColor(ColorCompat.getThemedColor(context, R.b.colorBackgroundSecondary))
        setContentPadding(4.dp, 0.5f.dp, 4.dp, 0.5f.dp)

        LinearLayout(context).addTo(this) {
            layoutParams = ConstraintLayout.LayoutParams(WRAP_CONTENT, WRAP_CONTENT)
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL

            badge = SimpleDraweeView(context).addTo(this)
            text = TextView(context, null, 0, R.i.UiKit_TextView).addTo(this) {
                layoutParams = LinearLayout.LayoutParams(WRAP_CONTENT, WRAP_CONTENT).apply {
                    leftMargin = 2.dp
                }
                setTypeface(typeface, Typeface.BOLD)
                isSingleLine = true
            }
        }

        setSize(12f)
    }

    /* Adds the tag to a constraint layout between two views */
    fun addBetween(parent: ConstraintLayout, left: View, right: View) {
        this.addTo(parent) {
            left.layoutParams = (left.layoutParams as ConstraintLayout.LayoutParams).apply {
                endToStart = id
            }
            right.layoutParams = (right.layoutParams as ConstraintLayout.LayoutParams).apply {
                startToEnd = id
            }

            layoutParams = ConstraintLayout.LayoutParams(WRAP_CONTENT, MATCH_PARENT).apply {
                marginStart = 4.dp
                verticalBias = 0.5f

                topToTop = PARENT_ID
                bottomToBottom = PARENT_ID
                startToEnd = left.id
                endToStart = right.id
            }
        }
    }

    fun configure(data: PrimaryGuild?) {
        setOnClickListener(null)
        visibility = GONE
        if (data == null) return

        val enabled = data.identityEnabled ?: return
        if (!enabled) return

        val tag = data.tag ?: return
        val guildId = data.identityGuildId ?: return
        val badgeHash = data.badge ?: return

        visibility = VISIBLE
        text.text = tag
        IconUtils.setIcon(
            /* view */ badge,
            /* url */ "https://cdn.discordapp.com/guild-tag-badges/${guildId}/${badgeHash}.png",
        )
        setOnClickListener {
            var ctx = context
            while (ctx is ContextWrapper && ctx !is FragmentActivity) {
                ctx = ctx.baseContext
            }
            if (ctx !is FragmentActivity) {
                logger.warn("Could not find fragment manager to show guild profile sheet (${context.javaClass.name}: ${context})")
                return@setOnClickListener
            }
            GuildProfileSheet.show(ctx.supportFragmentManager, guildId)
        }
    }
}
