package com.aliucord.coreplugins

import android.annotation.SuppressLint
import android.content.Context
import android.view.View
import android.widget.*
import com.aliucord.Constants.PLUGIN_REQUESTS_CHANNEL_ID
import com.aliucord.Utils
import com.aliucord.entities.Plugin
import com.aliucord.fragments.ConfirmDialog
import com.aliucord.fragments.InputDialog
import com.aliucord.patcher.Hook
import com.aliucord.patcher.Patcher
import com.discord.databinding.WidgetChatInputBinding
import com.discord.widgets.chat.input.ChatInputViewModel
import com.discord.widgets.chat.input.WidgetChatInput
import com.lytefast.flexinput.R

@SuppressLint("SetTextI18n")
internal class SupportWarn : Plugin() {
    private val bindingMethod = WidgetChatInput::class.java.getDeclaredMethod("getBinding").apply { isAccessible = true }

    private val channelList = listOf(
        811255667469582420L, // #offtopic
        811261478875299840L, // #plugin-development
        868419532992172073L, // #theme-development
        865188789542060063L, // #related-development
        811262084968742932L, // #core-development
        PLUGIN_REQUESTS_CHANNEL_ID,
    )

    private val chatWrapId = Utils.getResId("chat_input_wrap", "id")
    private val gateButtonTextId = Utils.getResId("chat_input_member_verification_guard_text", "id")
    private val gateButtonImageId = Utils.getResId("chat_input_member_verification_guard_icon", "id")
    private val gateButtonArrowId = Utils.getResId("chat_input_member_verification_guard_action", "id")
    private val gateButtonLayoutId = Utils.getResId("guard_member_verification", "id")

    init {
        Manifest().run {
            name = "SupportWarn"
            initialize(this)
        }
    }

    override fun load(context: Context) {
        Patcher.addPatch(WidgetChatInput::class.java.getDeclaredMethod("configureChatGuard", ChatInputViewModel.ViewState.Loaded::class.java), Hook {
            val loaded = it.args[0] as ChatInputViewModel.ViewState.Loaded

            if (loaded.channelId !in channelList || loaded.shouldShowVerificationGate) return@Hook
            if (loaded.channelId == PLUGIN_REQUESTS_CHANNEL_ID) {
                if (settings.getBool("acceptedPrdNotRequests", false)) return@Hook
            } else if (settings.getBool("acceptedDevNotSupport", false)) return@Hook

            val (text, desc, key) = when (loaded.channelId) {
                PLUGIN_REQUESTS_CHANNEL_ID -> Triple(
                    "PLEASE READ: This is not a request channel, do not request plugins!",
                    "This is NOT A REQUESTING CHANNEL. For information on how to request a plugin, check the pins in this channel. If you have read this, type \"I understand\" into the box.",
                    "acceptedPrdNotRequests"
                )
                else -> Triple(
                    "PLEASE READ: This is not a support channel, do not ask for help!",
                    "This is NOT A SUPPORT CHANNEL. Do NOT ask for help about using or installing a plugin or theme here or you will be muted. If you have read this, type \"I understand\" into the box.",
                    "acceptedDevNotSupport"
                )
            }

            val binding = bindingMethod(it.thisObject) as WidgetChatInputBinding

            val gateButtonText = binding.root.findViewById<TextView>(gateButtonTextId)
            val chatWrap = binding.root.findViewById<LinearLayout>(chatWrapId)
            val gateButtonImage = binding.root.findViewById<ImageView>(gateButtonImageId)
            val gateButtonArrow = binding.root.findViewById<ImageView>(gateButtonArrowId)
            val gateButtonLayout = binding.root.findViewById<RelativeLayout>(gateButtonLayoutId)

            gateButtonLayout.visibility = View.VISIBLE
            chatWrap.visibility = View.GONE

            gateButtonImage.setImageResource(R.e.ic_warning_circle_24dp)

            gateButtonText.text = text
            gateButtonArrow.setOnClickListener { _ ->
                val dialog = InputDialog()
                    .setTitle("Warning")
                    .setDescription(desc)

                dialog.setOnOkListener {
                    if (dialog.input != "I understand") return@setOnOkListener
                    settings.setBool(key, true)

                    gateButtonLayout.visibility = View.GONE
                    chatWrap.visibility = View.VISIBLE

                    dialog.dismiss()
                }

                dialog.show((it.thisObject as WidgetChatInput).parentFragmentManager, "Warning")
            }
        })
    }

    override fun start(context: Context) {}
    override fun stop(context: Context) {}
}
