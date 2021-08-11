package com.discord.api.sticker;

@SuppressWarnings("unused")
public enum StickerType {
    UNKNOWN(-1),
    STANDARD(1),
    GUILD(2);

    private final int apiValue;

    StickerType(int apiType) { apiValue = apiType; }

    public final int getApiValue() { return apiValue; }
}
