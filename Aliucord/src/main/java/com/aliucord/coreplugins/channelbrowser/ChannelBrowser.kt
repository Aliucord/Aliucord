package com.aliucord.coreplugins.channelbrowser

import android.annotation.SuppressLint
import android.content.Context
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.core.content.res.ResourcesCompat
import androidx.recyclerview.widget.RecyclerView
import com.aliucord.Constants
import com.aliucord.Utils
import com.aliucord.annotations.AliucordPlugin
import com.aliucord.entities.CorePlugin
import com.aliucord.patcher.after
import com.aliucord.utils.ReflectUtils
import com.discord.databinding.WidgetGuildProfileSheetBinding
import com.discord.utilities.color.ColorCompat
import com.discord.widgets.channels.list.`WidgetChannelListModel$Companion$guildListBuilder$$inlined$forEach$lambda$3`
import com.discord.widgets.guilds.profile.WidgetGuildProfileSheet
import com.discord.widgets.guilds.profile.WidgetGuildProfileSheetViewModel
import com.lytefast.flexinput.R

@AliucordPlugin
internal class ChannelBrowser: CorePlugin(Manifest("ChannelBrowser")) {
    init {
        manifest.description = "Adds the Manage Channels feature, which allows you to hide or to show channels on a specific guild."
    }

    override fun start(context: Context) {
        settingsTab = SettingsTab(ChannelBrowserSettings::class.java).withArgs(settings)

        patcher.after<`WidgetChannelListModel$Companion$guildListBuilder$$inlined$forEach$lambda$3`>("invoke") {
            val channel = `$channel`
            val channelId = try {
                channel.javaClass.getDeclaredField("id").apply { isAccessible = true }.get(channel) as? Long
            } catch (_: Throwable) {
                null
            }
            val hiddenChannels = settings.getObject("hiddenChannels", mutableListOf<String>()) as MutableList<String>
            if (channelId != null && hiddenChannels.contains(channelId.toString())) it.result = null
        }

        patcher.after<WidgetGuildProfileSheet>(
            "configureTabItems", Long::class.java,
            WidgetGuildProfileSheetViewModel.TabItems::class.java, Boolean::class.java
        ) {
            val binding = ReflectUtils.getMethodByArgs(WidgetGuildProfileSheet::class.java, "getBinding")
                .invoke(this) as WidgetGuildProfileSheetBinding

            val layout = binding.f.getRootView() as ViewGroup
            val primaryActions = layout.findViewById<CardView>(
                Utils.getResId("guild_profile_sheet_secondary_actions", "id")
            )
            val lay = primaryActions.getChildAt(0) as LinearLayout

            val alreadyHasBrowse = (0 until lay.childCount).any {
                val v = lay.getChildAt(it)
                v is TextView && v.text?.toString()?.contains("Browse Channels") == true
            }

            if (!alreadyHasBrowse) {
                val changeNicknameId = Utils.getResId("guild_profile_sheet_change_nickname", "id")
                val changeIdentityId = Utils.getResId("change_identity", "id")
                val changeNicknameView = lay.findViewById<View?>(changeNicknameId)
                val changeIdentityView = lay.findViewById<View?>(changeIdentityId)
                val insertIndex = when {
                    changeNicknameView != null -> lay.indexOfChild(changeNicknameView)
                    changeIdentityView != null -> lay.indexOfChild(changeIdentityView)
                    else -> 0
                }

                val styleId = Utils.getResId("GuildProfileSheet.Actions.Title", "style")
                val scale = context.resources.displayMetrics.density
                val pd = (16 * scale).toInt()

                val browseTv = TextView(lay.context, null, 0, styleId).apply {
                    setPadding(pd, pd, pd, pd)
                    typeface = ResourcesCompat.getFont(context, Constants.Fonts.whitney_semibold)
                    text = "Browse Channels"
                    textSize = 16f
                    gravity = android.view.Gravity.CENTER_VERTICAL
                    layoutParams = LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                    )
                    setOnClickListener {
                        Utils.openPageWithProxy(lay.context, ChannelBrowserPage(settings))
                    }
                }

                lay.addView(browseTv, insertIndex)
            }
        }
    }

    private class ChannelBrowserHeaderAdapter(
        val onClick: () -> Unit
    ) : RecyclerView.Adapter<ChannelBrowserHeaderAdapter.VH>() {

        class VH(val row: LinearLayout) : RecyclerView.ViewHolder(row)

        @SuppressLint("UseKtx")
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
            val ctx = parent.context
            val scale = ctx.resources.displayMetrics.density
            val minH = (48 * scale).toInt()
            val sidePadding = (8 * scale).toInt()

            val row = LinearLayout(ctx).apply {
                layoutParams = RecyclerView.LayoutParams(
                    RecyclerView.LayoutParams.MATCH_PARENT,
                    RecyclerView.LayoutParams.WRAP_CONTENT
                )
                orientation = LinearLayout.HORIZONTAL
                gravity = android.view.Gravity.CENTER_VERTICAL
                setPadding(sidePadding, 0, sidePadding, 0)
                minimumHeight = minH

                val attrs = intArrayOf(android.R.attr.selectableItemBackground)
                val typedArray = ctx.obtainStyledAttributes(attrs)
                background = typedArray.getDrawable(0)
                typedArray.recycle()
            }

            val icon = ImageView(ctx).apply {
                val resId = try {
                    R.e.ic_menu_24dp
                } catch (_: Throwable) {
                    android.R.drawable.ic_menu_sort_by_size
                }
                val drawable = androidx.core.content.ContextCompat.getDrawable(ctx, resId)?.mutate()
                try {
                    val color = ColorCompat.getThemedColor(ctx, R.b.colorInteractiveNormal)
                    drawable?.setTint(color)
                } catch (_: Throwable) {}
                setImageDrawable(drawable)
                val size = (24 * scale).toInt()
                layoutParams = LinearLayout.LayoutParams(size, size).apply {
                    leftMargin = (6 * scale).toInt()
                    rightMargin = (6 * scale).toInt()
                    gravity = android.view.Gravity.CENTER_VERTICAL
                }
            }
            row.addView(icon)

            val tv = TextView(ctx, null, 0, R.i.UiKit_Settings_Item).apply {
                text = "Browse Channels"
                typeface = ResourcesCompat.getFont(ctx, Constants.Fonts.whitney_medium)
                textSize = 16f
                val color = try {
                    androidx.core.content.ContextCompat.getColor(ctx, try { R.c.primary_dark } catch (_: Throwable) { android.R.color.black })
                } catch (_: Throwable) {
                    0xFF000000.toInt()
                }
                setTextColor(color)
                gravity = android.view.Gravity.CENTER_VERTICAL
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply {
                    leftMargin = 0
                }
                setPadding(0, 0, 0, 0)
            }
            row.addView(tv)

            return VH(row)
        }

        override fun onBindViewHolder(holder: VH, position: Int) {
            holder.row.setOnClickListener { onClick() }
        }

        override fun getItemCount(): Int = 1
        override fun getItemViewType(position: Int): Int = 10001
    }
}
