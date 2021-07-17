package com.discord.databinding;

import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.viewbinding.ViewBinding;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputLayout;

public class WidgetKickUserBinding implements ViewBinding {
    /** root */
    @NonNull
    public final LinearLayout a;
    /** body */
    @NonNull
    public final TextView b;

    /** cancel */
    @NonNull
    public final MaterialButton c;
    /** kick */
    @NonNull
    public final MaterialButton d;

    /** reason */
    @NonNull
    public final TextInputLayout e;
    /** title */
    @NonNull
    public final TextView f;

    public WidgetKickUserBinding(
            @NonNull LinearLayout linearLayout,
            @NonNull TextView textView,
            @NonNull MaterialButton materialButton,
            @NonNull MaterialButton materialButton2,
            @NonNull TextInputLayout textInputLayout,
            @NonNull TextView textView2) {
        a = linearLayout;
        b = textView;
        c = materialButton;
        d = materialButton2;
        e = textInputLayout;
        f = textView2;
    }

    @Override
    @NonNull
    public View getRoot() {
        return a;
    }
}
