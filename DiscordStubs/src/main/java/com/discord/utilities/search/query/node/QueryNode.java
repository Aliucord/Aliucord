package com.discord.utilities.search.query.node;

import android.content.Context;

import com.discord.simpleast.core.node.Node;

public abstract class QueryNode extends Node<Context> {
    public abstract CharSequence getText();
}
