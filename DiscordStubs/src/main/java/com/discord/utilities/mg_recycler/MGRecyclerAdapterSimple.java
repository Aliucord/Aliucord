package com.discord.utilities.mg_recycler;

import androidx.annotation.Nullable;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public abstract class MGRecyclerAdapterSimple<D extends MGRecyclerDataPayload> extends MGRecyclerAdapter<D> {
    public MGRecyclerAdapterSimple(RecyclerView recyclerView, boolean z2) { }

    public static <T extends MGRecyclerDataPayload> void access$dispatchUpdates(MGRecyclerAdapterSimple<T> mGRecyclerAdapterSimple, @Nullable DiffUtil.DiffResult diffResult, List<T> list, List<T> internalData) {}

    public void setData(List<? extends D> data) { }
    public final List<D> getInternalData() { return new ArrayList<>(); }

    public D getItem(int i) { return getItem(i); }

    public final void unsubscribeFromUpdates() { }

    @Override
    public int getItemCount() {
        return 0;
    }
}
