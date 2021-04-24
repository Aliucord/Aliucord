package com.discord.widgets.user.usersheet;

import com.discord.models.user.User;

@SuppressWarnings("unused")
public final class WidgetUserSheetViewModel {
    public static abstract class ViewState {
        public static final class Loaded extends ViewState {
            public final User getUser() { return null; }
        }
    }
}
