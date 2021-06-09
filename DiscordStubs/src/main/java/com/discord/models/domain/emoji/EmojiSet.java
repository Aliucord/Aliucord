package com.discord.models.domain.emoji;

import java.util.List;
import java.util.Map;
import java.util.Set;

@SuppressWarnings("unused")
public class EmojiSet {
    public final Map<Long, List<Emoji>> customEmojis = null;
    public final Map<String, Emoji> emojiIndex = null;
    public final Set<Emoji> favoriteEmoji = null;
    public final List<Emoji> recentEmojis = null;
    public final Map<EmojiCategory, List<Emoji>> unicodeEmojis = null;

    public EmojiSet(
            Map<EmojiCategory, List<Emoji>> unicodeEmojis,
            Map<Long, List<Emoji>> customEmojis,
            Map<String, Emoji> emojiIndex,
            List<Emoji> recentEmojis,
            Set<Emoji> favoriteEmoji) { }
}
