package com.discord.models.user;

import com.discord.api.premium.PremiumTier;

@SuppressWarnings("unused")
public final class MeUser implements User {
    public String getAvatar() { return ""; }
    public String getBanner() { return null; }
    public String getBannerColor() { return null; }
    public String getBio() { return null; }
    public int getDiscriminator() { return 0; }
    public int getFlags() { return 0; }
    public long getId() { return 0; }
    public PremiumTier getPremiumTier() { return null; }
    public int getPublicFlags() { return 0; }
    public String getUsername() { return ""; }
    public boolean isBot() { return false; }
    public boolean isSystemUser() { return false; }

    public boolean isVerified() { return false; }
}
