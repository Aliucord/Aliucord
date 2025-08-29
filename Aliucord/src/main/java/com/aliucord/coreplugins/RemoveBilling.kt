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

internal class RemoveBilling : CorePlugin(Manifest("RemoveBilling")) {
    init {
        manifest.description = "Removes all references to billing"
    }

    override fun start(context: Context) {
        // Remove Billing settings
        patcher.after<WidgetSettings>("onViewBound", View::class.java) { (_, view: View) ->
            var container: LinearLayout = view.findViewById(Utils.getResId("nitro_settings_container", "id"))
            var ids = arrayOf("settings_nitro", "nitro_boosting", "nitro_header")
            for (i in ids) {
                container.removeView(view.findViewById(Utils.getResId(i, "id")))
            }
            // Remove Billing settings divider
            container.removeViewAt(0)
            // Remove the Nitro gifting setting
            removeTextView(view, Utils.getResId("nitro_gifting", "id"))
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
                removeTextView(view, Utils.getResId(i, "id"))
            }
        }
        // Remove "Get Nitro" button from when trying to upload a custom banner
        patcher.after<d>("onViewBound", View::class.java) { (_, view: View) ->
            var view = view.findViewById<RelativeLayout>(Utils.getResId("get_premium_button", "id"))
            view.setVisibility(View.GONE)
        }
        // Remove "Get Nitro" button from when trying to use an emoji from another server
        patcher.after<c>("onViewBound", View::class.java) { (_, view: View) ->
            var view = view.findViewById<View>(Utils.getResId("premium_upsell_get_premium", "id"))
            view.setVisibility(View.GONE)
        }
    }

    override fun stop(context: Context) = patcher.unpatchAll()

    private fun removeTextView(rootView: View, textViewId: Int) {
        val textView = rootView.findViewById<TextView>(textViewId)
        textView.setVisibility(View.GONE)
    }
}
