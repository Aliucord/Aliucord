package com.discord.models.domain.emoji;

import androidx.annotation.Nullable;

public enum EmojiCategory {
    FAVORITE("favorite", false),
    RECENT("recent", false),
    CUSTOM("custom", false),
    PEOPLE("people", true),
    NATURE("nature", true),
    FOOD("food", true),
    // ACTIVITY(ActivityChooserModel.ATTRIBUTE_ACTIVITY, true),
    TRAVEL("travel", true),
    OBJECTS("objects", true),
    SYMBOLS("symbols", true),
    FLAGS("flags", true);
    
    public final boolean containsOnlyUnicode = false;

    private EmojiCategory(String str, boolean z2) { }

    @Nullable
    public static EmojiCategory getByString(String str) { return null; }
}
