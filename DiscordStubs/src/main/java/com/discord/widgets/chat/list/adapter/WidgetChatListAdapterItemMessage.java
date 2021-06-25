package com.discord.widgets.chat.list.adapter;

import android.content.Context;

import com.discord.api.message.Message;
import com.discord.stores.StoreMessageState;
import com.discord.utilities.textprocessing.MessagePreprocessor;
import com.discord.utilities.textprocessing.MessageRenderContext;
import com.discord.utilities.textprocessing.node.SpoilerNode;
import com.discord.widgets.chat.list.entries.MessageEntry;

import kotlin.Unit;
import kotlin.jvm.functions.Function1;

@SuppressWarnings("unused")
public final class WidgetChatListAdapterItemMessage extends WidgetChatListItem {
    public WidgetChatListAdapterItemMessage(int id, final WidgetChatListAdapter widgetChatListAdapter) {
        super(id, widgetChatListAdapter);
    }

    private boolean shouldLinkify(String content) { return false; }

    private MessagePreprocessor getMessagePreprocessor(long myUserId, Message message, StoreMessageState.State state) {
        return new MessagePreprocessor();
    }

    private MessageRenderContext getMessageRenderContext(Context context, MessageEntry entry, Function1<SpoilerNode<?>, Unit> spoilerClickHandler) {
        return null;
    }

    private Function1<SpoilerNode<?>, Unit> getSpoilerClickHandler(Message message) {
        return s -> null;
    }
}
