package com.aliucord.fragments;

import android.annotation.SuppressLint;
import android.view.View;

public class MissingPatchesDialog extends ConfirmDialog {
    @Override
    @SuppressLint("SetTextI18n")
    public void onViewBound(View view) {
        super.onViewBound(view);

        getHeader().setText("Missing patches");
        getBody().setText("Detected possibly missing patches, you should reinstall Aliucord using Installer to get all plugins working.");
        getCancelButton().setVisibility(View.GONE);
    }
}
