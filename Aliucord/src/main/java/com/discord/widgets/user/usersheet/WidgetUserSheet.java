package com.discord.widgets.user.usersheet;

import androidx.fragment.app.FragmentManager;

import com.discord.app.AppBottomSheet;

@SuppressWarnings("unused")
public final class WidgetUserSheet extends AppBottomSheet {
    public int getContentViewResId() { return 0; }

    public static final Companion Companion = new Companion();
    public static final class Companion {
        public final void show(long id, FragmentManager fragmentManager) {}
    }
}
