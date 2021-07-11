package com.discord.utilities.mg_recycler;

import java.util.Collections;
import java.util.List;

public abstract class MGRecyclerAdapterSimple<D extends MGRecyclerDataPayload> extends MGRecyclerAdapter<D> {
    @Override
    public int getItemCount() { return 0; }

    public final List<D> getInternalData() { return Collections.emptyList(); }
}
