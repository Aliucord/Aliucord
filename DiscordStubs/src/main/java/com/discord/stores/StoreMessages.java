package com.discord.stores;

import com.discord.api.message.allowedmentions.MessageAllowedMentions;
import com.discord.models.message.Message;

@SuppressWarnings("unused")
public final class StoreMessages {
    public static void access$handleLocalMessageCreate(StoreMessages instance, Message message) {}

    public final void deleteMessage(Message message) {}
    public final Message getMessage(long channelId, long id) { return null; }
    public final void handleMessageUpdate(com.discord.api.message.Message message) {}
    public final void editMessage(long messageId, long channelId, String content, MessageAllowedMentions allowedMentions) {}
}
