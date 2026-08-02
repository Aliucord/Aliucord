package com.discord.api.message;

import com.discord.api.channel.Channel;
import com.discord.api.guildmember.GuildMember;
import com.discord.api.message.attachment.MessageAttachment;
import com.discord.api.role.GuildRole;
import com.discord.api.user.User;
import java.util.Map;

public final class MessageResolved {
    public Map<Long, User> users;
    public Map<Long, GuildMember> members;
    public Map<Long, GuildRole> roles;
    public Map<Long, Channel> channels;
    public Map<Long, Message> messages;
    public Map<Long, MessageAttachment> attachments;
}
