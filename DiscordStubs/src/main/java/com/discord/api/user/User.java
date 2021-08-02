package com.discord.api.user;

import com.discord.api.guildmember.GuildMember;
import com.discord.api.premium.PremiumTier;
import com.discord.nullserializable.NullSerializable;

/**
 * Obfuscated class with regularly changing method names.
 * Do not use this directly, use {@link com.discord.models.user.CoreUser} instead.
 * Use {@link com.discord.utilities.user.UserUtils#synthesizeApiUser(com.discord.models.user.User)} to convert back to api user.
 */
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
        NullSerializable<String> bio,
        NullSerializable<String> bannerColor
    ) {}

    /**
     * getAvatar
     * @deprecated Do not use this directly, use {@link com.discord.models.user.CoreUser} instead.
     */
    @Deprecated
    public final NullSerializable<String> a() { return null; }
    /**
     * getBanner
     * @deprecated Do not use this directly, use {@link com.discord.models.user.CoreUser} instead.
     */
    @Deprecated
    public final NullSerializable<String> b() { return null; }
    /**
     * getBannerColor
     * @deprecated Do not use this directly, use {@link com.discord.models.user.CoreUser} instead.
     */
    @Deprecated
    public final NullSerializable<String> c() { return null; }
    /**
     * getBio
     * @deprecated Do not use this directly, use {@link com.discord.models.user.CoreUser} instead.
     */
    @Deprecated
    public final NullSerializable<String> d() { return null; }
    /**
     * getBot
     * @deprecated Do not use this directly, use {@link com.discord.models.user.CoreUser} instead.
     */
    @Deprecated
    public final Boolean e() { return false; }
    /**
     * getDiscriminator
     * @deprecated Do not use this directly, use {@link com.discord.models.user.CoreUser} instead.
     */
    @Deprecated
    public final String f() { return ""; }
    /**
     * getFlags
     * @deprecated Do not use this directly, use {@link com.discord.models.user.CoreUser} instead.
     */
    @Deprecated
    public final Integer h() { return 0; }
    /**
     * getId
     * @deprecated Do not use this directly, use {@link com.discord.models.user.CoreUser} instead.
     */
    @Deprecated
    public final long i() { return 0; }
    /**
     * getMember
     * CoreUser doesn't have representation of this method, however if possible, do not rely on this. Its name might change randomly in a future discord updates.
     */
    public final GuildMember j() { return null; }
    /**
     * getPublicFlags
     * @deprecated Do not use this directly, use {@link com.discord.models.user.CoreUser} instead.
     */
    @Deprecated
    public final Integer o() { return 0; }
    /**
     * getSystem
     * @deprecated Do not use this directly, use {@link com.discord.models.user.CoreUser} instead.
     */
    @Deprecated
    public final Boolean p() { return false; }
    /**
     * getUsername
     * @deprecated Do not use this directly, use {@link com.discord.models.user.CoreUser} instead.
     */
    @Deprecated
    public final String r() { return ""; }
    /**
     * getVerified
     * @deprecated Do not use this directly, use {@link com.discord.models.user.CoreUser} instead.
     */
    @Deprecated
    public final Boolean s() { return null; }
}
