package com.discord.widgets.chat;

import com.discord.models.user.User;

import java.util.List;

@SuppressWarnings("unused")
public final class MessageContent {
    public MessageContent(String content, List<? extends User> mentions) {}

    public List<User> getMentionedUsers() { return null; }
    public String getTextContent() { return ""; }
}
