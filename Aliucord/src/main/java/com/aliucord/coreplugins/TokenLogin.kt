/*
 * This file is part of Aliucord, an Android Discord client mod.
 * Copyright (c) 2021 Juby210 & Vendicated
 * Licensed under the Open Software License version 3.0
 */
package com.aliucord.coreplugins

import android.content.Context
import android.view.View
import android.widget.LinearLayout
import android.widget.RelativeLayout
import com.aliucord.Utils.getResId
import com.aliucord.Utils.openPage
import com.aliucord.entities.CorePlugin
import com.aliucord.patcher.Hook
import com.aliucord.patcher.Patcher
import com.aliucord.utils.DimenUtils.dpToPx
import com.aliucord.views.Button
import com.discord.api.auth.RequiredAction
import com.discord.app.AppActivity
import com.discord.app.AppFragment
import com.discord.models.domain.auth.ModelLoginResult
import com.discord.stores.StoreAuthentication
import com.discord.stores.StoreStream
import com.discord.utilities.view.extensions.ViewExtensions
import com.discord.widgets.auth.WidgetAuthLanding
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputLayout
import com.lytefast.flexinput.R
import de.robv.android.xposed.XC_MethodHook.MethodHookParam
import rx.functions.Action1

internal class TokenLogin : CorePlugin(Manifest("TokenLogin")) {
    init {
        manifest.description = "Provide functionality to log in with a token directly from the login screen"
    }

    internal class Page : AppFragment(getResId("widget_auth_login", "layout")) {
        override fun onViewBound(view: View) {
            super.onViewBound(view)

            val v = view.findViewById<LinearLayout>(getResId("auth_login_container", "id"))
            v.removeViewAt(1) // remove email input
            v.removeViewAt(2) // remove forgot password
            v.removeViewAt(2) // remove use a password manager

            val input = v.getChildAt(1) as TextInputLayout?
            if (input != null) {
                input.hint = "Token"
                ViewExtensions.setOnImeActionDone(input, false) { e ->
                    if (e.text != "") login(e.text)
                }
            }

            val button = v.getChildAt(2) as MaterialButton?
            button?.setOnClickListener { e ->
                if (input?.editText == null) return@setOnClickListener
                val token = input.editText!!.text
                if (token.length == 70) login(token)
            }
        }

        fun login(token: CharSequence) {
            StoreAuthentication.`access$dispatchLogin`(
                StoreStream.getAuthentication(),
                ModelLoginResult(token.toString().startsWith("mfa."), null, token.toString(), null, ArrayList<RequiredAction?>())
            )
        }
    }

    override fun start(appContext: Context) {
        Patcher.addPatch(
            WidgetAuthLanding::class.java.getDeclaredMethod("onViewBound", View::class.java),
            Hook { param ->
                val view = param.args[0] as RelativeLayout
                val context = (param!!.thisObject as WidgetAuthLanding).requireContext()
                val btn = Button(context).apply {
                    val padding = dpToPx(18)
                    setPadding(0, padding, 0, padding)
                    text = "Login using token"
                    textSize = 16.0f
                    setBackgroundColor(
                        context.resources.getColor(
                            if (StoreStream.getUserSettingsSystem().theme == "light") {
                                R.c.uikit_btn_bg_color_selector_secondary_light
                            } else {
                                R.c.uikit_btn_bg_color_selector_secondary_dark
                            },
                            null
                        )
                    )

                    setOnClickListener { e -> openPage(e!!.context, Page::class.java) }
                }
                val v = view.getChildAt(1) as LinearLayout
                v.addView(btn)
            }
        )

        Patcher.addPatch(AppActivity::class.java, "g", arrayOf(MutableList::class.java), Hook { param ->
            if (!(param.result as Boolean) && (param.thisObject as AppActivity).d() == Page::class.java) param.result = true
        })
    }

    override fun stop(context: Context) {}
}
