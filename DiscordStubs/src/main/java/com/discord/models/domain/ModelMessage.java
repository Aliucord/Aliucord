package com.discord.models.domain;

import com.discord.api.application.Application;
import com.discord.api.guildmember.GuildMember;
import com.discord.api.interaction.Interaction;
import com.discord.api.message.activity.MessageActivity;
import com.discord.api.message.MessageReference;
import com.discord.api.message.embed.MessageEmbed;
import com.discord.api.sticker.Sticker;
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

    public static final int TYPE_APPLICATION_COMMAND = 20;
    public static final int TYPE_CALL = 3;
    public static final int TYPE_CHANNEL_FOLLOW_ADD = 12;
    public static final int TYPE_CHANNEL_ICON_CHANGE = 5;
    public static final int TYPE_CHANNEL_NAME_CHANGE = 4;
    public static final int TYPE_CHANNEL_PINNED_MESSAGE = 6;
    public static final int TYPE_DEFAULT = 0;
    public static final int TYPE_GUILD_DISCOVERY_DISQUALIFIED = 14;
    public static final int TYPE_GUILD_DISCOVERY_GRACE_PERIOD_FINAL_WARNING = 17;
    public static final int TYPE_GUILD_DISCOVERY_GRACE_PERIOD_INITIAL_WARNING = 16;
    public static final int TYPE_GUILD_DISCOVERY_REQUALIFIED = 15;
    public static final int TYPE_GUILD_STREAM = 13;
    public static final int TYPE_LOCAL = -1;
    public static final int TYPE_LOCAL_APPLICATION_COMMAND = -5;
    public static final int TYPE_LOCAL_APPLICATION_COMMAND_SEND_FAILED = -4;
    public static final int TYPE_LOCAL_INVALID_ATTACHMENTS = -3;
    public static final int TYPE_LOCAL_SEND_FAILED = -2;
    public static final int TYPE_RECIPIENT_ADD = 1;
    public static final int TYPE_RECIPIENT_REMOVE = 2;
    public static final int TYPE_REPLY = 19;
    public static final int TYPE_THREAD_CREATED = 18;
    public static final int TYPE_USER_JOIN = 7;
    public static final int TYPE_USER_PREMIUM_GUILD_SUBSCRIPTION = 8;
    public static final int TYPE_USER_PREMIUM_GUILD_SUBSCRIPTION_TIER_1 = 9;
    public static final int TYPE_USER_PREMIUM_GUILD_SUBSCRIPTION_TIER_2 = 10;
    public static final int TYPE_USER_PREMIUM_GUILD_SUBSCRIPTION_TIER_3 = 11;

    public static ModelMessage createLocalApplicationCommandMessage(
            long id,
            String commandName,
            long channelId,
            User interactionUser,
            User author,
            boolean failed,
            boolean loading,
            Long interactionId,
            Clock clock
    ) { return new ModelMessage(null); }

    public static ModelMessage createLocalMessage(
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
            ModelAllowedMentions allowedMentions
    ) { return new ModelMessage(null); }

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
            List<MessageEmbed> embeds,
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
            Interaction interaction,
            ModelAllowedMentions allowedMentions,
            List<LocalAttachment> localAttachments,
            Long lastManualAttemptTimestamp,
            Long initialAttemptTimestamp,
            Integer retries,
            List<ModelSticker> stickers
    ) {}

    public ModelMessage(ModelMessage message) {}

    public User getAuthor() { return null; }
    public Call getCall() { return null; }
    public long getCallDuration() { return 0; }
    public long getChannelId() { return 0; }
    public String getContent() { return null; }
    public long getEditedTimestamp() { return 0; }
    public Long getEditedTimestampMilliseconds() { return null; }
    public List<MessageEmbed> getEmbeds() { return new ArrayList<>(); }
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
    public boolean isLoading() { return false; }
    public boolean isLocal() { return false; }
    public boolean isLocalApplicationCommand() { return false; }
    public boolean isSourceDeleted() { return false; }
    public boolean isWebhook() { return false; }
}
