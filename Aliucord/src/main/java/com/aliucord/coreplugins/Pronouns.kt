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
import com.aliucord.entities.CorePlugin
import com.aliucord.patcher.after
import com.discord.utilities.view.text.SimpleDraweeSpanTextView
import com.discord.widgets.user.profile.UserProfileHeaderView
import com.discord.widgets.user.profile.UserProfileHeaderViewModel
import com.lytefast.flexinput.R

internal class Pronouns : CorePlugin(Manifest("Pronouns")) {
    private val sheetProfileHeaderViewId = Utils.getResId("user_sheet_profile_header_view", "id")
    private val userProfileHeaderSecondaryNameViewId = Utils.getResId("user_profile_header_secondary_name", "id")
    private val pronounsViewId = View.generateViewId()

    init {
        manifest.description = "Display the new pronouns feature on user profiles"
    }

    override fun start(context: Context) {
        patcher.after<UserProfileHeaderView>("configureSecondaryName", UserProfileHeaderViewModel.ViewState.Loaded::class.java) {
            if (id != sheetProfileHeaderViewId) return@after
            val state = it.args[0] as? UserProfileHeaderViewModel.ViewState.Loaded ?: return@after

            val profile = state.userProfile as? RNUserProfile ?: return@after
            val pronouns = profile.guildMemberProfile?.pronouns?.ifEmpty { null }
                ?: profile.userProfile?.pronouns?.ifEmpty { null }
                ?: return@after

            val secondaryNameView = findViewById<SimpleDraweeSpanTextView>(userProfileHeaderSecondaryNameViewId)
            val layout = secondaryNameView.parent as LinearLayout

            layout.findViewById(pronounsViewId) ?: TextView(layout.context, null, 0, R.i.UiKit_TextView_Semibold).apply {
                id = pronounsViewId
                typeface = ResourcesCompat.getFont(layout.context, Constants.Fonts.whitney_semibold)
                setTextColor(secondaryNameView.currentTextColor)
                setTextSize(TypedValue.COMPLEX_UNIT_PX, secondaryNameView.textSize)
                text = pronouns

                layout.addView(this, layout.indexOfChild(secondaryNameView) + 1)
            }
        }
    }

    override fun stop(context: Context) = patcher.unpatchAll()
}
