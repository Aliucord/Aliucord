package com.discord.api.user;

import com.discord.api.guildmember.GuildMember;
import com.discord.api.premium.PremiumTier;
import com.discord.nullserializable.NullSerializable;

@SuppressWarnings("unused")
public final class User {
    public User(
            long id,
            String username,
            NullSerializable<String> avatar,
            NullSerializable<String> banner,
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
            GuildMember guildMember,
            String bio,
            String bannerColor
    ) {}

    /** getAvatar */
    public final NullSerializable<String> a() { return null; }
    /** getBanner */
    public final NullSerializable<String> b() { return null; }
    /** getBannerColor */
    public final String c() { return null; }
    /** getBio */
    public final String d() { return null; }
    /** getBot */
    public final Boolean e() { return false; }
    /** getDiscriminator */
    public final String f() { return ""; }
    /** getFlags */
    public final Integer h() { return 0; }
    /** getId */
    public final long i() { return 0; }
    /** getMember */
    public final GuildMember j() { return null; }
    /** getPublicFlags */
    public final Integer o() { return 0; }
    /** getSystem */
    public final Boolean p() { return false; }
    /** getUsername */
    public final String r() { return ""; }
    /** getVerified */
    public final Boolean s() { return null; }
}
