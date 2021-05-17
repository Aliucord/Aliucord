package com.discord.widgets.settings;

import android.view.View;

import com.discord.app.AppFragment;
import com.discord.databinding.WidgetSettingsBinding;

public final class WidgetSettings extends AppFragment {
    @Override
    public void onViewBound(View view) { super.onViewBound(view); }

    private WidgetSettingsBinding getBinding() { return new WidgetSettingsBinding(); }
}
