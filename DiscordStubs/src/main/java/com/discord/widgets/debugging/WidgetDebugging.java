package com.discord.widgets.debugging;

import android.view.ViewGroup;

import androidx.annotation.NonNull;

import com.discord.app.AppLog;
import com.discord.databinding.WidgetDebuggingAdapterItemBinding;
import com.discord.utilities.mg_recycler.MGRecyclerAdapterSimple;
import com.discord.utilities.mg_recycler.MGRecyclerViewHolder;

public final class WidgetDebugging {
    public static final class Adapter extends MGRecyclerAdapterSimple<AppLog.LoggedItem> {
        @SuppressWarnings("unused")
        public static final class Item extends MGRecyclerViewHolder<Adapter, AppLog.LoggedItem> {
            private WidgetDebuggingAdapterItemBinding binding;

            public Item(int id, Adapter adapter) {
                super(id, adapter);
            }

            @Override
            public void onConfigure(int i, AppLog.LoggedItem data) {
                super.onConfigure(i, data);
            }
        }

        @NonNull
        @Override
        public MGRecyclerViewHolder<?, AppLog.LoggedItem> onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            return new Item(0, this);
        }
    }
}
