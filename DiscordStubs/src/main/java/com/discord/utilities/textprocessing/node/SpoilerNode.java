package com.discord.utilities.textprocessing.node;

import com.discord.simpleast.core.node.Node;

import kotlin.Unit;
import kotlin.jvm.functions.Function1;

@SuppressWarnings("unused")
public final class SpoilerNode<T extends SpoilerNode.RenderContext> extends Node<T> implements Spoilerable {
    public interface RenderContext extends BasicRenderContext {
        int getSpoilerColorRes();
        Function1<SpoilerNode<?>, Unit> getSpoilerOnClick();
        int getSpoilerRevealedColorRes();
    }

    @Override
    public boolean isRevealed() { return false; }

    @Override
    public void setRevealed(boolean z) {}
}
