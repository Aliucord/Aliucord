package com.discord.simpleast.core.parser;

import com.discord.simpleast.core.node.Node;

import java.util.List;

/** https://github.com/discord/SimpleAST/blob/master/simpleast-core/src/main/java/com/discord/simpleast/core/parser/Parser.kt */
public class Parser<R, T extends Node<R>, S> {
    public final List<T> parse(CharSequence source, S initialState) { return null; }
    public final List<T> parse(CharSequence source, S initialState, List<? extends Rule<R, ? extends T, S>> rules) { return null; }
}
