package com.discord.databinding;

import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.widget.NestedScrollView;
import androidx.viewbinding.ViewBinding;

@SuppressWarnings("unused")
public final class WidgetChatListActionsBinding implements ViewBinding {
    /** root */
    public NestedScrollView a;
    /** delete button */
    public final TextView e = new TextView(null);

    @NonNull
    @Override
    public View getRoot() {
        return a;
    }
}
