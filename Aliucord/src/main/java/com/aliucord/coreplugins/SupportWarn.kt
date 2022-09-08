package com.aliucord.coreplugins

import android.annotation.SuppressLint
import android.content.Context
import android.view.View
import android.view.ViewGroup
import android.widget.*
import com.aliucord.Constants.PLUGIN_DEVELOPMENT_CHANNEL_ID
import com.aliucord.Constants.PLUGIN_REQUESTS_CHANNEL_ID
import com.aliucord.Utils
import com.aliucord.api.SettingsAPI
import com.aliucord.entities.Plugin
import com.aliucord.fragments.ConfirmDialog
import com.aliucord.fragments.InputDialog
import com.aliucord.patcher.Hook
import com.aliucord.patcher.Patcher
import com.aliucord.settings.delegate
import com.discord.widgets.chat.input.ChatInputViewModel
import com.discord.widgets.chat.input.WidgetChatInput
import com.lytefast.flexinput.R

@SuppressLint("SetTextI18n")
internal class SupportWarn : Plugin(Manifest("SupportWarn")) {
    private val SettingsAPI.acceptedPrdNotRequests: Boolean by settings.delegate(false)
    private val SettingsAPI.acceptedDevNotSupport: Boolean by settings.delegate(false)

    override fun load(context: Context) {
        if (settings.acceptedPrdNotRequests && settings.acceptedDevNotSupport) return

        val channelList = listOf(
            811255667469582420L, // #offtopic
            811261478875299840L, // #plugin-development
            868419532992172073L, // #theme-development
            865188789542060063L, // #related-development
            811262084968742932L, // #core-development
            PLUGIN_REQUESTS_CHANNEL_ID,
        )

        val chatWrapId = Utils.getResId("chat_input_wrap", "id")
        val gateButtonTextId = Utils.getResId("chat_input_member_verification_guard_text", "id")
        val gateButtonImageId = Utils.getResId("chat_input_member_verification_guard_icon", "id")
        val gateButtonArrowId = Utils.getResId("chat_input_member_verification_guard_action", "id")
        val gateButtonLayoutId = Utils.getResId("guard_member_verification", "id")

        Patcher.addPatch(WidgetChatInput::class.java.getDeclaredMethod("configureChatGuard", ChatInputViewModel.ViewState.Loaded::class.java), Hook {
            val loaded = it.args[0] as ChatInputViewModel.ViewState.Loaded

            if (loaded.channelId !in channelList || loaded.shouldShowVerificationGate) return@Hook

            val (text, desc, key) = if (loaded.channelId == PLUGIN_REQUESTS_CHANNEL_ID) {
                if (settings.acceptedPrdNotRequests) return@Hook
                Triple(
                    "PLEASE READ: This is not a request channel, do not request plugins!",
                    "This is NOT A REQUESTING CHANNEL. For information on how to request a plugin, check the pins in this channel. If you have read this, type \"I understand\" into the box.",
                    "acceptedPrdNotRequests"
                )
            } else {
                if (settings.acceptedDevNotSupport) return@Hook
                Triple(
                    "PLEASE READ: This is not a support channel, do not ask for help!",
                    "This is NOT A SUPPORT CHANNEL. Do NOT ask for help about using or installing a plugin or theme here or you will be muted. If you have read this, type \"I understand\" into the box.",
                    "acceptedDevNotSupport"
                )
            }

            val thisObject = it.thisObject as WidgetChatInput
            val root = WidgetChatInput.`access$getBinding$p`(thisObject).root
            val gateButtonLayout = root.findViewById<ViewGroup>(gateButtonLayoutId)
            val chatWrap = root.findViewById<LinearLayout>(chatWrapId)

            gateButtonLayout.visibility = View.VISIBLE
            chatWrap.visibility = View.GONE
          
            root.findViewById<TextView>(gateButtonTextId).text = text
            root.findViewById<ImageView>(gateButtonImageId).setImageResource(R.e.ic_warning_circle_24dp)
            root.findViewById<ImageView>(gateButtonArrowId).setOnClickListener {
                val dialog = InputDialog()
                    .setTitle("Warning")
                    .setDescription(desc)

                dialog.setOnOkListener {
                    if (!dialog.input.contains("I understand", ignoreCase = true)) return@setOnOkListener
                    settings.setBool(key, true)

                    gateButtonLayout.visibility = View.GONE
                    chatWrap.visibility = View.VISIBLE

                    dialog.dismiss()
                }

                dialog.show(thisObject.parentFragmentManager, "Warning")
            }
        })
    }

    override fun start(context: Context) {}
    override fun stop(context: Context) {}
}
