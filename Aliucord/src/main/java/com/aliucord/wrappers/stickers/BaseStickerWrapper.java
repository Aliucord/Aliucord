/*
 * Copyright (c) 2021 Juby210 & Vendicated
 * Licensed under the Open Software License version 3.0
 */

package com.aliucord.wrappers.stickers;

import com.discord.api.sticker.*;

/**
 * Wraps the obfuscated {@link BaseSticker} class to provide nice method names and require only one central
 * update if method names change after an update
 */
@SuppressWarnings({ "unused", "deprecation" })
public class BaseStickerWrapper {
    private final BaseSticker sticker;

    public BaseStickerWrapper(BaseSticker sticker) {
        this.sticker = sticker;
    }

    /** Returns the raw (obfuscated) {@link BaseSticker} Object associated with this wrapper */
    public final BaseSticker raw() {
        return sticker;
    }

    public final StickerFormatType getFormatType() {
        return getFormatType(sticker);
    }

    public final String getFormat() {
        return getFormat(sticker);
    }

    public final StickerPartial getStickerPartial() {
        return getStickerPartial(sticker);
    }

    public final long getId() {
        return getId(sticker);
    }



    public static StickerFormatType getFormatType(BaseSticker sticker) {
        return sticker.a();
    }

    public static String getFormat(BaseSticker sticker) {
        return sticker.b();
    }

    public static StickerPartial getStickerPartial(BaseSticker sticker) {
        return sticker.c();
    }

    public static long getId(BaseSticker sticker) {
        return sticker.d();
    }
}
