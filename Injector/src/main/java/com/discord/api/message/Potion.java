package com.discord.api.message;

import com.discord.api.message.reaction.MessageReactionEmoji;
import com.discord.api.utcdatetime.UtcDateTime;
import java.util.List;

public class Potion {
    public Long usedBy;
    public Integer type;
    public List<MessageReactionEmoji> emoji;
    public UtcDateTime createdAt;
}
