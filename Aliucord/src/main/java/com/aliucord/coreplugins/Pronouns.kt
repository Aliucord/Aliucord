package com.aliucord.coreplugins

import android.content.Context
import android.util.TypedValue
import android.view.View
import android.widget.TextView
import android.widget.LinearLayout
import androidx.core.content.res.ResourcesCompat
import com.aliucord.Constants
import com.aliucord.Utils
import com.aliucord.api.rn.user.RNUserProfile
import com.aliucord.entities.Plugin
import com.aliucord.patcher.after
import com.discord.utilities.view.text.SimpleDraweeSpanTextView
import com.discord.widgets.user.profile.UserProfileHeaderView
import com.discord.widgets.user.profile.UserProfileHeaderViewModel

val sheetProfileHeaderViewId = Utils.getResId("user_sheet_profile_header_view", "id")
val userProfileHeaderSecondaryNameViewId = Utils.getResId("user_profile_header_secondary_name", "id")

val pronounsViewId = View.generateViewId()

internal class Pronouns : Plugin(Manifest("Pronouns")) {
    override fun load(context: Context) {
        patcher.after<UserProfileHeaderView>("configureSecondaryName", UserProfileHeaderViewModel.ViewState.Loaded::class.java) {
            val state = it.args[0] as? UserProfileHeaderViewModel.ViewState.Loaded ?: return@after
            val view = it.thisObject as UserProfileHeaderView

            val profile = state.userProfile as? RNUserProfile ?: return@after
            val pronouns = profile.guildMemberProfile?.pronouns?.ifEmpty { null }
                ?: profile.userProfile?.pronouns?.ifEmpty { null }
                ?: return@after

            if (view.id == sheetProfileHeaderViewId) {
                val secondaryNameView = view.findViewById<SimpleDraweeSpanTextView>(userProfileHeaderSecondaryNameViewId)
                val layout = secondaryNameView.parent as LinearLayout

                layout.findViewById(pronounsViewId) ?: TextView(layout.context, null, 0, com.lytefast.flexinput.R.i.UiKit_TextView_Semibold).apply {
                    id = pronounsViewId
                    typeface = ResourcesCompat.getFont(layout.context, Constants.Fonts.whitney_semibold)
                    setTextColor(secondaryNameView.currentTextColor)
                    setTextSize(TypedValue.COMPLEX_UNIT_PX, secondaryNameView.textSize)
                    text = pronouns

                    layout.addView(this, layout.indexOfChild(secondaryNameView) + 1)
                }
            }
        }
    }

    override fun start(context: Context?) {}

    override fun stop(context: Context?) {}
}
