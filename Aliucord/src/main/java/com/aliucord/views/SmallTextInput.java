/*
 * This file is part of Aliucord, an Android Discord client mod.
 * Copyright (c) 2021 Juby210 & Vendicated
 * Licensed under the Open Software License version 3.0
 */

package com.aliucord.views;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.view.LayoutInflater;
import android.widget.LinearLayout;
import android.graphics.Color;
import android.graphics.drawable.Drawable;

import androidx.core.content.ContextCompat;
import androidx.cardview.widget.CardView;
import androidx.annotation.StringRes;
import androidx.annotation.DrawableRes;

import com.aliucord.Utils;
import com.aliucord.utils.DimenUtils;

import com.discord.utilities.color.ColorCompat;
import com.discord.utilities.icon.IconUtils;

import com.google.android.material.textfield.TextInputLayout;
import com.google.android.material.textfield.TextInputEditText;

import com.lytefast.flexinput.R;

public class SmallTextInput extends CardView {
    public TextInputLayout layout;

    public SmallTextInput(Context ctx) {
        super(ctx);
        LinearLayout root = new LinearLayout(ctx);
        LayoutInflater.from(ctx).inflate(Utils.getResId("widget_change_guild_identity", "layout"), root);
        layout = (TextInputLayout) root.findViewById(Utils.getResId("set_nickname_text", "id"));
        ((CardView) layout.getParent()).removeView(layout);
        addView(layout);
        setCardBackgroundColor(Color.TRANSPARENT);
        getRoot().setHint("Enter Text");
        setRadius(DimenUtils.getDefaultCardRadius());
    }

    /**
     * Returns the root {@link TextInputLayout}
     */
    public TextInputLayout getRoot() {
        return layout;
    }

    /**
     * Returns the main {@link TextInputEditText}
     */
    public TextInputEditText getEditText() {
        return (TextInputEditText) ((ViewGroup) getRoot().getChildAt(0)).getChildAt(0);
    }

    /**
     * Sets the hint message
     * @param hint The hint
     * @return self
     */
    public SmallTextInput setHint(String hint) {
        getRoot().setHint(hint);
        return this;
    }

    /**
     * Sets the hint message
     * @param hint The hint res id
     * @return self
     */
    public SmallTextInput setHint(@StringRes int hint) {
        getRoot().setHint(hint);
        return this;
    }

    /**
     * Sets the end icon to the specified drawable and sets it tint to the users's chosen theme
     * @param icon End icon drawable
     * @return self
     */
    public SmallTextInput setThemedEndIcon(Drawable icon) {
        getRoot().setEndIconDrawable(Utils.tintToTheme(icon.mutate()));
        return this;
    }

    /**
     * Sets the end icon to the specified drawable and sets it tint to the users's chosen theme
     * @param icon End icon drawable res id
     * @return self
     */
    public SmallTextInput setThemedEndIcon(@DrawableRes int icon) {
        getRoot().setEndIconDrawable(Utils.tintToTheme(ContextCompat.getDrawable(getRoot().getContext(), icon)));
        return this;
    }
}