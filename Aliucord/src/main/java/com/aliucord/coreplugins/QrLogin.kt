/*
 * This file is part of Aliucord, an Android Discord client mod.
 * Copyright (c) 2026 Juby210 & Vendicated
 * Licensed under the Open Software License version 3.0
 */

package com.aliucord.coreplugins

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.graphics.drawable.ColorDrawable
import android.os.SystemClock
import android.text.InputType
import androidx.fragment.app.Fragment
import com.aliucord.*
import com.aliucord.fragments.InputDialog
import com.aliucord.utils.IOUtils
import com.discord.app.AppFragment
import com.discord.utilities.color.ColorCompat
import com.discord.widgets.auth.WidgetRemoteAuthViewModel
import com.google.android.material.button.MaterialButton
import com.lytefast.flexinput.R
import org.json.JSONObject
import java.io.IOException
import kotlin.math.ceil

internal object QrLogin {
    private const val TIMEOUT = 15_000
    private const val LOGIN_BUTTON = "remote_auth_login_button"
    private const val MFA_TAG = "qr_mfa"
    private const val EXPIRED_MESSAGE = "Handshake expired\nDid you switch apps?\n\nRestart the process, make sure to authorize before switching from Aliucord."

    private const val FINISH_SUCCESS = 0
    private const val FINISH_MFA = 1
    private const val FINISH_FAIL = 2

    private val logger = Logger("QrLogin")
    private const val HANDSHAKE_TTL = 120_000L

    @Volatile private var handshakeToken: String? = null
    @Volatile private var mfaTicket: String? = null
    @Volatile private var mfaType: String? = null
    @Volatile private var mfaNumeric = false
    @Volatile private var pendingMfa: JSONObject? = null
    @Volatile private var pendingError: String? = null
    @Volatile private var pendingHandshake: String? = null
    @Volatile private var handshakeTime = 0L
    @Volatile private var recovering = false

    fun onRemoteAuthState(host: AppFragment, viewState: Any?) {
        when (viewState) {
            is WidgetRemoteAuthViewModel.ViewState.Loaded -> {
                if (viewState.loginAllowed) bindLogin(host, viewState.handshakeToken)
            }
            is WidgetRemoteAuthViewModel.ViewState.Failed -> {
                // Native reinit on resume 404 and ignore mid-MFA, otherwise the
                // first handshake may still be valid, so offer to finish with it before throwing
                /// the cant find pc error
                if (!mfaTicket.isNullOrEmpty() || recovering) return

                val token = pendingHandshake

                if (!token.isNullOrEmpty() && SystemClock.elapsedRealtime() - handshakeTime <= HANDSHAKE_TTL) {
                    recovering = true
                    promptRecover(host, token)
                } else {
                    logger.errorToast(EXPIRED_MESSAGE)
                }
            }
        }
    }

    private fun bindLogin(host: AppFragment, token: String) {
        val button = findButton(host) ?: return

        pendingHandshake = token
        handshakeTime = SystemClock.elapsedRealtime()

        button.apply {
            isEnabled = true
            setOnClickListener {
                isEnabled = false
                Utils.threadPool.submit { confirmLogin(host, token) }
            }
        }
    }

    private fun confirmLogin(host: AppFragment, token: String) {
        when (remoteFinish(token, null)) {
            FINISH_SUCCESS -> loginDone(host)
            FINISH_MFA -> startMfa(host, token, pendingMfa)
            else -> {
                logger.errorToast(pendingError ?: "Login failed")
                reEnable(host)
            }
        }
    }

    private fun promptRecover(host: AppFragment, token: String) {
        Utils.mainThread.post {
            if (!host.isAdded) return@post

            AlertDialog.Builder(host.requireActivity())
                .setTitle("Resume login?")
                .setMessage("The login screen reloaded. Approve the pending login?")
                .setCancelable(false)
                .setPositiveButton("Approve") { _, _ ->
                    recovering = false
                    Utils.threadPool.submit { confirmLogin(host, token) }
                }
                .setNegativeButton("Cancel") { _, _ ->
                    recovering = false
                    cancelFlow()
                }.show()
        }
    }

    private fun startMfa(host: AppFragment, token: String, mfa: JSONObject?) {
        val ticket = mfa?.optString("ticket")

        if (ticket.isNullOrEmpty()) {
            logger.errorToast("MFA required but no ticket received")
            reEnable(host)
            return
        }

        var hasTotp = false
        var hasBackup = false
        var hasPassword = false
        val available = StringBuilder()
        val methods = mfa?.optJSONArray("methods")

        if (methods != null) {
            for (i in 0 until methods.length()) {
                val method = methods.optJSONObject(i)
                val type = method?.optString("type") ?: ""

                if (type.isEmpty()) continue
                if (available.isNotEmpty()) available.append(", ")

                available.append(type)

                when (type) {
                    "totp" -> hasTotp = true
                    "backup" -> hasBackup = true
                    "password" -> hasPassword = true
                }
            }
        }

        if (!hasTotp && !hasBackup && !hasPassword) {
            logger.errorToast("Unsupported 2FA method: $available")
            reEnable(host)
            return
        }

        handshakeToken = token
        mfaTicket = ticket
        mfaType = if (hasTotp) "totp" else if (hasBackup) "backup" else "password"
        mfaNumeric = hasTotp

        Utils.mainThread.post {
            Utils.openPage(host.requireContext(), TokenLogin.MfaHost::class.java)
            host.requireActivity().finish()
        }
    }

    fun onMfaHostBound(host: AppFragment) {
        if (mfaTicket.isNullOrEmpty()) {
            host.requireActivity().finish()
            return
        }

        val activity = host.requireActivity()
        activity.window.setBackgroundDrawable(
            ColorDrawable(ColorCompat.getThemedColor(host.requireContext(), R.b.colorBackgroundPrimary))
        )

        val fm = activity.supportFragmentManager
        if (fm.findFragmentByTag(MFA_TAG) == null) {
            TokenLogin.MfaDialog().show(fm, MFA_TAG)
        }
    }

    @SuppressLint("SetTextI18n")
    fun bindMfaDialog(dialog: InputDialog) {
        val password = mfaType == "password"

        dialog.apply {
            isCancelable = false
            header.text = if (password) "Password Required" else "Two-Factor Auth"
            body.text = when {
                password -> "Enter your password"
                mfaNumeric -> "Enter your 6-digit authentication code"
                else -> "Enter a backup code"
            }
            inputLayout.hint = when {
                password -> "Password"
                mfaNumeric -> "Authentication code"
                else -> "Backup code"
            }
            inputLayout.editText?.let {
                if (mfaNumeric) it.inputType = InputType.TYPE_CLASS_NUMBER
                else if (password) it.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
            }
            okButton.setOnClickListener {
                // do NOT trim passwords
                val code = if (password) dialog.input else dialog.input.trim()
                if (code.isNullOrEmpty()) return@setOnClickListener

                okButton.isEnabled = false
                Utils.threadPool.submit { submitMfa(dialog, code) }
            }
            cancelButton.setOnClickListener {
                cancelFlow()
                dialog.requireActivity().finish()
            }
        }
    }

    private fun submitMfa(dialog: InputDialog, code: String) {
        if (mfaTicket.isNullOrEmpty()) {
            mfaError(dialog, "Session expired")
            return
        }

        val mfaToken = mfaVerify(code)
        if (mfaToken.isNullOrEmpty()) {
            mfaError(dialog, pendingError)
            return
        }

        if (remoteFinish(handshakeToken, mfaToken) == FINISH_SUCCESS) {
            loginDone(dialog)
        } else {
            mfaError(dialog, pendingError)
        }
    }

    private fun mfaVerify(code: String): String? {
        return try {
            val body = JSONObject()
                .put("ticket", mfaTicket)
                .put("mfa_type", mfaType)
                .put("data", code)
                .toString()
            val response = post("/mfa/finish", body, null)

            if (response.isNullOrEmpty()) {
                pendingError = "MFA verification failed"
                return null
            }

            val json = JSONObject(response)

            if (json.has("token")) {
                return json.optString("token")
            }

            if (json.optInt("code", -1) == 60008) {
                pendingError = "Invalid code, try again"
                return null
            }

            if (json.has("retry_after")) {
                pendingError = "Rate limited, wait ${ceil(json.optDouble("retry_after")).toInt()}s"
                return null
            }

            pendingError = json.optString("message", "MFA verification failed")
            null
        } catch (e: Exception) {
            logger.error("mfaVerify failed", e)
            pendingError = "MFA verification failed"
            null
        }
    }

    private fun remoteFinish(token: String?, mfaToken: String?): Int {
        return try {
            val response = post("/users/@me/remote-auth/finish", parseHandshake(token), mfaToken)

            if (response.isNullOrEmpty()) {
                pendingError = "Login failed"
                return FINISH_FAIL
            }

            if (response.trim().isEmpty()) return FINISH_SUCCESS

            val json = JSONObject(response)
            val code = json.optInt("code", -1)

            if (code == 60003) {
                pendingMfa = json.optJSONObject("mfa")
                return FINISH_MFA
            }

            if (code == -1) {
                return FINISH_SUCCESS
            }

            pendingError = "Login failed: ${json.optString("message", "code $code")}"
            FINISH_FAIL
        } catch (e: Exception) {
            logger.error("remoteFinish failed", e)
            pendingError = "Login failed"
            FINISH_FAIL
        }
    }

    private fun cancelFlow() {
        val token = handshakeToken
        clearState()
        if (!token.isNullOrEmpty()) {
            Utils.threadPool.submit {
                post("/users/@me/remote-auth/cancel", parseHandshake(token), null)
            }
        }
    }

    private fun loginDone(fragment: Fragment) {
        logger.infoToast("Logged in!")
        clearState()
        Utils.mainThread.post { fragment.requireActivity().finish() }
    }

    private fun mfaError(dialog: InputDialog, message: String?) {
        logger.errorToast(message ?: "Unknown error")
        Utils.mainThread.post {
            try { dialog.okButton.isEnabled = true } catch (_: Throwable) { /* we ignore */ }
        }
    }

    private fun reEnable(host: AppFragment) {
        Utils.mainThread.post {
            findButton(host)?.isEnabled = true
        }
    }

    private fun findButton(host: AppFragment): MaterialButton? {
        val root = host.view ?: return null

        return root.findViewById(Utils.getResId(LOGIN_BUTTON, "id"))
    }

    private fun clearState() {
        handshakeToken = null
        mfaTicket = null
        mfaType = null
        pendingMfa = null
        pendingHandshake = null
        recovering = false
    }

    private fun parseHandshake(value: String?): String {
        return try {
            JSONObject().put("handshake_token", value).toString()
        } catch (e: Exception) {
            logger.error("parseHandshake failed", e)
            "{}"
        }
    }

    private fun post(route: String, body: String, mfaToken: String?): String? {
        var req: Http.Request? = null
        return try {
            req = Http.Request.newDiscordRequest(route, "POST")
            req.setHeader("Content-Type", "application/json")
            req.setRequestTimeout(TIMEOUT)

            if (!mfaToken.isNullOrEmpty()) {
                req.setHeader("X-Discord-MFA-Authorization", mfaToken)
                req.setHeader("Cookie", "__Secure-recent_mfa=$mfaToken")
            }

            req.executeWithBody(body)

            val stream = try {
                req.conn.inputStream
            } catch (_: IOException) {
                req.conn.errorStream
            }

            if (stream == null) "" else IOUtils.readAsText(stream)
        } catch (e: Exception) {
            logger.error("POST $route failed", e)
            null
        } finally {
            req?.close()
        }
    }
}
