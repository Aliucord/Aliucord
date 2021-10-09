/*
 * Copyright (c) 2021 Juby210 & Vendicated
 * Licensed under the Open Software License version 3.0
 */

package com.aliucord.fragments;

import android.text.method.LinkMovementMethod;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.core.content.ContextCompat;

import com.aliucord.Utils;
import com.discord.app.AppDialog;
import com.discord.databinding.LeaveGuildDialogBinding;
import com.discord.views.LoadingButton;
import com.discord.widgets.guilds.leave.WidgetLeaveGuildDialog$binding$2;
import com.google.android.material.button.MaterialButton;
import com.lytefast.flexinput.R;

/**
 * Creates a Confirmation Dialog similar to the <strong>Leave Guild</strong> dialog.
 * This class offers convenient builder methods so you should usually not have to do any layouts manually.
 */
@SuppressWarnings("unused")
public class ConfirmDialog extends AppDialog {
    private static final int resId = Utils.getResId("leave_guild_dialog", "layout");
    public ConfirmDialog() {
        super(resId);
    }

    private LeaveGuildDialogBinding binding;
    private CharSequence title;
    private CharSequence description;
    private boolean isDangerous = false;
    private View.OnClickListener onCancelListener;
    private View.OnClickListener onOkListener;

    @Override
    public void onViewBound(View view) {
        super.onViewBound(view);

        binding = WidgetLeaveGuildDialog$binding$2.INSTANCE.invoke(view);
        LoadingButton okButton = getOKButton();
        okButton.setText("OK");
        okButton.setIsLoading(false);
        okButton.setOnClickListener(onOkListener != null ? onOkListener : e -> dismiss());
        var btnColor = isDangerous ? R.c.uikit_btn_bg_color_selector_red : R.c.uikit_btn_bg_color_selector_brand;
        okButton.setBackgroundColor(ContextCompat.getColor(view.getContext(), btnColor));

        getCancelButton().setOnClickListener(onCancelListener != null ? onCancelListener : e -> dismiss());
        getHeader().setText(title != null ? title : "Confirm");
        getBody().setText(description != null ? description : "Are you sure?");
        getBody().setMovementMethod(LinkMovementMethod.getInstance());
    }

    /**
     * Returns the root layout of this dialog.
     * Should only be called from within onClickHandlers or onViewBound as it will likely throw a {@link NullPointerException} in other cases
     */
    public final LinearLayout getRoot() { return binding.a; }

    /**
     * Returns the cancel button of this dialog.
     * Should only be called from within onClickHandlers or onViewBound as it will likely throw a {@link NullPointerException} in other cases
     * @see #setOnOkListener(View.OnClickListener)
     */
    public final MaterialButton getCancelButton() { return binding.b; }

    /**
     * Returns the OK button of this dialog.
     * Should only be called from within onClickHandlers or onViewBound as it will likely throw a {@link NullPointerException} in other cases
     * @see #setOnOkListener(View.OnClickListener)
     */
    public final LoadingButton getOKButton() { return binding.c; }

    /**
     * Returns the body of this dialog.
     * Should only be called from within onClickHandlers or onViewBound as it will likely throw a {@link NullPointerException} in other cases
     * @see #setDescription(CharSequence)
     */
    public final TextView getBody() { return binding.d; }

    /**
     * Returns the header of this dialog.
     * Should only be called from within onClickHandlers or onViewBound as it will likely throw a {@link NullPointerException} in other cases
     * @see #setTitle(CharSequence)
     */
    public final TextView getHeader() { return binding.e; }


    /**
     * Sets the title of this dialog
     * @param title The description
     * @return Builder for chaining
     */
    public ConfirmDialog setTitle(CharSequence title) {
        this.title = title;
        return this;
    }

    /**
     * Sets the description of this dialog
     * @param description The description
     * @return Builder for chaining
     */
    public ConfirmDialog setDescription(CharSequence description) {
        this.description = description;
        return this;
    }

    /**
     * Sets the {@link View.OnClickListener} that will be called when the OK button is pressed (By default simply closes this dialog)
     * @param listener The listener
     * @return Builder for chaining
     */
    public ConfirmDialog setOnOkListener(View.OnClickListener listener) {
        onOkListener = listener;
        return this;
    }

    /**
     * Sets the {@link View.OnClickListener} that will be called when the cancel button is pressed (By default simply closes this dialog)
     * @param listener The listener
     * @return Builder for chaining
     */
    public ConfirmDialog setOnCancelListener(View.OnClickListener listener) {
        onCancelListener = listener;
        return this;
    }

    /**
     * Indicates that this confirm dialog is for a dangerous action by making the OK button Red
     * @param isDangerous Whether this action is dangerous
     * @return Builder for chaining
     */
    public ConfirmDialog setIsDangerous(boolean isDangerous) {
        this.isDangerous = isDangerous;
        return this;
    }
}
