package com.discord.stores;

import com.discord.models.domain.ModelAllowedMentions;
import com.discord.models.domain.ModelMessage;

@SuppressWarnings("unused")
public final class StoreMessages {
    public static void access$handleLocalMessageCreate(StoreMessages instance, ModelMessage message) {}

    public final void deleteMessage(ModelMessage message) {}
    public final ModelMessage getMessage(long channelId, long id) { return null; }
    public final void handleMessageUpdate(ModelMessage message) {}
    public final void editMessage(long j, long j2, String str, ModelAllowedMentions modelAllowedMentions) { }
}
