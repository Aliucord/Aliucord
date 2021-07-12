package com.discord.stores;

import android.content.Context;
import com.discord.app.AppActivity;
import com.discord.models.domain.ModelGuildFolder;
import java.util.Collection;
import java.util.List;
import rx.Observable;

@SuppressWarnings("unused")
public final class StoreUserSettings {
    public static final Companion Companion = new Companion();
    public static final class Companion {}

    public final void expireCustomStatus() {}

    public static Observable observeIsAnimatedEmojisEnabled$default(StoreUserSettings storeUserSettings, boolean z2, int i, Object obj) { return null; }
    public static Observable observeIsAutoPlayGifsEnabled$default(StoreUserSettings storeUserSettings, boolean z2, int i, Object obj) { return null; }
    public static Observable observeStickerAnimationSettings$default(StoreUserSettings storeUserSettings, boolean z2, int i, Object obj) { return null; }

    public final void updateAllowAccessibilityDetectionInternal(boolean z2) {}

    public final boolean getIsAttachmentMediaInline() { return false; }
    public final boolean getIsAutoImageCompressionEnabled() { return false; }
    public final boolean getIsAutoPlayGifsEnabled() { return false; }
    public final boolean getIsChromeCustomTabsEnabled() { return false; }
    public final boolean getIsDeveloperMode() { return false; }
    public final boolean getIsEmbedMediaInlined() { return false; }
    public final boolean getIsMobileOverlayEnabled() { return false; }
    public final boolean getIsRenderEmbedsEnabled() { return false; }
    public final boolean getIsShiftEnterToSendEnabled() { return false; }
    public final boolean getIsStickerSuggestionsEnabled() { return false; }
    public final boolean getIsSyncTextAndImagesEnabled() { return false; }
    public final int getStickerAnimationSettings() { return 0; }

    public void init(Context context) {}

    public final Observable<Integer> observeExplicitContentFilter() { return null; }
    public final Observable<Integer> observeFriendDiscoveryFlags() { return null; }
    public final Observable<List<ModelGuildFolder>> observeGuildFolders() { return null; }
    public final Observable<Boolean> observeIsAccessibilityDetectionAllowed() { return null; }
    public final Observable<Boolean> observeIsAnimatedEmojisEnabled(boolean z2) { return null; }
    public final Observable<Boolean> observeIsAutoPlayGifsEnabled(boolean z2) { return null; }
    public final Observable<Boolean> observeIsDefaultGuildsRestricted() { return null; }
    public final Observable<Boolean> observeIsRenderEmbedsEnabled() { return null; }
    public final Observable<Boolean> observeIsShowCurrentGameEnabled() { return null; }
    public final Observable<Boolean> observeIsStickerSuggestionsEnabled() { return null; }
    public final Observable<List<Long>> observeRestrictedGuildIds() { return null; }
    public final Observable<Integer> observeStickerAnimationSettings(boolean z2) { return null; }

    public final void setDefaultGuildsRestricted(AppActivity appActivity, boolean z2, Collection<Long> collection) {}
    public final void setExplicitContentFilter(AppActivity appActivity, int i) {}
    public final void setFriendDiscoveryFlags(AppActivity appActivity, int i) {}
    public final void setFriendSourceFlags(AppActivity appActivity, Boolean bool, Boolean bool2, Boolean bool3) {}
    public final void setIsAnimatedEmojisEnabled(AppActivity appActivity, boolean z2) {}
    public final void setIsAttachmentMediaInline(AppActivity appActivity, boolean z2) {}
    public final void setIsAutoImageCompressionEnabled(boolean z2) {}
    public final boolean setIsAutoPlayGifsEnabled(boolean z2) { return false; }
    public final void setIsChromeCustomTabsEnabled(boolean z2) {}
    public final void setIsDeveloperMode(AppActivity appActivity, boolean z2) {}
    public final void setIsEmbedMediaInlined(AppActivity appActivity, boolean z2) {}
    public final void setIsMobileOverlayEnabled(boolean z2) {}
    public final void setIsRenderEmbedsEnabled(AppActivity appActivity, boolean z2) {}
    public final void setIsShiftEnterToSendEnabled(boolean z2) {}
    public final void setIsShowCurrentGameEnabled(AppActivity appActivity, boolean z2) {}
    public final boolean setIsStickerSuggestionsEnabled(boolean z2) { return false; }
    public final void setRestrictedGuildId(AppActivity appActivity, long j, boolean z2) {}
    public final void setRestrictedGuildIds(AppActivity appActivity, Collection<Long> collection, long j, boolean z2) {}
    public final void setStickerAnimationSettings(AppActivity appActivity, int i) {}
    public final void getIsSyncTextAndImagesEnabled(boolean z2) {}
}
