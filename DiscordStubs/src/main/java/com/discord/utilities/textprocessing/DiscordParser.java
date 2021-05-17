package com.discord.utilities.textprocessing;

import android.content.Context;

import com.facebook.drawee.span.DraweeSpanStringBuilder;

@SuppressWarnings("unused")
public final class DiscordParser {
    public enum ParserOptions {
        DEFAULT,
        ALLOW_MASKED_LINKS,
        REPLY
    }

    public static DraweeSpanStringBuilder parseChannelMessage(
            Context context,
            String content,
            MessageRenderContext renderContext,
            MessagePreprocessor messagePreprocessor,
            ParserOptions options,
            boolean edited
    ) {
        return new DraweeSpanStringBuilder();
    }
}
