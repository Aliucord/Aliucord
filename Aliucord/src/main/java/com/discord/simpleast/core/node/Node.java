package com.discord.simpleast.core.node;

import android.text.SpannableStringBuilder;

// https://github.com/discord/SimpleAST/blob/master/simpleast-core/src/main/java/com/discord/simpleast/core/node/Node.kt
@SuppressWarnings("unused")
public class Node<R> {
    // Parent
    public static class a<R> extends Node<R> {
        @SafeVarargs
        public a(Node<R>... children) {}

        @Override
        public void render(SpannableStringBuilder builder, R renderContext) {}
    }

    public void render(SpannableStringBuilder builder, R renderContext) {}
}
