package com.discord.models.member;

import androidx.annotation.ColorInt;

import com.discord.api.channel.Channel;
import com.discord.api.channel.ChannelRecipientNick;
import com.discord.api.role.GuildRole;
import com.discord.api.utcdatetime.UtcDateTime;
import com.discord.models.user.User;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@SuppressWarnings("unused")
public final class GuildMember {
    public static final Companion Companion = new Companion();
    private static final List<Long> emptyRoles = new ArrayList<>();

    public static final class Companion {
        public final int compareUserNames(User user, User user2, GuildMember guildMember, GuildMember guildMember2) { return 0; }

        public final GuildMember from(Map<Long, GuildRole> roles, com.discord.api.guildmember.GuildMember guildMember, long j) { return new GuildMember(); }

        public final int getColor(@ColorInt int i, @ColorInt int i2) { return 0; }

        public final int getColor(GuildMember guildMember, @ColorInt int i) { return 0; }

        public final String getNickOrUsername(GuildMember guildMember, User user) { return ""; }

        public final String getNickOrUsername(User user, GuildMember guildMember, Channel channel, List<ChannelRecipientNick> list) { return ""; }
    }

    public GuildMember(
            @ColorInt int color,
            long hoistRoleId,
            List<Long> roles,
            String nick,
            String premiumSince,
            boolean pending,
            UtcDateTime joinedAt,
            String avatarHash,
            long guildId,
            long userId) { }

    public static int compareUserNames(User user, User user2, GuildMember guildMember, GuildMember guildMember2) { return 0; }

    public static int getColor(@ColorInt int i, @ColorInt int i2) { return 0; }

    public static int getColor(GuildMember guildMember, @ColorInt int i) { return 0; }

    public static String getNickOrUsername(GuildMember guildMember, User user) { return ""; }

    public final String getAvatarHash() { return null; }

    public final int getColor() { return 0; }

    public final long getGuildId() { return 0; }

    public final long getHoistRoleId() { return 0; }

    public final UtcDateTime getJoinedAt() { return new UtcDateTime(0); }

    public final String getNick() { return null; }

    public final boolean getPending() { return false; }

    public final String getPremiumSince() { return null; }

    public final List<Long> getRoles() { return new ArrayList<>(); }

    public final long getUserId() { return 0; }

    public final boolean hasAvatar() { return false; }

    public GuildMember() { }
}
