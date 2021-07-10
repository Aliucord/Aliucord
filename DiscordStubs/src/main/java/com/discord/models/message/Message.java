package com.discord.models.message;

import androidx.annotation.NonNull;

import com.discord.api.application.Application;
import com.discord.api.botuikit.Component;
import com.discord.api.channel.Channel;
import com.discord.api.interaction.Interaction;
import com.discord.api.message.LocalAttachment;
import com.discord.api.message.MessageReference;
import com.discord.api.message.activity.MessageActivity;
import com.discord.api.message.allowedmentions.MessageAllowedMentions;
import com.discord.api.message.attachment.MessageAttachment;
import com.discord.api.message.call.MessageCall;
import com.discord.api.message.embed.MessageEmbed;
import com.discord.api.message.reaction.MessageReaction;
import com.discord.api.sticker.Sticker;
import com.discord.api.sticker.StickerPartial;
import com.discord.api.user.User;
import com.discord.api.utcdatetime.UtcDateTime;

import java.util.*;

@SuppressWarnings("unused")
public final class Message {
    public final boolean canResend() { return false; }

    public final MessageActivity getActivity() { return null; }

    public final MessageAllowedMentions getAllowedMentions() { return new MessageAllowedMentions(); }

    public final Application getApplication() { return null; }

    public final Long getApplicationId() { return null; }

    public final List<MessageAttachment> getAttachments() { return new ArrayList<>(); }

    @NonNull
    @SuppressWarnings("ConstantConditions")
    public final User getAuthor() { return null; }

    public final MessageCall getCall() { return null; }

    public final long getCallDuration() { return 0; }

    public final long getChannelId() { return 0; }

    public final List<Component> getComponents() { return new ArrayList<>(); }

    public final String getContent() { return null; }

    public final UtcDateTime getEditedTimestamp() { return null; }

    public final List<MessageEmbed> getEmbeds() { return new ArrayList<>(); }

    public final Long getFlags() { return null; }

    public final Long getGuildId() { return null; }

    public final boolean getHasLocalUploads() { return false; }

    public final Boolean getHit() { return null; }

    public final long getId() { return 0; }

    public final Long getInitialAttemptTimestamp() { return null; }

    public final Interaction getInteraction() { return null; }

    public final Long getLastManualAttemptTimestamp() { return null; }

    public final List<LocalAttachment> getLocalAttachments() { return new ArrayList<>(); }

    public final Boolean getMentionEveryone() { return false; }

    public final List<Long> getMentionRoles() { return new ArrayList<>(); }

    public final List<User> getMentions() { return new ArrayList<>(); }

    public final MessageReference getMessageReference() { return null; }

    public final String getNonce() { return ""; }

    public final Integer getNumRetries() { return null; }

    public final Boolean getPinned() { return false; }

    public final List<MessageReaction> getReactions() { return new ArrayList<>(); }

    public final Map<String, MessageReaction> getReactionsMap() { return new HashMap<>(); }

    public final com.discord.api.message.Message getReferencedMessage() { return null; }

    public final List<StickerPartial> getStickerItems() { return new ArrayList<>(); }

    public final List<Sticker> getStickers() { return new ArrayList<>(); }

    public final Channel getThread() { return null; }

    public final UtcDateTime getTimestamp() { return new UtcDateTime(0); }

    public final Boolean getTts() { return false; }

    public final Integer getType() { return null; }

    public final Long getWebhookId() { return null; }

    public final boolean hasAttachments() { return false; }

    public final boolean hasEmbeds() { return false; }

    public final boolean hasFlag(long j) { return false; }

    public final boolean hasStickers() { return false; }

    public final boolean hasThread() { return false; }

    public final boolean isCrosspost() { return false; }

    public final boolean isCrossposted() { return false; }

    public final boolean isEmbeddedMessageType() { return false; }

    public final boolean isEphemeralMessage() { return false; }

    public final boolean isFailed() { return false; }

    public final boolean isInteraction() { return false; }

    public final boolean isLoading() { return false; }

    public final boolean isLocal() { return false; }

    public final boolean isLocalApplicationCommand() { return false; }

    public final boolean isSourceDeleted() { return false; }

    public final boolean isSpotifyListeningActivity() { return false; }

    public final boolean isSystemMessage() { return false; }

    public final boolean isUrgent() { return false; }

    public final boolean isUserMessage() { return false; }

    public final boolean isWebhook() { return false; }

    public final Message merge(com.discord.api.message.Message message) { return this; }

    public final com.discord.api.message.Message synthesizeApiMessage() { return new com.discord.api.message.Message(); }

    public Message(com.discord.api.message.Message message) { }
}
