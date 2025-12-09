package com.aliucord.coreplugins.decorations.nameplate

import android.graphics.drawable.GradientDrawable
import android.view.View
import androidx.constraintlayout.widget.*
import androidx.core.content.ContextCompat
import androidx.core.graphics.ColorUtils
import com.aliucord.coreplugins.decorations.Decorator
import com.aliucord.utils.DimenUtils.dp
import com.aliucord.utils.ViewUtils.addTo
import com.aliucord.utils.ViewUtils.endPadding
import com.aliucord.utils.ViewUtils.leftPadding
import com.aliucord.utils.ViewUtils.rightPadding
import com.aliucord.utils.accessField
import com.aliucord.wrappers.users.collectibles
import com.discord.api.user.Collectibles
import com.discord.databinding.WidgetChannelMembersListItemUserBinding
import com.discord.stores.StoreStream
import com.discord.utilities.color.ColorCompat
import com.discord.utilities.drawable.DrawableCompat
import com.discord.utilities.icon.IconUtils
import com.discord.widgets.channels.memberlist.adapter.ChannelMembersListAdapter
import com.discord.widgets.channels.memberlist.adapter.ChannelMembersListViewHolderMember
import com.facebook.drawee.view.SimpleDraweeView
import com.lytefast.flexinput.R

private val ChannelMembersListViewHolderMember.binding by accessField<WidgetChannelMembersListItemUserBinding>()

private const val decoAlpha = 0.6f
private const val PARENT_ID = ConstraintLayout.LayoutParams.PARENT_ID

internal class NameplateDecorator() : Decorator() {
    private val decoId = View.generateViewId()
    private val solidMaskId = View.generateViewId()
    private val gradientMaskId = View.generateViewId()
    private val barrierId = View.generateViewId()
    private val rippleId = View.generateViewId()
    private val guidelineId = View.generateViewId()

    private fun addDecoViews(
        layout: ConstraintLayout,
        maskAnchors: IntArray,
        params: ConstraintLayout.LayoutParams,
        startIndex: Int = 0,
    ) {
        var idx = startIndex
        val context = layout.context

        // Add main nameplate view (foreground: nameplate, background: palette gradient)
        SimpleDraweeView(context).addTo(layout, idx++) {
            id = decoId
            alpha = decoAlpha
            layoutParams = params
        }

        // Find background colour of members list for masking
        // Ideally ColorCompat.getThemedColor(context, R.b.colorBackgroundPrimary), but doesn't work with themer
        val colorId = when (StoreStream.getUserSettingsSystem().theme) {
            "light" -> R.c.white_500
            "dark" -> R.c.primary_dark_600
            "pureEvil" -> R.c.black
            else -> R.c.primary_dark_600
        }
        val color = ColorCompat.getColor(context, colorId)
            .let { ColorUtils.setAlphaComponent(it, 200) } // Make mask colour slightly translucent

        // A barrier at the end of relevant foreground elements (username, status, etc..)
        Barrier(context).addTo(layout, idx++) {
            id = barrierId
            type = Barrier.END
            referencedIds = maskAnchors
        }
        // A solid mask to keep foreground text legible
        View(context).addTo(layout, idx++) {
            id = solidMaskId
            layoutParams = ConstraintLayout.LayoutParams(0, 0).apply {
                startToStart = PARENT_ID
                endToStart = barrierId
                topToTop = decoId
                bottomToBottom = decoId
                rightMargin = 48.dp
            }

            setBackgroundColor(color)
        }
        // A gradient mask to transition from translucent to transparent
        View(context).addTo(layout, idx++) {
            id = gradientMaskId
            layoutParams = ConstraintLayout.LayoutParams(96.dp, 0).apply {
                startToEnd = solidMaskId
                topToTop = decoId
                bottomToBottom = decoId
            }

            background = GradientDrawable(GradientDrawable.Orientation.LEFT_RIGHT, intArrayOf(color, 0))
        }
        // An element to retain the ripple effect when clicking
        View(context).addTo(layout, idx++) {
            id = rippleId
            layoutParams = ConstraintLayout.LayoutParams(0, 0).apply {
                startToStart = PARENT_ID
                endToEnd = PARENT_ID
                topToTop = decoId
                bottomToBottom = decoId
            }

            val res = DrawableCompat.getThemedDrawableRes(context, R.b.selectableItemBackground)
            foreground = ContextCompat.getDrawable(context, res)
        }
    }

    private fun findAndConfigure(parent: View, data: Collectibles.Nameplate?) {
        val visible = if (data != null) View.VISIBLE else View.INVISIBLE
        parent.findViewById<View>(solidMaskId)?.visibility = visible
        parent.findViewById<View>(gradientMaskId)?.visibility = visible
        parent.findViewById<View>(rippleId)?.visibility = visible
        parent.findViewById<SimpleDraweeView>(decoId)?.run {
            visibility = visible
            if (data != null) {
                IconUtils.setIcon(
                    this,
                    "https://cdn.discordapp.com/assets/collectibles/${data.asset}img.png?passthrough=true",
                )
                background = Palette.from(data.palette).drawable()
            }
        }
    }

    override fun onMembersListInit(
        holder: ChannelMembersListViewHolderMember,
        binding: WidgetChannelMembersListItemUserBinding
    ) {
        val layout = binding.a
        val avatarView = binding.b
        val boostedIndicator = binding.c
        val gameView = binding.d
        val ownerIndicator = binding.e
        val usernameView = binding.f
        val rpcIconView = binding.h

        // Guideline to limit width of username and status text
        Guideline(layout.context).addTo(layout) {
            id = guidelineId
            layoutParams = ConstraintLayout.LayoutParams(0, 0).apply {
                orientation = ConstraintLayout.LayoutParams.VERTICAL
                guideEnd = 32.dp
            }
        }

        // Clear the layout's padding and add our own
        layout.leftPadding = 0
        layout.rightPadding = 0
        usernameView.endPadding = 4.dp
        ConstraintSet().run {
            clone(layout)

            val start = ConstraintSet.START
            val end = ConstraintSet.END
            val right = ConstraintSet.RIGHT
            val top = ConstraintSet.TOP
            val bottom = ConstraintSet.BOTTOM

            setMargin(avatarView.id, start, 16.dp)

            // Move the owner indicator so it doesn't block nameplates
            connect(ownerIndicator.id, top, usernameView.id, top)
            connect(ownerIndicator.id, bottom, usernameView.id, bottom)
            connect(ownerIndicator.id, start, usernameView.id, end)
            setDimensionRatio(ownerIndicator.id, "W,1:1")
            constrainedWidth(ownerIndicator.id, true)

            // Move the boosted indicator so it doesn't block nameplates
            connect(boostedIndicator.id, top, usernameView.id, top)
            connect(boostedIndicator.id, bottom, usernameView.id, bottom)
            connect(boostedIndicator.id, start, ownerIndicator.id, end)
            setDimensionRatio(boostedIndicator.id, "W,1:1")
            constrainedWidth(boostedIndicator.id, true)

            // Fixup some extraneous constraints
            clear(boostedIndicator.id, right)
            clear(boostedIndicator.id, end)
            clear(usernameView.id, right)
            clear(usernameView.id, end)
            clear(gameView.id, right)
            clear(gameView.id, end)
            clear(rpcIconView.id, right)
            clear(rpcIconView.id, end)

            // Set width limits for username and status
            connect(usernameView.id, end, guidelineId, end)
            connect(gameView.id, end, rpcIconView.id, start)
            connect(rpcIconView.id, end, guidelineId, end)

            applyTo(layout)
        }

        // Add the nameplate views
        addDecoViews(
            layout,
            intArrayOf(usernameView.id, gameView.id),
            ConstraintLayout.LayoutParams(0, 0).apply {
                dimensionRatio = "W,16:3"
                endToEnd = PARENT_ID
                topToTop = PARENT_ID
                bottomToBottom = PARENT_ID
                topMargin = 1.dp
                bottomMargin = 1.dp
            }
        )
    }

    override fun onMembersListConfigure(
        holder: ChannelMembersListViewHolderMember,
        item: ChannelMembersListAdapter.Item.Member,
        adapter: ChannelMembersListAdapter
    ) {
        val layout = holder.binding.a

        val user = StoreStream.getUsers().users[item.userId]
        val member = StoreStream.getGuilds().getMember(item.guildId ?: -1, item.userId)
        val data = member?.collectibles?.nameplate ?: user?.collectibles?.nameplate
        findAndConfigure(layout, data)
    }
}
