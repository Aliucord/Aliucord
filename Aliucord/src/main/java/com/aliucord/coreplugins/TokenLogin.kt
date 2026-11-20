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
import com.aliucord.Http
import com.aliucord.Utils
import com.aliucord.entities.CorePlugin
import com.aliucord.fragments.InputDialog
import com.aliucord.patcher.after
import com.aliucord.utils.DimenUtils
import com.aliucord.views.Button
import com.discord.app.AppActivity
import com.discord.app.AppFragment
import com.discord.models.domain.auth.ModelLoginResult
import com.discord.stores.StoreAuthentication
import com.discord.stores.StoreStream
import com.discord.utilities.color.ColorCompat
import com.discord.utilities.view.extensions.ViewExtensions
import com.discord.views.LoadingButton
import com.discord.widgets.auth.WidgetAuthLanding
import com.discord.widgets.auth.WidgetRemoteAuth
import com.discord.widgets.auth.WidgetRemoteAuthViewModel
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputLayout
import com.lytefast.flexinput.R
import java.io.IOException

internal class TokenLogin : CorePlugin(Manifest("TokenLogin")) {
    companion object {
        private const val TIMEOUT = 10_000
    }

    init {
        manifest.description = "Provide functionality to log in with a token directly from the login screen"
    }

    // Token login page
    class Page : AppFragment(Utils.getResId("widget_auth_login", "layout")) {
        private var loginButton: LoadingButton? = null

        override fun onViewBound(view: View) {
            super.onViewBound(view)

            // prevent crash if layout change
            val v = view.findViewById<LinearLayout>(Utils.getResId("auth_login_container", "id")) ?: return

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
            if (button != null) {
                val newButton = LoadingButton(view.context, null).apply {
                    setIsLoading(false)
                    setText(button.text)
                }

                loginButton = newButton

                val index = v.indexOfChild(button)
                v.removeViewAt(index)
                v.addView(newButton, index, button.layoutParams)

                newButton.setOnClickListener {
                    input?.editText?.text?.let { login(it) }
                }
            }
        }

        fun login(token: CharSequence) {
            val trimmedToken = token.toString().trim()
            if (trimmedToken.isEmpty()) {
                Utils.showToast("Token cannot be empty")
                return
            }

            setLoading(true)
            Utils.threadPool.execute {
                try {
                    Http.Request.newDiscordRNRequest("/users/@me")
                        .setHeader("Authorization", trimmedToken)
                        .setRequestTimeout(TIMEOUT)
                        .use { req ->
                            req.execute().assertOk()
                            StoreAuthentication.`access$dispatchLogin`(
                                StoreStream.getAuthentication(),
                                ModelLoginResult(trimmedToken.startsWith("mfa."), null, trimmedToken, null, ArrayList())
                            )
                        }
                } catch (e: Http.HttpException) {
                    Utils.showToast("Invalid token: ${e.statusCode}: ${e.statusMessage}")
                } catch (e: IOException) {
                    Utils.showToast("Failed to verify token: ${e.message}")
                } finally {
                    setLoading(false)
                }
            }
        }

        private fun setLoading(state: Boolean) {
            val button = loginButton ?: return

            if (state) {
                button.setIsLoading(true)
                button.isEnabled = false
            } else {
                Utils.mainThread.post {
                    button.setIsLoading(false)
                    button.isEnabled = true
                }
            }
        }
    }

    // Backdrop screen that hosts the MFA dialog (so the bg isn't white)
    class MfaHost : AppFragment(Utils.getResId("widget_kick_user", "layout")) {
        override fun onViewBound(view: View) {
            super.onViewBound(view)
            view.visibility = View.INVISIBLE  // just a dim backdrop; the dialog is the UI
            QrLogin.onMfaHostBound(this)
        }
    }

    // MFA code entry for QR login page
    class MfaDialog : InputDialog() {
        override fun onViewBound(view: View) {
            super.onViewBound(view)
            QrLogin.bindMfaDialog(this)
        }
    }

    override fun start(context: Context) {
        // Add "Login using token" button to the auth landing screen
        patcher.after<WidgetAuthLanding>("onViewBound", View::class.java) { param ->
            val ctx = requireContext()
            val view = param.args[0] as RelativeLayout
            val v = view.getChildAt(1) as LinearLayout
            val padding = DimenUtils.dpToPx(18)

            val colorRes = if (StoreStream.getUserSettingsSystem().theme == "light")
                { R.c.uikit_btn_bg_color_selector_secondary_light } else
                { R.c.uikit_btn_bg_color_selector_secondary_dark }

            v.addView(Button(ctx).apply {
                setPadding(0, padding, 0, padding)
                text = "Login using token"
                textSize = 16f
                setBackgroundColor(ColorCompat.getColor(this, colorRes))
                setOnClickListener { Utils.openPage(it.context, Page::class.java) }
            })
        }

        // The native QR scanner already launches WidgetRemoteAuth for ra codes. Route its "Login"
        // button through our finish.
        // Hook the "Scan QR Code" button in user settings to the existing WidgetRemoteAuth
        // Also hook to the login button so we can catch the error 60003, which warn
        // the user when the handshake has expired.
        patcher.after<WidgetRemoteAuth>("configureUI", WidgetRemoteAuthViewModel.ViewState::class.java) { param ->
            QrLogin.onRemoteAuthState(this, param.args[0])
        }

        // horrible
        patcher.after<AppActivity>("g", List::class.java) { param ->
            if (param.result != true && this.d() == Page::class.java) param.result = true
        }
    }

    override fun stop(context: Context) = patcher.unpatchAll()
}
