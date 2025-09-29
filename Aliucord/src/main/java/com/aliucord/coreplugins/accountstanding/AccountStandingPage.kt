/*
 * This file is part of Aliucord, an Android Discord client mod.
 * Copyright (c) 2025 Juby210 & Vendicated
 * Licensed under the Open Software License version 3.0
 */

package com.aliucord.coreplugins

import android.text.SpannableStringBuilder
import android.view.Gravity
import android.view.View
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import com.aliucord.Constants
import com.aliucord.Utils
import com.aliucord.fragments.SettingsPage
import com.aliucord.utils.DimenUtils
import com.aliucord.utils.MDUtils
import com.aliucord.views.DangerButton
import com.lytefast.flexinput.R
import java.io.File
import java.util.*
import com.aliucord.Http
import com.aliucord.Http.*
import com.aliucord.utils.*
import com.aliucord.Logger

data class ApiResponse(
    var account_standing: accountStandingState,
    var classifications: List<userClassifications>
) {
    data class userClassifications(val description: String)
    data class accountStandingState(val state: Int)
}

class AccountStandingPage : SettingsPage() {
    override fun onViewBound(view: View) {
        super.onViewBound(view)
        setActionBarTitle("Checking account standing..")

        val context = view.context

        Utils.threadPool.execute {
            try {
                val req = Http.Request.newDiscordRNRequest("/safety-hub/@me", "GET")
                val res = req.execute()
                val json = res.json(GsonUtils.gsonRestApi, ApiResponse::class.java)
                val number = json.account_standing.state
                var string: String? = null
                var classificationsareEmpty = if (json.classifications.isEmpty()) true else false
                var user_classifications = if (!classificationsareEmpty) json.classifications.first() else null
                var violation = if (!classificationsareEmpty) user_classifications!!.description else null
                when (number) {
                    100 -> string = "No violations found :)"
                    200 -> string = "Your account seems limited :|"
                    300 -> string = "Your account is very limited :|"
                    400 -> string = "Your account is at risk of getting banned :P"
                    500 -> string = "Your account is banned :("
                    else -> string = "Failed to check :|"
                }
                Utils.mainThread.post {
                    setActionBarTitle("Account Standing")
                    TextView(context, null, 0, R.i.UiKit_Settings_Item_SubText).run {
                        if (!classificationsareEmpty) text = string + " You've broke the rules for $violation (there might be some more aswell)" else text = string
                        typeface = ResourcesCompat.getFont(context, Constants.Fonts.whitney_medium)
                        gravity = Gravity.CENTER
                        linearLayout.addView(this)
                    }
                }
            } catch (e: Exception) {
                Logger("AccountStanding").warn("Failed to check account standing", e)
            }
        }
    }
}
