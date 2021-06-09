package com.discord.models.domain.emoji;

import com.discord.api.user.User;

import java.util.List;

public final class ModelEmojiGuild {
    public ModelEmojiGuild(
            long id,
            String name,
            boolean managed,
            List<Long> roles,
            boolean requiredColons,
            User author,
            boolean animated,
            boolean available) { }

    public final boolean getAnimated() { return false; }
    public final boolean getAvailable() { return false; }
    public final long getId() { return 0; }
    public final boolean getManaged() { return false; }
    public final String getName() { return null; }
    public final boolean getRequiredColons() { return false; }
    public final List<Long> getRoles() { return null; }
    public final User getUser() { return null; }
}
