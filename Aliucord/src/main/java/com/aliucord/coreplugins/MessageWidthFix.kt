package com.aliucord.coreplugins

import android.content.Context
import android.view.ViewGroup.LayoutParams
import androidx.constraintlayout.widget.ConstraintLayout
import com.aliucord.Utils
import com.aliucord.entities.CorePlugin
import com.aliucord.patcher.*
import com.discord.widgets.chat.list.adapter.*
import com.discord.widgets.chat.list.entries.ChatListEntry
import com.discord.widgets.chat.list.entries.StickerEntry
import com.google.android.material.card.MaterialCardView

internal class MessageWidthFix : CorePlugin(Manifest("MessageWidthFix")) {
    override val isRequired = true

    override fun start(context: Context) {
        val containerCardViewId = Utils.getResId("chat_list_item_embed_container_card", "id")

        patcher.after<WidgetChatListItem>(
            "onConfigure",
            Int::class.java,
            ChatListEntry::class.java
        ) { (_, _: Any, chatListEntry: ChatListEntry) ->
            with(itemView) {
                when (this@after) {
                    is WidgetChatListAdapterItemEmbed -> {
                        layoutParams.width = LayoutParams.MATCH_PARENT

                        with(findViewById<MaterialCardView>(containerCardViewId).layoutParams as ConstraintLayout.LayoutParams) {
                            width = LayoutParams.WRAP_CONTENT
                            horizontalBias = 0.0f
                            constrainedWidth = true
                        }
                    }

                    is WidgetChatListAdapterItemSticker -> {
                        layoutParams.width = LayoutParams.MATCH_PARENT

                        val stickerEntry = chatListEntry as StickerEntry

                        setOnClickListener {
                            adapter.eventHandler.onMessageClicked(
                                chatListEntry.message,
                                stickerEntry.message.hasThread()
                            )
                        }

                        setOnLongClickListener {
                            adapter.eventHandler.onMessageLongClicked(
                                stickerEntry.message,
                                "",
                                stickerEntry.message.hasThread()
                            )
                            true
                        }
                    }
                }
            }
        }
    }

    override fun stop(context: Context) = patcher.unpatchAll()
}
