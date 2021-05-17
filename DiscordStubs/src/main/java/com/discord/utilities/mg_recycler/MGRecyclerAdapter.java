package com.discord.utilities.mg_recycler;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public abstract class MGRecyclerAdapter<D> extends RecyclerView.Adapter<MGRecyclerViewHolder<?, D>> {
    @Override
    public void onBindViewHolder(@NonNull MGRecyclerViewHolder<?, D> holder, int position) {}
}
