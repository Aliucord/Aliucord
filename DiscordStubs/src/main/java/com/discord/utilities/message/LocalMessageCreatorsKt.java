package com.discord.utilities.message;

import com.discord.api.application.Application;
import com.discord.api.message.LocalAttachment;
import com.discord.api.message.Message;
import com.discord.api.message.MessageReference;
import com.discord.api.message.activity.MessageActivity;
import com.discord.api.message.allowedmentions.MessageAllowedMentions;
import com.discord.api.sticker.Sticker;
import com.discord.api.user.User;
import com.discord.utilities.time.Clock;

import java.util.List;

@SuppressWarnings("unused")
public final class LocalMessageCreatorsKt {
    public static Message createLocalApplicationCommandMessage(
        long id,
        String commandName,
        long channelId,
        User interactionUser,
        User author,
        boolean failed,
        boolean loading,
        Long interactionId,
        Clock clock
    ) { return new Message(); }

    public static Message createLocalApplicationCommandMessage(
        Message other,
        Long interactionId,
        boolean failed,
        boolean loading,
        Clock clock
    ) { return other; }

    public static Message createLocalMessage(
        String content,
        long channelId,
        User author,
        List<User> mentions,
        boolean failed,
        boolean hasLocalUploads,
        Application application,
        MessageActivity activity,
        Clock clock,
        List<LocalAttachment> localAttachments,
        Long lastManualAttemptTimestamp,
        Long initialAttemptTimestamp,
        Integer retries,
        List<Sticker> stickers,
        MessageReference messageReference,
        MessageAllowedMentions allowedMentions
    ) { return new Message(); }
}
