package com.discord.databinding;

import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.viewbinding.ViewBinding;

import com.discord.views.LoadingButton;
import com.google.android.material.button.MaterialButton;

@SuppressWarnings("unused")
public final class LeaveGuildDialogBinding implements ViewBinding {
    // root
    @NonNull
    public final LinearLayout a;

    // cancel
    @NonNull
    public final MaterialButton b;

    // leave
    @NonNull
    public final LoadingButton c;

    // body
    @NonNull
    public final TextView d;

    // header
    @NonNull
    public final TextView e;

    public LeaveGuildDialogBinding(
            @NonNull LinearLayout root,
            @NonNull MaterialButton cancel,
            @NonNull LoadingButton leave,
            @NonNull TextView body,
            @NonNull TextView header,
            @NonNull LinearLayout headerContainer
    ) {
        a = root;
        b = cancel;
        c = leave;
        d = body;
        e = header;
    }


    @NonNull
    @Override
    public View getRoot() { return a; }
}
