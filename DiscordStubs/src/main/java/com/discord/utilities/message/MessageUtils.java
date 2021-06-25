package com.discord.utilities.message;

import com.discord.api.channel.Channel;
import com.discord.api.channel.ChannelRecipientNick;
import com.discord.api.message.Message;
import com.discord.models.member.GuildMember;

import java.util.*;

@SuppressWarnings("unused")
public final class MessageUtils {
    public static final MessageUtils INSTANCE = new MessageUtils();

    public static int compareMessages(Long id, Long id2) { return -1; }
    public static Map<Long, String> getNickOrUsernames(Message message, Channel channel, Map<Long, GuildMember> map, List<ChannelRecipientNick> list) {
        return Collections.emptyMap();
    }
    public static Comparator<Long> getSORT_BY_IDS_COMPARATOR() { return null; }
    public static boolean isLocal(Message message) { return false; }
    public static boolean isNewer(Long id, Long id2) { return false; }
    public static Message merge(Message old, Message newMessage) { return newMessage; }

    public final long getCallDuration(Message message) { return 0; }
    public final long getEditedTimestampMillis(Message message) { return 0; }
    public final boolean hasAttachments(Message message) { return false; }
    public final boolean hasEmbeds(Message message) { return false; }
    public final boolean hasFlag(Message message) { return false; }
    public final boolean isCrosspost(Message message) { return false; }
    public final boolean isCrossposted(Message message) { return false; }
    public final boolean isEmbeddedMessageType(Message message) { return false; }
    public final boolean isEphemeralMessage(Message message) { return false; }
    public final boolean isFailed(Message message) { return false; }
    public final boolean isInteraction(Message message) { return false; }
    public final boolean isLoading(Message message) { return false; }
    public final boolean isLocalApplicationCommand(Message message) { return false; }
    public final boolean isSourceDeleted(Message message) { return false; }
    public final boolean isSpotifyListeningActivity(Message message) { return false; }
    public final boolean isSystemMessage(Message message) { return false; }
    public final boolean isUrgent(Message message) { return false; }
    public final boolean isUserMessage(Message message) { return false; }
    public final boolean isWebhook(Message message) { return false; }
}
