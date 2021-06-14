/*
 * Copyright (c) 2021 Juby210
 * Licensed under the Open Software License version 3.0
 */

package com.aliucord.wrappers;

import com.discord.api.message.embed.*;
import com.discord.api.utcdatetime.UtcDateTime;

import java.util.List;

@SuppressWarnings({"unused", "deprecation"})
public class MessageEmbedWrapper {
    private final MessageEmbed embed;

    public MessageEmbedWrapper(MessageEmbed embed) {
        this.embed = embed;
    }

    public final MessageEmbed raw() {
        return embed;
    }

    public final EmbedAuthor getAuthor() {
        return embed.a();
    }

    public final Integer getColor() {
        return embed.b();
    }

    public final String getDescription() {
        return embed.c();
    }

    public final List<EmbedField> getFields() {
        return embed.d();
    }

    public final EmbedFooter getFooter() {
        return embed.e();
    }

    public final EmbedImage getImage() {
        return embed.f();
    }

    public final EmbedProvider getProvider() {
        return embed.g();
    }

    public final EmbedThumbnail getThumbnail() {
        return embed.h();
    }

    public final UtcDateTime getTimestamp() {
        return embed.i();
    }

    public final String getTitle() {
        return embed.j();
    }

    public final EmbedType getType() {
        return embed.k();
    }

    public final String getUrl() {
        return embed.l();
    }

    public final EmbedVideo getVideo() {
        return embed.m();
    }
}
