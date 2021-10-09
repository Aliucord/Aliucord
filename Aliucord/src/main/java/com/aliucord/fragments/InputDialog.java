/*
 * Copyright (c) 2021 Juby210 & Vendicated
 * Licensed under the Open Software License version 3.0
 */

package com.aliucord.fragments;

import static android.view.View.OnClickListener;

import android.annotation.SuppressLint;
import android.text.method.LinkMovementMethod;
import android.view.View;
import android.widget.*;

import com.aliucord.Utils;
import com.discord.app.AppDialog;
import com.discord.databinding.WidgetKickUserBinding;
import com.discord.widgets.user.WidgetKickUser$binding$2;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputLayout;
import com.lytefast.flexinput.R;

import java.util.Objects;

/**
 * Creates a Input Dialog similar to the <strong>Kick User</strong> dialog.
 * This class offers convenient builder methods so you should usually not have to do any layouts manually.
 */
@SuppressWarnings("unused")
public class InputDialog extends AppDialog {
    private static final int resId = Utils.getResId("widget_kick_user", "layout");
    public InputDialog() {
        super(resId);
    }

    private WidgetKickUserBinding binding;
    private CharSequence title;
    private CharSequence description;
    private CharSequence placeholder;
    private View.OnClickListener onCancelListener;
    private View.OnClickListener onOkListener;
    private Integer inputType;

    @SuppressLint("SetTextI18n")
    @Override
    public void onViewBound(View view) {
        super.onViewBound(view);

        binding = WidgetKickUser$binding$2.INSTANCE.invoke(view);

        // Hide ugly redundant secondary "REASON FOR KICK" header
        ((LinearLayout) ((ScrollView) ((LinearLayout) view).getChildAt(2)).getChildAt(0)).getChildAt(1).setVisibility(View.GONE);

        getHeader().setText(title != null ? title : "Input");
        getBody().setText(description != null ? description : "Please enter some text");
        getBody().setMovementMethod(LinkMovementMethod.getInstance());

        TextInputLayout inputLayout = getInputLayout();
        if (placeholder != null) inputLayout.setHint(placeholder);
        else if (title != null) inputLayout.setHint(title);
        else inputLayout.setHint("Text");

        if (inputType != null && inputLayout.getEditText() != null) inputLayout.getEditText().setInputType(inputType);

        MaterialButton okButton = getOKButton();
        LinearLayout buttonLayout = ((LinearLayout) okButton.getParent());
        // The button has no room to breathe - Discord moment, it doesn't even line up with the input field ~ Whatever, fix that
        buttonLayout.setPadding(buttonLayout.getPaddingLeft(), buttonLayout.getPaddingTop(), buttonLayout.getPaddingRight() * 2, buttonLayout.getPaddingBottom());
        okButton.setBackgroundColor(view.getResources().getColor(R.c.uikit_btn_bg_color_selector_brand, view.getContext().getTheme()));
        okButton.setOnClickListener(onOkListener != null ? onOkListener : e -> dismiss());

        getCancelButton().setOnClickListener(onCancelListener != null ? onCancelListener : e -> dismiss());
    }

    /**
     * Returns the root layout of this dialog.
     * Should only be called from within onClickHandlers or onViewBound as it will likely throw a {@link NullPointerException} in other cases
     */
    public final LinearLayout getRoot() { return binding.a; }

    /**
     * Returns the cancel {@link MaterialButton} of this dialog.
     * Should only be called from within onClickHandlers or onViewBound as it will likely throw a {@link NullPointerException} in other cases
     * @see #setOnCancelListener(OnClickListener)
     */
    public final MaterialButton getCancelButton() { return binding.c; }

    /**
     * Returns the OK {@link MaterialButton} of this dialog.
     * Should only be called from within onClickHandlers or onViewBound as it will likely throw a {@link NullPointerException} in other cases
     * @see #setOnOkListener(OnClickListener)
     */
    public final MaterialButton getOKButton() { return binding.d; }

    /**
     * Returns the body of this dialog.
     * Should only be called from within onClickHandlers or onViewBound as it will likely throw a {@link NullPointerException} in other cases
     * @see #setDescription(CharSequence)
     */
    public final TextView getBody() { return binding.b; }

    /**
     * Returns the header of this dialog.
     * Should only be called from within onClickHandlers or onViewBound as it will likely throw a {@link NullPointerException} in other cases
     * @see #setTitle(CharSequence)
     */
    public final TextView getHeader() { return binding.f; }

    /**
     * Returns the {@link TextInputLayout} of this dialog.
     * Should only be called from within onClickHandlers or onViewBound as it will likely throw a {@link NullPointerException} in other cases
     * @see #setInputType(int)
     * @see #getInput()
     */
    public final TextInputLayout getInputLayout() { return binding.e; }

    /**
     * Returns the input the user entered.
     * Should only be called from within onClickHandlers or onViewBound as it will likely throw a {@link NullPointerException} in other cases
     * @see #getInputLayout()
     */
    public String getInput() { return Objects.requireNonNull(getInputLayout().getEditText()).getText().toString(); }

    /**
     * Sets the title of this dialog
     * @param title The description
     * @return Builder for chaining
     */
    public InputDialog setTitle(CharSequence title) {
        this.title = title;
        return this;
    }

    /**
     * Sets the description of this dialog
     * @param description The description
     * @return Builder for chaining
     */
    public InputDialog setDescription(CharSequence description) {
        this.description = description;
        return this;
    }

    /**
     * Sets the placeholder text for the input field (By default the title if set or "Text")
     * @param placeholder The placeholder text
     * @return Builder for chaining
     */
    public InputDialog setPlaceholderText(CharSequence placeholder) {
        this.placeholder = placeholder;
        return this;
    }

    /**
     * Sets the {@link OnClickListener} that will be called when the OK button is pressed (By default simply closes this dialog)
     * @param listener The listener
     * @return Builder for chaining
     */
    public InputDialog setOnOkListener(OnClickListener listener) {
        onOkListener = listener;
        return this;
    }

    /**
     * Sets the {@link OnClickListener} that will be called when the cancel button is pressed (By default simply closes this dialog)
     * @param listener The listener
     * @return Builder for chaining
     */
    public InputDialog setOnCancelListener(OnClickListener listener) {
        onCancelListener = listener;
        return this;
    }

    /**
     * Sets the {@link android.text.InputType}
     * @param type The input type
     * @return Builder for chaining
     */
    public InputDialog setInputType(int type) {
        inputType = type;
        return this;
    }
}
