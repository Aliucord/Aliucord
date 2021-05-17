package com.discord.utilities.mg_recycler;

import com.discord.utilities.recycler.DiffKeyProvider;

@SuppressWarnings("unused")
public interface MGRecyclerDataPayload extends DiffKeyProvider {
    @Override
    String getKey();

    int getType();
}
