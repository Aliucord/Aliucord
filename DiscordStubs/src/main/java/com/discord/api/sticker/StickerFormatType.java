package com.discord.api.sticker;

@SuppressWarnings("unused")
public enum StickerFormatType {
    UNKNOWN(-1),
    PNG(1),
    APNG(2),
    LOTTIE(3);

    private final int apiValue;

    StickerFormatType(int apiType) { apiValue = apiType; }

    public final int getApiValue() { return apiValue; }
}
