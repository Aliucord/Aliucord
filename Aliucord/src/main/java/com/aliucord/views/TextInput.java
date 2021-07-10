/*
 * Copyright (c) 2021 Juby210 & Vendicated
 * Licensed under the Open Software License version 3.0
 */

package com.aliucord.views;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.drawable.GradientDrawable;
import android.text.Editable;
import android.text.InputType;
import android.view.inputmethod.EditorInfo;

import com.aliucord.Utils;
import com.discord.utilities.color.ColorCompat;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.lytefast.flexinput.R$b;
import com.lytefast.flexinput.R$h;

public class TextInput extends TextInputLayout {
    @SuppressLint("SetTextI18n")
    public TextInput(Context context) {
        super(context);
        int padding = Utils.dpToPx(12);
        int bgColor = ColorCompat.getThemedColor(context, R$b.colorBackgroundTertiary);
        ColorStateList hintColor = ColorStateList.valueOf(ColorCompat.getThemedColor(context, R$b.colorHeaderSecondary));

        setHintTextColor(ColorCompat.INSTANCE.createDefaultColorStateList(ColorCompat.getThemedColor(context, R$b.colorTextMuted)));
        setBoxStrokeWidth(0);
        setBoxStrokeWidthFocused(0);
        setErrorTextAppearance(R$h.UiKit_TextAppearance);
        setHintTextAppearance(R$h.UiKit_TextAppearance_MaterialEditText_Label);
        setHintTextColor(hintColor);
        setDefaultHintTextColor(ColorStateList.valueOf(ColorCompat.getThemedColor(context, R$b.colorTextMuted)));
        setPadding(padding, padding, padding, 0);

        GradientDrawable shape = new GradientDrawable();
        shape.setCornerRadius(Utils.getDefaultCardRadius());
        shape.setColor(bgColor);
        setBackground(shape);

        TextInputEditText input = new TextInputEditText(context);
        input.setTextSize(16);
        input.setBackgroundColor(0);
        input.setTextColor(ColorCompat.getThemedColor(context, R$b.colorHeaderPrimary));
        input.setInputType(InputType.TYPE_CLASS_TEXT);
        input.setImeOptions(EditorInfo.IME_ACTION_DONE);
        input.setPadding(0, padding, 0, padding);
        input.setOnFocusChangeListener((view, focus) -> {
            Editable text = input.getText();
            if (text == null) return;
            String textStr = text.toString();
            if (!focus && textStr.equals("")) input.setText(" ");
            if (focus && textStr.startsWith(" ")) input.setText(textStr.trim().replace(" ", ""));
        });
        if (!input.hasFocus() && (input.getText() == null || input.getText().toString().equals(""))) input.setText(" ");
        addView(input);
    }
}
