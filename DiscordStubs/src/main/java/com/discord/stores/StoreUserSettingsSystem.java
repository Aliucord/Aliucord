package com.discord.stores;

import android.content.SharedPreferences;

@SuppressWarnings("unused")
public class StoreUserSettingsSystem {
    public SharedPreferences prefs = null;
    
    public String getTheme() { return ""; }
    public final int getFontScale() { return 0; }
    public final String getLocale() { return null; }

    public final void setFontScale(int i) { }
}
