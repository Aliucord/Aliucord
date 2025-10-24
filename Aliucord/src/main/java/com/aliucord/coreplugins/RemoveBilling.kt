package com.aliucord.coreplugins

import android.content.Context
import android.view.View
import android.view.ViewGroup
import android.widget.*
import b.a.a.b.c
import b.a.a.c as ImageUploadFailedView
import b.a.a.b.d
import com.aliucord.entities.CorePlugin
import com.aliucord.patcher.*
import com.discord.databinding.WidgetEmojiSheetBinding
import com.discord.models.guild.Guild
import com.discord.stores.StoreStream
import com.discord.utilities.user.UserUtils
import com.discord.widgets.emoji.WidgetEmojiSheet
import com.discord.widgets.servers.guildboost.WidgetGuildBoost
import com.discord.widgets.settings.WidgetSettings
import com.discord.widgets.settings.WidgetSettingsMedia
import com.discord.widgets.settings.premium.`WidgetSettingsGifting$binding$2`
import com.discord.widgets.stickers.UnsendableStickerPremiumUpsellDialog
import com.lytefast.flexinput.fragment.FlexInputFragment
import com.lytefast.flexinput.fragment.`FlexInputFragment$d`
import com.aliucord.utils.ViewUtils.findViewById

internal class RemoveBilling : CorePlugin(Manifest("RemoveBilling")) {
    override val isHidden = true

    init {
        manifest.description = "Removes references to billing/nitro when user doesn't have nitro"
    }

    override fun start(context: Context) {
        val meUser = StoreStream.getUsers().getMe()
        val userUtils = UserUtils.INSTANCE
        val hasNitro = userUtils.isPremium(meUser)

        // Don't patch if the user has nitro, Theres some functionalities like unsubscribing/seeing nitro status that still work.
        if (hasNitro) return

        // Remove Billing settings
        patcher.after<WidgetSettings>("onViewBound", View::class.java) { (_, view: View) ->
            val container = view.findViewById<LinearLayout>("nitro_settings_container")
            for (id in arrayOf("settings_nitro", "nitro_boosting", "nitro_header")) {
                container.removeView(view.findViewById(id))
            }
            // Remove Billing settings divider
            container.removeViewAt(0)
        }

        // Remove Gift button
        patcher.after<`FlexInputFragment$d`>("invoke", Any::class.java) {
			val fragment = this.receiver as FlexInputFragment
			val binding = fragment.j() ?: return@after
			binding.m.visibility = View.GONE
		}

        // Remove boost/subscribe buttons on Boosting page
        patcher.after<WidgetGuildBoost>("onViewBound", View::class.java) { (_, view: View) ->
            val ids = arrayOf("boost_status_subscribe_button", "boost_status_protip", "boost_status_subscribe_button2", "view_premium_marketing_learn_more", "menu_premium_guild")
            for (i in ids) {
                view.findViewById<TextView>(i).apply { visibility = View.GONE }
            }
        }

        // Remove the "Get Nitro" button from when trying to upload a custom banner
        patcher.after<d>("onViewBound", View::class.java) { (_, view: View) ->
            view.findViewById<RelativeLayout>("get_premium_button").apply { visibility = View.GONE }
        }

        // Remove the "Get Nitro" button from when trying to use an emoji from another server
        patcher.after<c>("onViewBound", View::class.java) { (_, view: View) ->
            view.findViewById<View>("premium_upsell_get_premium").apply { visibility = View.GONE }
            // Remove the "Learn more" button from when trying to use an animated avatar
            view.findViewById<View>("premium_upsell_learn_more").apply { visibility = View.GONE }
        }

        // Remove the "Subscribe" button from when trying to use a sticker from another server
        patcher.after<UnsendableStickerPremiumUpsellDialog>("onViewBound", View::class.java) { (_, view: View) ->
            view.findViewById<View>("sticker_premium_upsell_subscribe_button").apply { visibility = View.GONE }
        }

        // Remove the Nitro advertisement on the "Auto-compress Images" toggle
        patcher.after<WidgetSettingsMedia>("onViewBound", View::class.java) { (_, view: View) ->
            view.findViewById<TextView>("compression_toggle_subtext").apply { visibility = View.GONE }
        }

        // Remove some nonfunctional elements in the Gifting page
        patcher.after<`WidgetSettingsGifting$binding$2`>("invoke", View::class.java) { (_, view: View) ->
            view.findViewById<LinearLayout>("settings_gifting_purchase_gift_section").apply { visibility = View.GONE }
        }

        // Remove the "Learn about Nitro" button when a huge file is uploaded
        patcher.after<ImageUploadFailedView>("onViewBound", View::class.java) { (_, view: View) ->
            view.findViewById<RelativeLayout>("image_upload_failed_nitro_wrapper")
                .apply { visibility = View.GONE }
        }

        // Remove "Get Nitro" button when holding down on emojis
        val getBinding = WidgetEmojiSheet::class.java.getDeclaredMethod("getBinding")
            .apply { isAccessible = true }

        patcher.after<WidgetEmojiSheet>("configureButtons", Boolean::class.java, Boolean::class.java, Guild::class.java) { param ->
            val binding = getBinding.invoke(param.thisObject) as WidgetEmojiSheetBinding
            val root = binding.root as ViewGroup
            val rootLayout = root.getChildAt(0) as LinearLayout
            rootLayout.removeViewAt(1)
        }
    }

    override fun stop(context: Context) = patcher.unpatchAll()
}
