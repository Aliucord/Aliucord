package com.discord.api.user;

import com.discord.api.guildmember.GuildMember;
import com.discord.api.premium.PremiumTier;

@SuppressWarnings("unused")
public final class User {
    public User(
            long id,
            String username,
            UserAvatar avatar,
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

    /** getAvatar */
    public final UserAvatar a() { return null; }
    /** getBot */
    public final Boolean b() { return false; }
    /** getDiscriminator */
    public final String c() { return ""; }
    /** getFlags */
    public final Integer e() { return 0; }
    /** getId */
    public final long f() { return 0; }
    /** getMember */
    public final GuildMember g() { return null; }
    /** getPublicFlags */
    public final Integer l() { return 0; }
    /** getSystem */
    public final Boolean m() { return false; }
    /** getUsername */
    public final String o() { return ""; }
    /** getVerified */
    public final Boolean p() { return null; }
}
