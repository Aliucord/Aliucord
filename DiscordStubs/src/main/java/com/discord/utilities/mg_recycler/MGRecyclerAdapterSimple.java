package com.discord.utilities.mg_recycler;

public abstract class MGRecyclerAdapterSimple<D extends MGRecyclerDataPayload> extends MGRecyclerAdapter<D> {
    @Override
    public int getItemCount() {
        return 0;
    }
}
