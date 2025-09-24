package com.aliucord.coreplugins

import android.content.Context
import android.view.*
import android.widget.*
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.aliucord.Utils
import com.aliucord.entities.CorePlugin
import com.aliucord.patcher.*
import com.lytefast.flexinput.R
import com.discord.widgets.settings.WidgetSettings
import com.lytefast.flexinput.fragment.FlexInputFragment
import com.lytefast.flexinput.fragment.`FlexInputFragment$d`
import com.discord.widgets.servers.guildboost.WidgetGuildBoost
import com.discord.widgets.settings.profile.WidgetEditProfileBannerSheet
import b.a.a.b.d
import b.a.a.b.c
import com.discord.widgets.stickers.UnsendableStickerPremiumUpsellDialog
import com.discord.widgets.settings.WidgetSettingsMedia
import com.discord.widgets.settings.premium.`WidgetSettingsGifting$binding$2`

internal class RemoveBilling : CorePlugin(Manifest("RemoveBilling")) {
    override val isHidden = true

    init {
        manifest.description = "Removes all references to billing and nitro"
    }

    override fun start(context: Context) {
        // Remove Billing settings
        patcher.after<WidgetSettings>("onViewBound", View::class.java) { (_, view: View) ->
            var container = view.findViewById<LinearLayout>(Utils.getResId("nitro_settings_container", "id"))
            for (id in arrayOf("settings_nitro", "nitro_boosting")) {
                container.removeView(view.findViewById(Utils.getResId(id, "id")))
            }
        }

        // Remove Gift button
        patcher.after<`FlexInputFragment$d`>("invoke", Any::class.java) {
			val fragment = this.receiver as FlexInputFragment
			val binding = fragment.j() ?: return@after
			binding.m.visibility = View.GONE
		}

        // Remove boost/subscribe buttons on Boosting page
        patcher.after<WidgetGuildBoost>("onViewBound", View::class.java) { (_, view: View) ->
            var ids = arrayOf("boost_status_subscribe_button", "boost_status_protip", "boost_status_subscribe_button2", "view_premium_marketing_learn_more", "menu_premium_guild")
            for (i in ids) {
                val textview = view.findViewById<TextView>(Utils.getResId(i, "id")); textview.setVisibility(View.GONE)
            }
        }

        // Remove the "Get Nitro" button from when trying to upload a custom banner
        patcher.after<d>("onViewBound", View::class.java) { (_, view: View) ->
            val textview = view.findViewById<RelativeLayout>(Utils.getResId("get_premium_button", "id")); textview.setVisibility(View.GONE)
        }

        // Remove the "Get Nitro" button from when trying to use an emoji from another server
        patcher.after<c>("onViewBound", View::class.java) { (_, view: View) ->
            val textview = view.findViewById<View>(Utils.getResId("premium_upsell_get_premium", "id")); textview.setVisibility(View.GONE)
            // Remove the "Learn more" button from when trying to use an animated avatar
            val textview2 = view.findViewById<View>(Utils.getResId("premium_upsell_learn_more", "id")); textview2.setVisibility(View.GONE)
        }

        // Remove the "Subscribe" button from when trying to use a sticker from another server
        patcher.after<UnsendableStickerPremiumUpsellDialog>("onViewBound", View::class.java) { (_, view: View) ->
            val textview = view.findViewById<View>(Utils.getResId("sticker_premium_upsell_subscribe_button", "id")); textview.setVisibility(View.GONE)
        }

        // Remove the Nitro advertisement on the "Auto-compress Images" toggle
        patcher.after<WidgetSettingsMedia>("onViewBound", View::class.java) { (_, view: View) ->
            val textview = view.findViewById<TextView>(Utils.getResId("compression_toggle_subtext", "id")); textview.setVisibility(View.GONE)
        }

        // Remove some nonfunctional elements in the Gifting page
        patcher.after<`WidgetSettingsGifting$binding$2`>("invoke", View::class.java) { (_, view: View) ->
            val textview = view.findViewById<LinearLayout>(Utils.getResId("settings_gifting_purchase_gift_section", "id")); textview.setVisibility(View.GONE)
        }

    }

    override fun stop(context: Context) = patcher.unpatchAll()
}
