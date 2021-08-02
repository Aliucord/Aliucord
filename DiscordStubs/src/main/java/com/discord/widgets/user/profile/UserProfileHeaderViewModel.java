package com.discord.widgets.user.profile;

import com.discord.models.domain.ModelUserProfile;
import com.discord.models.member.GuildMember;
import com.discord.models.user.User;

@SuppressWarnings("unused")
public final class UserProfileHeaderViewModel {
    public static abstract class ViewState {
        public static final class Loaded extends ViewState {
            public final boolean getAllowAnimatedEmojis() { return false; }
            public final boolean getAllowAnimationInReducedMotion() { return false; }
            public final String getAvatarColorId() { return ""; }
            public final String getBanner() { return ""; }
            public final String getBannerColorHex() { return ""; }
            public final boolean getEditable() { return false; }
            public final GuildMember getGuildMember() { return getGuildMember(); }
            public final String getGuildMemberColorId() { return ""; }
            public final boolean getHasGuildMemberAvatar() { return false; }
            public final boolean getHasNickname() { return false; }
            public final boolean getReducedMotionEnabled() { return false; }
            public final boolean getShouldAnimateBanner() { return false; }
            public final boolean getShouldShowGIFTag() { return false; }
            public final boolean getShowMediumAvatar() { return false; }
            public final boolean getShowPresence() { return false; }
            public final boolean getShowSmallAvatar() { return false; }
            public final User getUser() { return getUser(); }
            public final ModelUserProfile getUserProfile() { return getUserProfile(); }
            public final String getUserNickname() { return ""; }
            public final boolean isMeUserPremium() { return false; }
            public final boolean isMeUserVerified() { return false; }
            public final boolean isProfileLoaded() { return false; }
        }

        public static final class Uninitialized extends ViewState {
            public static final Uninitialized INSTANCE = new Uninitialized();
        }
    }
}
