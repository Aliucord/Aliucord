/*
 * This file is part of Aliucord, an Android Discord client mod.
 * Copyright (c) 2021 Juby210 & Vendicated
 * Licensed under the Open Software License version 3.0
 */

package com.aliucord.views;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.*;
import android.widget.EditText;
import android.widget.LinearLayout;

import androidx.annotation.*;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;

import com.aliucord.Utils;
import com.aliucord.utils.DimenUtils;
import com.google.android.material.textfield.TextInputLayout;

public class TextInput extends CardView {
    public TextInputLayout layout;

    public TextInput(@NonNull Context context) {
        this(context, null, null, null, null);
    }

    public TextInput(@NonNull Context context, @Nullable CharSequence hint) {
        this(context, hint, null, null, null);
    }

    public TextInput(@NonNull Context context, @Nullable CharSequence hint, @Nullable String value) {
        this(context, hint, value, null, null);
    }

    public TextInput(@NonNull Context context, @Nullable CharSequence hint, @Nullable String value, @Nullable TextWatcher textChangedListener) {
        this(context, hint, value, textChangedListener, null);
    }

    public TextInput(@NonNull Context context, @Nullable CharSequence hint, @Nullable String value, @Nullable View.OnClickListener endIconOnClick) {
        this(context, hint, value, null, endIconOnClick);
    }

    public TextInput(@NonNull Context context, @Nullable CharSequence hint, @Nullable String value, @Nullable TextWatcher textChangedListener, @Nullable View.OnClickListener endIconOnClick) {
        super(context);
        LinearLayout root = new LinearLayout(context);
        LayoutInflater.from(context).inflate(Utils.getResId("widget_change_guild_identity", "layout"), root);
        layout = (TextInputLayout) root.findViewById(Utils.getResId("set_nickname_text", "id"));
        ((CardView) layout.getParent()).removeView(layout);
        addView(layout);
        setCardBackgroundColor(Color.TRANSPARENT);
        getRoot().setHint(hint == null ? "Enter Text" : hint);
        if(value != null && !value.isEmpty()) getEditText().setText(value);
        //if(placeholder != null && !placeholder.isEmpty()) getEditText().setPlaceholder(placeholder);
        setRadius(DimenUtils.getDefaultCardRadius());
        getRoot().setEndIconVisible(false);
        getEditText().addTextChangedListener(textChangedListener == null ? new TextWatcher() {
            public void afterTextChanged(Editable s) {
                getRoot().setEndIconVisible(!s.toString().isEmpty());
            }

            public void beforeTextChanged(CharSequence s, int start, int count, int after) { }
            public void onTextChanged(CharSequence s, int start, int before, int count) { }
        } : textChangedListener);
        getRoot().setEndIconOnClickListener(endIconOnClick == null ? v -> getEditText().setText("") : endIconOnClick);
    }

    /**
     * Returns the root layout
     * @return TextInputLayout
     */
    @NonNull
    public TextInputLayout getRoot() {
        return layout;
    }

    /**
     * Returns the main edit text
     * @return EditText
     */
    @NonNull
    public EditText getEditText() {
        return (EditText) ((ViewGroup) getRoot().getChildAt(0)).getChildAt(0);
    }

    /**
     * Sets the hint message
     * @param hint The hint
     * @return self
     * @noinspection UnusedReturnValue
     */
    public TextInput setHint(@NonNull CharSequence hint) {
        getRoot().setHint(hint);
        return this;
    }

    /**
     * Sets the hint message
     * @param hint The hint res id
     * @return self
     */
    public TextInput setHint(@StringRes int hint) {
        getRoot().setHint(hint);
        return this;
    }

    /**
     * Sets the end icon to the specified drawable and sets it tint to the users's chosen theme
     * @param icon End icon drawable
     * @return self
     */
    public TextInput setThemedEndIcon(@NonNull Drawable icon) {
        getRoot().setEndIconDrawable(Utils.tintToTheme(icon.mutate()));
        return this;
    }

    /**
     * Sets the end icon to the specified drawable and sets it tint to the users's chosen theme
     * @param icon End icon drawable res id
     * @return self
     */
    public TextInput setThemedEndIcon(@DrawableRes int icon) {
        getRoot().setEndIconDrawable(Utils.tintToTheme(ContextCompat.getDrawable(getRoot().getContext(), icon)));
        return this;
    }
}
