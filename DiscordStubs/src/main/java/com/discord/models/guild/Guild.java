package com.discord.models.guild;

import com.discord.api.emoji.GuildEmoji;
import com.discord.api.guild.*;
import com.discord.api.role.GuildRole;
import com.discord.api.sticker.Sticker;

import java.util.Collections;
import java.util.List;
import java.util.Set;

@SuppressWarnings({"unused"})
public final class Guild {
    public Guild(com.discord.api.guild.Guild apiGuild) {}

    public final boolean canHaveBanner() { return false; }
    public final boolean canHaveSplash() { return false; }
    public final boolean canHaveVanityURL() { return false; }
    public final Long getAfkChannelId() { return null; }
    public final int getAfkTimeout() { return 0; }
    public final int getApproximatePresenceCount() { return 0; }
    public final String getBanner() { return null; }
    public final int getDefaultMessageNotifications() { return 0; }
    public final String getDescription() { return null; }
    public final List<GuildEmoji> getEmojis() { return Collections.emptyList(); }
    public final GuildExplicitContentFilter getExplicitContentFilter() { return null; }
    public final Set<GuildFeature> getFeatures() { return Collections.emptySet(); }
    public final String getIcon() { return null; }
    public final long getId() { return 0; }
    public final String getJoinedAt() { return ""; }
    public final int getMemberCount() { return 0; }
    public final String getName() { return ""; }
    public final boolean getNsfw() { return false; }
    public final long getOwnerId() { return 0; }
    public final String getPreferredLocale() { return null; }
    public final int getPremiumSubscriptionCount() { return 0; }
    public final int getPremiumTier() { return 0; }
    public final Long getPublicUpdatesChannelId() { return null; }
    public final List<GuildRole> getRoles() { return Collections.emptyList(); }
    public final Long getRulesChannelId() { return null; }
    public final String getShortName() { return null; }
    public final String getSplash() { return null; }
    public final List<Sticker> getStickers() { return Collections.emptyList(); }
    public final int getSystemChannelFlags() { return 0; }
    public final Long getSystemChannelId() { return null; }
    public final boolean getUnavailable() { return false; }
    public final String getVanityUrlCode() { return null; }
    public final GuildVerificationLevel getVerificationLevel() { return GuildVerificationLevel.NONE; }

    public final boolean hasFeature(GuildFeature feature) { return false; }
    public final boolean hasIcon() { return false; }
    public final boolean isOwner(long id) { return false; }
}
