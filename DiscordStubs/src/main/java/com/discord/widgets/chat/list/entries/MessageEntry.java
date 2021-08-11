package com.discord.widgets.chat.list.entries;

import com.discord.api.channel.Channel;
import com.discord.api.role.GuildRole;
import com.discord.api.sticker.BaseSticker;
import com.discord.models.member.GuildMember;
import com.discord.models.message.Message;
import com.discord.stores.StoreMessageReplies;
import com.discord.stores.StoreMessageState;

import java.util.Collections;
import java.util.Map;

@SuppressWarnings({ "unused", "InfiniteRecursion" })
public final class MessageEntry extends ChatListEntry {
    public static final class ReplyData {
        public final MessageEntry getMessageEntry() { return new MessageEntry(); }
        public final StoreMessageReplies.MessageState getMessageState() { return StoreMessageReplies.MessageState.Unloaded.INSTANCE; }
        public final boolean isRepliedUserBlocked() { return false; }
    }

    public static final class WelcomeCtaData {
        public final Channel getChannel() { return getChannel(); }
        public final BaseSticker getSticker() { return getSticker(); }
    }

    public final boolean getAnimateEmojis() { return false; }
    public final GuildMember getAuthor() { return null; }
    public final GuildMember getFirstMentionedUser() { return null; }
    public final GuildMember getInteractionAuthor() { return null; }
    @Override
    public String getKey() { return null; }
    public final Message getMessage() { return getMessage(); }
    public final StoreMessageState.State getMessageState() { return getMessageState(); }
    public final Map<Long, String> getNickOrUsernames() { return Collections.emptyMap(); }
    public final ReplyData getReplyData() { return null; }
    public final Map<Long, GuildRole> getRoles() { return Collections.emptyMap(); }
    @Override
    public int getType() { return 0; }
    @Override
    public final boolean isInExpandedBlockedMessageChunk() { return false; }
    public final boolean isThreadStarterMessage() { return false; }
}
