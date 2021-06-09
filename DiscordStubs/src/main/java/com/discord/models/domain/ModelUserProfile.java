package com.discord.models.domain;

import android.content.Context;

import com.discord.api.user.User;

import java.util.List;
import java.util.Map;

public class ModelUserProfile implements Model {
    public static final class GuildReference implements Model {
        public long getGuildId() { return 0; }
        public String getNick() { return null; }
    }

    public ModelUserProfile() { }
    public static ModelUserProfile createForTesting(List<com.discord.models.domain.ModelConnectedAccount> list) { return null; }
    public List<com.discord.models.domain.ModelConnectedAccount> getConnectedAccounts() { return null; }
    public Map<Long, GuildReference> getMutualGuilds() { return null; }
    public Integer getPremiumGuildMonthsSubscribed() { return null; }
    public String getPremiumGuildSince() { return null; }
    public String getPremiumSince() { return null; }
    public User getUser() { return null; }
    public boolean isPremiumGuildSubscriber() { return false; }
    public String getPremiumGuildSince(Context context) { return null; }
    public String getPremiumSince(Context context) { return null; }
}
