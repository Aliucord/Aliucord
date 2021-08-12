package com.discord.databinding;

import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.viewbinding.ViewBinding;

public final class WidgetDebuggingAdapterItemBinding implements ViewBinding {
    /** root */
    public LinearLayout a;
    /** logMessage */
    public TextView b;

    @NonNull
    @Override
    public View getRoot() {
        return a;
    }
}
