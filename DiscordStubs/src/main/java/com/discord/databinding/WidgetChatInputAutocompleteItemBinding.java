package com.discord.databinding;

import android.view.View;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.Barrier;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.viewbinding.ViewBinding;
import com.discord.utilities.view.text.SimpleDraweeSpanTextView;
import com.discord.views.StatusView;
import com.facebook.drawee.view.SimpleDraweeView;

public final class WidgetChatInputAutocompleteItemBinding implements ViewBinding {
    /** Root */
    public ConstraintLayout a;

    public SimpleDraweeView b;

    /** chatInputItemDescription */
    public TextView c;

    public SimpleDraweeSpanTextView d;

    /** ChatInputItemName */
    public TextView e;

    /** chatInputItemNameRight */
    public TextView f;

    public StatusView g;

    public WidgetChatInputAutocompleteItemBinding(@NonNull ConstraintLayout constraintLayout, @NonNull Barrier barrier, @NonNull SimpleDraweeView simpleDraweeView, @NonNull TextView textView, @NonNull View view, @NonNull SimpleDraweeSpanTextView simpleDraweeSpanTextView, @NonNull TextView textView2, @NonNull TextView textView3, @NonNull StatusView statusView) { }

    @Override
    @NonNull
    public View getRoot() {
        return this.a;
    }
}
