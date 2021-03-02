package com.discord.models.domain;

import com.discord.api.guildmember.GuildMember;
import com.discord.api.user.User;
import com.discord.models.messages.LocalAttachment;
import com.discord.models.sticker.dto.ModelSticker;
import com.discord.utilities.time.Clock;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

@SuppressWarnings("unused")
public class ModelMessage {
    public static class Activity {}
    public static class Call {}
    public static class MessageReference {}

    public static final int TYPE_DEFAULT = 0;
    public static final int TYPE_LOCAL = -1;
    public static final int TYPE_LOCAL_SEND_FAILED = -2;

    public static ModelMessage createLocalMessage(
            String content,
            long channelId,
            User author,
            List<User> mentions,
            boolean failed,
            boolean hasLocalUploads,
            ModelApplication application,
            Activity activity,
            Clock clock,
            List<LocalAttachment> localAttachments,
            Long lastManualAttemptTimestamp,
            Long initialAttemptTimestamp,
            Integer retries,
            List<ModelSticker> stickers,
            MessageReference messageReference,
            ModelAllowedMentions allowedMentions
    ) { return null; }

    public ModelMessage(
            long id,
            String nonce,
            long channelId,
            int type,
            String content,
            User author,
            List<User> mentions,
            String timestamp,
            String editedTimestamp,
            List<ModelMessageAttachment> attachments,
            List<ModelMessageEmbed> embeds,
            boolean tts,
            Call call,
            boolean mentionEveryone,
            LinkedHashMap<String, ModelMessageReaction> reactions,
            Boolean pinned,
            Long webhookId,
            ModelApplication application,
            Activity activity,
            boolean hit,
            List<Long> mentionRoles,
            boolean hasLocalUploads,
            Long flags,
            MessageReference messageReference,
            ModelAllowedMentions allowedMentions,
            List<LocalAttachment> localAttachments,
            Long lastManualAttemptTimestamp,
            Long initialAttemptTimestamp,
            Integer retries,
            List<ModelSticker> stickers
    ) {}

    public User getAuthor() { return null; }
    public Call getCall() { return null; }
    public long getCallDuration() { return 0; }
    public long getChannelId() { return 0; }
    public String getContent() { return null; }
    public long getEditedTimestamp() { return 0; }
    public Long getEditedTimestampMilliseconds() { return null; }
    public List<ModelMessageEmbed> getEmbeds() { return new ArrayList<>(); }
    public Long getFlags() { return null; }
    public Long getGuildId() { return null; }
    public long getId() { return 0; }
    public Long getInitialAttemptTimestamp() { return null; }
    public Long getLastManualAttemptTimestamp() { return null; }
    public List<LocalAttachment> getLocalAttachments() { return new ArrayList<>(); }
    public GuildMember getMember() { return null; }
    public List<Long> getMentionRoles() { return new ArrayList<>(); }
    public List<User> getMentions() { return new ArrayList<>(); }
    public MessageReference getMessageReference() { return null; }
    public int getType() { return 0; }
    public boolean isLocal() { return false; }
}
