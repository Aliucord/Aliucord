package com.discord.databinding;

import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.widget.NestedScrollView;
import androidx.viewbinding.ViewBinding;

@SuppressWarnings("unused")
public final class WidgetUserSheetBinding implements ViewBinding {
    public NestedScrollView a;
    /** developer section header */
    public TextView j;
    /** note section header */
    public TextView u;

    @NonNull
    @Override
    public View getRoot() {
        return a;
    }
}
