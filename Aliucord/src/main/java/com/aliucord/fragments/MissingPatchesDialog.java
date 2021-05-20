package com.aliucord.fragments;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.view.View;

import com.google.android.material.button.MaterialButton;

public final class MissingPatchesDialog extends ConfirmDialog {
    @Override
    @SuppressLint("SetTextI18n")
    public void onViewBound(View view) {
        super.onViewBound(view);

        getHeader().setText("Missing patches");
        getBody().setText("Detected possibly missing patches, you should reinstall Aliucord using Installer to get all plugins working.");
        MaterialButton btn = getCancelButton();
        btn.setText("Open Installer");
        btn.setOnClickListener(e -> {
            Context context = e.getContext();
            Intent intent = context.getPackageManager().getLaunchIntentForPackage("com.aliucord.installer");
            if (intent != null) context.startActivity(intent);
        });
    }
}
