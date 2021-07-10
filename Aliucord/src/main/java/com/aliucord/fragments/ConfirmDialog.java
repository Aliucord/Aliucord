/*
 * Copyright (c) 2021 Juby210 & Vendicated
 * Licensed under the Open Software License version 3.0
 */

package com.aliucord.fragments;

import android.view.View;
import android.widget.TextView;

import com.aliucord.Utils;
import com.discord.app.AppDialog;
import com.discord.databinding.LeaveGuildDialogBinding;
import com.discord.views.LoadingButton;
import com.discord.widgets.guilds.leave.WidgetLeaveGuildDialog$binding$2;
import com.google.android.material.button.MaterialButton;

@SuppressWarnings("unused")
public class ConfirmDialog extends AppDialog {
    public ConfirmDialog() {
        super(Utils.getResId("leave_guild_dialog", "layout"));
    }

    private LeaveGuildDialogBinding binding;

    @Override
    public void onViewBound(View view) {
        super.onViewBound(view);

        binding = WidgetLeaveGuildDialog$binding$2.INSTANCE.invoke(view);
        LoadingButton okButton = getOKButton();
        okButton.setText("OK");
        okButton.setIsLoading(false);
        okButton.setOnClickListener(e -> dismiss());
    }

    public final MaterialButton getCancelButton() { return binding.b; }
    public final LoadingButton getOKButton() { return binding.c; }
    public final TextView getBody() { return binding.d; }
    public final TextView getHeader() { return binding.e; }
}
