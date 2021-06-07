package com.discord.stores;

import android.content.SharedPreferences;

@SuppressWarnings("unused")
public class StoreUserSettings {
    public SharedPreferences prefs = null;

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

    public final void setIsAutoImageCompressionEnabled(boolean z2) { }
    public final boolean setIsAutoPlayGifsEnabled(boolean z2) { return false; }
    public final void setIsChromeCustomTabsEnabled(boolean z2) { }
    public final void setIsMobileOverlayEnabled(boolean z2) { }
    public final void setIsShiftEnterToSendEnabled(boolean z2) { }
    public final boolean setIsStickerSuggestionsEnabled(boolean z2) { return false; }
}
