package com.discord.utilities.message;

import android.content.Context;

import androidx.annotation.NonNull;

import com.discord.api.channel.Channel;
import com.discord.api.channel.ChannelRecipientNick;
import com.discord.models.member.GuildMember;
import com.discord.models.message.Message;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

@SuppressWarnings("unused")
public final class MessageUtils {
    public static final MessageUtils INSTANCE = new MessageUtils();

    public static int compareMessages(Long id, Long id2) { return -1; }
    public static Map<Long, String> getNickOrUsernames(@NonNull Message message, Channel channel, @NonNull Map<Long, GuildMember> channelMembers, List<ChannelRecipientNick> list) {
        return Collections.emptyMap();
    }
    public static Comparator<Long> getSORT_BY_IDS_COMPARATOR() { return Comparator.comparingLong(i -> 0); }
    public static boolean isNewer(Long id, Long id2) { return false; }

    public final int getSystemMessageUserJoin(Context ctx, long id) { return 0; }
}
