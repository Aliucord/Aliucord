package com.discord.utilities.mg_recycler;

import android.view.View;

import androidx.annotation.LayoutRes;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

@SuppressWarnings("unused")
public class MGRecyclerViewHolder<T extends MGRecyclerAdapter<D>, D> extends RecyclerView.ViewHolder {
    public final T adapter;

    public MGRecyclerViewHolder(@NonNull View itemView, T t) {
        super(itemView);
        adapter = t;
    }

    public MGRecyclerViewHolder(@LayoutRes int id, T t) {
        this(new View(null), t);
    }
}
