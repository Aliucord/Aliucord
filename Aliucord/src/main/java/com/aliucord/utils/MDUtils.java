/*
 * This file is part of Aliucord, an Android Discord client mod.
 * Copyright (c) 2021 Juby210 & Vendicated
 * Licensed under the Open Software License version 3.0
 */

package com.aliucord.utils;

import android.content.Context;
import android.text.SpannableStringBuilder;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.aliucord.Main;
import com.discord.simpleast.code.CodeNode;
import com.discord.simpleast.core.node.Node;
import com.discord.simpleast.core.parser.Parser;
import com.discord.simpleast.core.parser.Rule;
import com.discord.utilities.textprocessing.*;
import com.discord.utilities.textprocessing.node.BasicRenderContext;
import com.discord.utilities.textprocessing.node.BlockBackgroundNode;

import java.util.List;
import java.util.Map;

import c.a.k.b;

@SuppressWarnings("unchecked")
public final class MDUtils {
    public static class RenderContext implements BasicRenderContext {
        private final Context context;

        public RenderContext(Context ctx) { context = ctx; }

        @Override
        public Context getContext() { return context; }
    }

    /**
     * Discord's MD Parser that's used for messages by default
     */
    public static Parser<MessageRenderContext, Node<MessageRenderContext>, MessageParseState> parser;

    private static Map<String, List<? extends Rule<MessageRenderContext, ? extends Node<MessageRenderContext>, MessageParseState>>> languageRules;

    /**
     * Renders code block into {@link SpannableStringBuilder}
     * @param context Context
     * @param builder Builder
     * @param language Code language
     * @param content Code block content
     * @return Builder for chaining
     */
    @NonNull
    public static SpannableStringBuilder renderCodeBlock(@NonNull Context context, @NonNull SpannableStringBuilder builder, @Nullable String language, @NonNull String content) {
        var rules = language == null || languageRules == null ? null : languageRules.get(language);
        var node = new BlockBackgroundNode<>(false, new CodeNode<>(
            rules == null ?
                new CodeNode.a.b(content) :
                new CodeNode.a.a<>(content, parser.parse(content, MessageParseState.access$getInitialState$cp(), rules)),
            language,
            Rules$createCodeBlockRule$codeStyleProviders$1.INSTANCE
        ));
        node.render(builder, new RenderContext(context));
        return builder;
    }

    /**
     * Renders discord spice markdown
     * @param source The markdown to render
     * @return Rendered markdown
     */
    public static CharSequence render(CharSequence source) {
        try {
            return b.l(source, new Object[0], null, 2);
        } catch (Throwable e) { Main.logger.error("Failed to render markdown", e); }
        return source;
    }

    static {
        try {
            parser = (Parser<MessageRenderContext, Node<MessageRenderContext>, MessageParseState>) ReflectUtils.getField(DiscordParser.class, null, "SAFE_LINK_PARSER");
            languageRules = (Map<String, List<? extends Rule<MessageRenderContext, ? extends Node<MessageRenderContext>, MessageParseState>>>) ReflectUtils.getField(Rules.INSTANCE.createCodeBlockRule(), "a");
        } catch (Throwable e) { Main.logger.error("Failed to get parser and language rules", e); }
    }
}
