package com.discord.widgets.chat.input.autocomplete.adapter;

import android.view.View;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.discord.databinding.WidgetChatInputAutocompleteItemBinding;
import com.discord.widgets.chat.input.autocomplete.ApplicationCommandAutocompletable;

@SuppressWarnings("unused")
public class AutocompleteItemViewHolder extends RecyclerView.ViewHolder {
    private WidgetChatInputAutocompleteItemBinding binding;

    public AutocompleteItemViewHolder(@NonNull View itemView) {
        super(itemView);
    }

    public final void bindCommand(ApplicationCommandAutocompletable applicationCommandAutocompletable, boolean z2) { }
}
