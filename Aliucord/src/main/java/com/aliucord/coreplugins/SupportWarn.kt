package com.aliucord.coreplugins

import android.annotation.SuppressLint
import android.content.Context
import android.view.View
import android.widget.*
import com.aliucord.Constants.PLUGIN_REQUESTS_CHANNEL_ID
import com.aliucord.Utils
import com.aliucord.entities.Plugin
import com.aliucord.fragments.ConfirmDialog
import com.aliucord.patcher.Hook
import com.aliucord.patcher.Patcher
import com.discord.databinding.WidgetChatInputBinding
import com.discord.widgets.chat.input.ChatInputViewModel
import com.discord.widgets.chat.input.WidgetChatInput
import com.lytefast.flexinput.R

@SuppressLint("SetTextI18n")
internal class SupportWarn : Plugin() {
    private val bindingMethod = WidgetChatInput::class.java.getDeclaredMethod("getBinding").apply { isAccessible = true }

    private val channelList = listOf(811261478875299840L, 868419532992172073L, 865188789542060063L, 811262084968742932L, PLUGIN_REQUESTS_CHANNEL_ID)

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

            if (loaded.channelId in channelList && !loaded.shouldShowVerificationGate && !settings.getBool("devNotSupport", false)) {
                val binding = bindingMethod(it.thisObject) as WidgetChatInputBinding

                val gateButtonText = binding.root.findViewById<TextView>(gateButtonTextId)
                val chatWrap = binding.root.findViewById<LinearLayout>(chatWrapId)
                val gateButtonImage = binding.root.findViewById<ImageView>(gateButtonImageId)
                val gateButtonArrow = binding.root.findViewById<ImageView>(gateButtonArrowId)
                val gateButtonLayout = binding.root.findViewById<RelativeLayout>(gateButtonLayoutId)

                gateButtonLayout.visibility = View.VISIBLE
                chatWrap.visibility = View.GONE

                gateButtonImage.setImageResource(R.e.ic_warning_circle_24dp)

                val text = if (loaded.channelId == PLUGIN_REQUESTS_CHANNEL_ID)
                    "PLEASE READ: This channel is NOT for requesting plugins, do not send requests here."
                else "PLEASE READ: This channel is not a support channel, do not ask for help."

                val desc = if (loaded.channelId == PLUGIN_REQUESTS_CHANNEL_ID)
                    "#plugin-request-discussion is not for requesting plugins. For information on how to request a plugin, check the pins in this channel."
                else "The development channels are not a support channel. Please do not ask for help about using or installing a plugin or theme here or you will be muted."

                gateButtonText.text = text
                gateButtonArrow.setOnClickListener { _ ->
                    val dialog = ConfirmDialog()
                        .setTitle("Warning")
                        .setDescription(desc)

                    dialog.setOnOkListener {
                        settings.setBool("devNotSupport", true)

                        gateButtonLayout.visibility = View.GONE
                        chatWrap.visibility = View.VISIBLE

                        dialog.dismiss()
                    }

                    dialog.show((it.thisObject as WidgetChatInput).parentFragmentManager, "Warning")
                }
            }
        })
    }

    override fun start(context: Context) {}
    override fun stop(context: Context) {}
}
