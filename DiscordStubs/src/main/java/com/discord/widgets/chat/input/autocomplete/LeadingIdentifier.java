package com.discord.widgets.chat.input.autocomplete;

@SuppressWarnings("unused")
public enum LeadingIdentifier {
    APP_COMMAND('/'),
    EMOJI_AND_STICKERS(':'),
    CHANNELS('#'),
    MENTION('@'),
    NONE(null);

    public static final Companion Companion = new Companion();
    public static final class Companion {
        public final LeadingIdentifier fromChar(Character ch) { return LeadingIdentifier.NONE; }
    }
    LeadingIdentifier(Character ch) { }
    public final Character getIdentifier() { return null; }
}