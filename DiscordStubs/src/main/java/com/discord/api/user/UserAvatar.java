package com.discord.api.user;

import kotlin.jvm.internal.DefaultConstructorMarker;

@SuppressWarnings("unused")
public abstract class UserAvatar {
    public static final class Avatar extends UserAvatar {
        public Avatar(String url) {
            super(url, null);
        }
    }

    public static final class NoAvatar extends UserAvatar {
        public static final NoAvatar INSTANCE = new NoAvatar();

        public NoAvatar() {
            super(null, null);
        }
    }

    public UserAvatar(String url, DefaultConstructorMarker defaultConstructorMarker) {}

    /** getUrl */
    public final String a() { return ""; }
}
