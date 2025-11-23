package com.aliucord.coreplugins.accountstanding

import com.aliucord.utils.SerializedName
import com.discord.api.utcdatetime.UtcDateTime

internal data class AccountStandingPageResponse(
    @SerializedName("account_standing")
    val accountStanding: AccountStandingState,
    val classifications: List<UserClassifications>
) {
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
    data class AccountStandingState(val state: Int) {
        val headerString: String
            get() = when (state) {
                100 -> "Your account is all good"
                200 -> "Your account is limited"
                300 -> "Your account is very limited."
                400 -> "Your account is at risk"
                500 -> "Your account is suspended."
                else -> "Unknown"
            }

        val bodyString: String
            get() = when (state) {
                100 -> "Thank you for upholding Discord's Terms of Service and Community Guidelines. If you break the rules, it will show up here."
                200 -> "You may lose access to some parts of Discord if you break the rules again."
                300 -> "You can't use some parts of Discord, You may be suspended if you break the rules again."
                400 -> "You broke Discord's rules. You will be permanently suspended if you break them again."
                500 -> "Due to serious policy violations, your account is permanently suspended, You can no longer use Discord."
                else -> "Unknown"
            }
    }
}

// Only really store the classification IDs on the cache with the plugin
internal data class AccountStandingResponse(
    val classifications: List<Classifications>?
) {
    data class Classifications(
        val id: Long
    )
}
