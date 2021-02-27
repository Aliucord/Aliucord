package com.discord.api.user;

import com.discord.api.guildmember.GuildMember;
import com.discord.api.premium.PremiumTier;

@SuppressWarnings("unused")
public final class User {
    public User(
            long id,
            String username,
            String avatar,
            String discriminator,
            Integer publicFlags,
            Integer flags,
            Boolean bot,
            Boolean system,
            String token,
            String email,
            Boolean verified,
            String locale,
            NsfwAllowance nsfwAllowance,
            Boolean mfa,
            Phone phone,
            String str,
            PremiumTier premiumTier,
            Integer approximateGuildCount,
            GuildMember guildMember
    ) {}

    // getId
    public final long f() { return 0; }
}
