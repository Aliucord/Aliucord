package com.aliucord.coreplugins.polls.chatview

import com.aliucord.utils.RxUtils.subscribe
import com.discord.stores.StoreStream

internal object PollChatViewModelFactory {
    private val models = HashMap<Long, PollChatViewModel>()

    init {
        StoreStream.getChannelsSelected().observeSelectedChannel().subscribe {
            models.values.forEach { it.unsubscribe() }
            models.clear()
        }
    }

    fun create(
        entry: PollChatEntry,
        onModelUpdate: (PollChatView.Model, isUpdate: Boolean) -> Unit
    ): PollChatViewModel {
        return models.getOrPut(entry.message.id) {
            PollChatViewModel(entry.message, entry.poll, onModelUpdate)
        }.also { it.onModelUpdate = onModelUpdate }
    }
}
