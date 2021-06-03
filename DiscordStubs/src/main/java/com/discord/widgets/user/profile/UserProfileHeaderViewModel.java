package com.discord.widgets.user.profile;

import com.discord.models.user.User;

@SuppressWarnings("unused")
public final class UserProfileHeaderViewModel {
    public static abstract class ViewState {
        public static final class Loaded extends ViewState {
            public final User getUser() { return null; }
            public final String getUserNickname() { return null; }
            public final boolean isMeUserPremium() { return false; }
        }

        public static final class Uninitialized extends ViewState {
            public static final Uninitialized INSTANCE = new Uninitialized();
        }
    }
}
