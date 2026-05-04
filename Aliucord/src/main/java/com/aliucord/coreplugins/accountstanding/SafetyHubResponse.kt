package com.aliucord.coreplugins.accountstanding

import com.aliucord.Utils
import com.aliucord.utils.SerializedName
import com.discord.api.utcdatetime.UtcDateTime
import com.lytefast.flexinput.R

internal data class AccountStandingPageResponse(
    @SerializedName("account_standing")
    val accountStanding: AccountStandingState,
    val classifications: List<UserClassifications>
) {
    data class AccountStandingState(val state: Int)
    data class UserClassifications(
        val id: Long,
        val description: String,
        @SerializedName("flagged_content")
        val flaggedContent: List<FlaggedContent>?,
        val actions: List<Actions>?,
        @SerializedName("max_expiration_time")
        val maxExpirationTime: UtcDateTime
    )

    data class Actions(val descriptions: List<String>)
    data class FlaggedContent(val content: String?)

    enum class AccountStanding(
        val state: Int,
        val header: String,
        val body: String,
        private val colorRes: Int
    ) {
        ALL_GOOD(
            100,
            "Your account is all good",
            "Thank you for upholding Discord's Terms of Service and Community Guidelines. If you break the rules, it will show up here.",
            R.c.uikit_btn_bg_color_selector_green
        ),
        LIMITED(
            200,
            "Your account is limited",
            "You may lose access to some parts of Discord if you break the rules again.",
            R.c.status_yellow
        ),
        VERY_LIMITED(
            300,
            "Your account is very limited",
            "You can't use some parts of Discord. You may be suspended if you break the rules again.",
            R.c.status_orange
        ),
        AT_RISK(
            400,
            "Your account is at risk",
            "You broke Discord's rules. You will be permanently suspended if you break them again.",
            R.c.uikit_btn_bg_color_selector_red
        ),
        SUSPENDED(
            500,
            "Your account is suspended",
            "Due to serious policy violations, your account is permanently suspended. You can no longer use Discord.",
            R.c.uikit_btn_bg_color_selector_red
        ),
        UNKNOWN(-1, "Unknown", "Unknown", R.c.uikit_btn_bg_color_selector_red);

        val color: Int get() = Utils.appContext.getColor(colorRes)

        companion object {
            fun from(state: Int) = entries.firstOrNull { it.state == state } ?: UNKNOWN
        }
    }
}

