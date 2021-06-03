package com.discord.widgets.chat.list.sheet;

import com.discord.api.channel.Channel;
import com.discord.api.commands.Application;
import com.discord.api.role.GuildRole;
import com.discord.models.member.GuildMember;
import com.discord.models.user.User;
import com.discord.stores.StoreApplicationInteractions;

import java.util.Map;
import java.util.Set;

@SuppressWarnings("unused")
public final class WidgetApplicationCommandBottomSheetViewModel {
    public static final class SlashCommandParam {
        public SlashCommandParam(String name, String value, Integer valueColor, String copyText) {}
    }

    public static final class StoreState {
        public StoreState(
                User user,
                GuildMember member,
                StoreApplicationInteractions.State state,
                Application application,
                Set<Long> mentionedUsers,
                Map<Long, GuildMember> guildMembers,
                Map<Long, GuildRole> guildRoles,
                Map<Long, ? extends User> users,
                Map<Long, Channel> channels,
                Map<String, SlashCommandParam> commandValues
        ) {}
    }

    public static void access$handleStoreState(WidgetApplicationCommandBottomSheetViewModel instance, StoreState state) {}

    public final long getApplicationId() { return 0; }
    public final long getChannelId() { return 0; }
    public final Long getGuildId() { return 0L; }
    public final long getInteractionId() { return 0; }
    public final long getInteractionUserId() { return 0; }
    public final long getMessageId() { return 0; }
}
