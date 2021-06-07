package com.discord.simpleast.core.node;

/** https://github.com/discord/SimpleAST/blob/master/simpleast-core/src/main/java/com/discord/simpleast/core/node/StyleNode.kt */
@SuppressWarnings("unused")
public class StyleNode<RC, T> {
    /** SpanProvider */
    public interface a<RC> {
        Iterable<?> get(RC renderContext);
    }
}
