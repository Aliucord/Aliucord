package com.discord.widgets.user.usersheet;

import androidx.fragment.app.FragmentManager;

import com.discord.app.AppBottomSheet;
import com.discord.databinding.WidgetUserSheetBinding;

@SuppressWarnings("unused")
public final class WidgetUserSheet extends AppBottomSheet {
    public int getContentViewResId() { return 0; }

    public static WidgetUserSheetBinding access$getBinding$p(WidgetUserSheet instance) { return new WidgetUserSheetBinding(); }

    public static final Companion Companion = new Companion();
    public static final class Companion {
        public final void show(long id, FragmentManager fragmentManager) {}
    }
}
