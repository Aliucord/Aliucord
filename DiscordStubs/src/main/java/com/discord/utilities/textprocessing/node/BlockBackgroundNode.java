package com.discord.utilities.textprocessing.node;

import android.text.SpannableStringBuilder;

import com.discord.simpleast.core.node.Node;

import java.util.Arrays;

@SuppressWarnings("unused")
public final class BlockBackgroundNode<R extends BasicRenderContext> extends Node.a<R> implements Spoilerable {
    @SafeVarargs
    public BlockBackgroundNode(boolean inQuote, Node<R>... children) {
        super(Arrays.copyOf(children, children.length));
    }

    @Override
    public boolean isRevealed() { return true; }

    @Override
    public void setRevealed(boolean z) {}

    @Override
    public void render(SpannableStringBuilder builder, R renderContext) {
        super.render(builder, renderContext);
    }
}
