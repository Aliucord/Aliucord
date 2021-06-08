package com.discord.api.guild;

import androidx.constraintlayout.solver.widgets.analyzer.BasicMeasure;
import com.discord.api.channel.Channel;
import com.discord.api.emoji.GuildEmoji;
import com.discord.api.guild.welcome.GuildWelcomeScreen;
import com.discord.api.guildhash.GuildHashes;
import com.discord.api.guildmember.GuildMember;
import com.discord.api.presence.Presence;
import com.discord.api.role.GuildRole;
import com.discord.api.stageinstance.StageInstance;
import com.discord.api.voice.state.VoiceState;
import com.discord.models.domain.ModelAuditLogEntry;
import java.util.List;
import java.util.Objects;

/* compiled from: Guild.kt */
public final class Guild {
    public Guild(
            List<GuildRole> roles,
            List<GuildEmoji> emojis,
            String name,
            String description,
            Integer defaultMessageNotifications,
            long id,
            String region,
            long ownerId,
            String icon,
            GuildVerificationLevel guildVerificationLevel,
            GuildExplicitContentFilter guildExplicitContentFilter,
            List<Presence> presences,
            List<Channel> channels,
            List<GuildMember> members,
            List<VoiceState> voiceStates,
            boolean unavailable,
            int mfaLevel,
            int afkTimeout,
            Long afkChannelId,
            Long systemChannelId,
            List<? extends GuildFeature> features,
            int memberCount,
            String banner,
            String splash,
            int premiumTier,
            int premiumSubscriptionCount,
            int systemChannelFlags,
            String joinedAt,
            Long rulesChannelId,
            Long publicUpdatesChannelId,
            String preferredLocale,
            GuildWelcomeScreen guildWelcomeScreen,
            GuildMaxVideoChannelUsers guildMaxVideoChannelUsers,
            String vanityUrlCode,
            int approximateMemberCount,
            int approximatePresenceCount,
            Integer permissions,
            GuildHashes guildHashes,
            List<Channel> channelUpdates,
            List<Channel> threads,
            boolean nsfw,
            List<StageInstance> stageInstances) { }

    /** Maybe another time
    public static Guild a(Guild guild, List list, List list2, String str, String str2, Integer num, long j, String str3, long j2, String str4, GuildVerificationLevel guildVerificationLevel, GuildExplicitContentFilter guildExplicitContentFilter, List list3, List list4, List list5, List list6, boolean z2, int i, int i2, Long l, Long l2, List list7, int i3, String str5, String str6, int i4, int i5, int i6, String str7, Long l3, Long l4, String str8, GuildWelcomeScreen guildWelcomeScreen, GuildMaxVideoChannelUsers guildMaxVideoChannelUsers, String str9, int i7, int i8, Integer num2, GuildHashes guildHashes2, List list8, List list9, boolean z3, List list10, int i9, int i10) {
        List list11 = (i9 & 1) != 0 ? guild.roles : list;
        List list12 = (i9 & 2) != 0 ? guild.emojis : list2;
        String str10 = (i9 & 4) != 0 ? guild.name : str;
        String str11 = (i9 & 8) != 0 ? guild.description : str2;
        Integer num3 = (i9 & 16) != 0 ? guild.defaultMessageNotifications : num;
        long j3 = (i9 & 32) != 0 ? guild.f1566id : j;
        String str12 = (i9 & 64) != 0 ? guild.region : str3;
        long j4 = (i9 & 128) != 0 ? guild.ownerId : j2;
        String str13 = (i9 & 256) != 0 ? guild.icon : str4;
        GuildVerificationLevel guildVerificationLevel2 = (i9 & 512) != 0 ? guild.verificationLevel : guildVerificationLevel;
        GuildExplicitContentFilter guildExplicitContentFilter2 = (i9 & 1024) != 0 ? guild.explicitContentFilter : guildExplicitContentFilter;
        List list13 = (i9 & 2048) != 0 ? guild.presences : list3;
        List list14 = (i9 & 4096) != 0 ? guild.channels : list4;
        List list15 = (i9 & 8192) != 0 ? guild.members : list5;
        List<VoiceState> list16 = (i9 & 16384) != 0 ? guild.voiceStates : null;
        boolean z4 = (i9 & 32768) != 0 ? guild.unavailable : z2;
        int i11 = (i9 & 65536) != 0 ? guild.mfaLevel : i;
        int i12 = (i9 & 131072) != 0 ? guild.afkTimeout : i2;
        Long l5 = (i9 & 262144) != 0 ? guild.afkChannelId : l;
        Long l6 = (i9 & 524288) != 0 ? guild.systemChannelId : l2;
        List list17 = (i9 & 1048576) != 0 ? guild.features : list7;
        int i13 = (i9 & 2097152) != 0 ? guild.memberCount : i3;
        String str14 = (i9 & 4194304) != 0 ? guild.banner : str5;
        String str15 = (i9 & 8388608) != 0 ? guild.splash : str6;
        int i14 = (i9 & 16777216) != 0 ? guild.premiumTier : i4;
        int i15 = (i9 & 33554432) != 0 ? guild.premiumSubscriptionCount : i5;
        int i16 = (i9 & 67108864) != 0 ? guild.systemChannelFlags : i6;
        String str16 = (i9 & 134217728) != 0 ? guild.joinedAt : null;
        Long l7 = (i9 & 268435456) != 0 ? guild.rulesChannelId : l3;
        Long l8 = (i9 & 536870912) != 0 ? guild.publicUpdatesChannelId : l4;
        String str17 = (i9 & BasicMeasure.EXACTLY) != 0 ? guild.preferredLocale : str8;
        GuildWelcomeScreen guildWelcomeScreen2 = (i9 & Integer.MIN_VALUE) != 0 ? guild.welcomeScreen : null;
        GuildMaxVideoChannelUsers guildMaxVideoChannelUsers2 = (i10 & 1) != 0 ? guild.maxVideoChannelUsers : guildMaxVideoChannelUsers;
        String str18 = (i10 & 2) != 0 ? guild.vanityUrlCode : str9;
        int i17 = (i10 & 4) != 0 ? guild.approximateMemberCount : i7;
        int i18 = (i10 & 8) != 0 ? guild.approximatePresenceCount : i8;
        Integer num4 = (i10 & 16) != 0 ? guild.permissions : null;
        GuildHashes guildHashes3 = (i10 & 32) != 0 ? guild.guildHashes : null;
        List<Channel> list18 = (i10 & 64) != 0 ? guild.channelUpdates : null;
        List<Channel> list19 = (i10 & 128) != 0 ? guild.threads : null;
        boolean z5 = (i10 & 256) != 0 ? guild.nsfw : z3;
        List<StageInstance> list20 = (i10 & 512) != 0 ? guild.stageInstances : null;
        Objects.requireNonNull(guild);
        m.checkNotNullParameter(str10, ModelAuditLogEntry.CHANGE_KEY_NAME);
        m.checkNotNullParameter(list17, "features");
        return new Guild(list11, list12, str10, str11, num3, j3, str12, j4, str13, guildVerificationLevel2, guildExplicitContentFilter2, list13, list14, list15, list16, z4, i11, i12, l5, l6, list17, i13, str14, str15, i14, i15, i16, str16, l7, l8, str17, guildWelcomeScreen2, guildMaxVideoChannelUsers2, str18, i17, i18, num4, guildHashes3, list18, list19, z5, list20);
    }
    */

    /** getPresences */
    public final List<Presence> A() { return null; }
    /** getPublicUpdatesChannelId */
    public final Long B() { return null; }
    /** getRegion */
    public final String C() { return null; }
    /** getRoles */
    public final List<GuildRole> D() { return null; }
    /** getRulesChannelId */
    public final Long E() { return null; }
    /** getSplash */
    public final String F() { return null; }
    /** getStageInstances */
    public final List<StageInstance> G() { return null; }
    /** getSystemChannelFlags */
    public final int H() { return 0; }
    /** getSystemChannelId */
    public final Long I() { return null; }
    /** getThreads */
    public final List<Channel> J() { return null; }
    /** getIsUnavailable */
    public final boolean K() { return false; }
    /** getVanityUrlCode */
    public final String L() { return null; }
    /** getGuildVerificationLevel */
    public final GuildVerificationLevel M() { return null; }
    /** getVoiceStates */
    public final List<VoiceState> N() { return null; }
    /** getWelcomeScreen */
    public final GuildWelcomeScreen O() { return null; }
    /** getAfkChannelId */
    public final Long b() { return null; }
    /** getAfkTimeoput */
    public final int c() { return 0; }
    /** getApproxPresenceCount */
    public final int d() { return 0; }
    /** getBanner */
    public final String e() { return null; }
    /** getChannelUpdates */
    public final List<Channel> f() { return null; }
    /** getChannels */
    public final List<Channel> g() { return null; }
    /** getDefaultMessageNotifications */
    public final Integer h() { return null; }
    /** getDescription */
    public final String i() { return null; }
    /** getEmojis */
    public final List<GuildEmoji> j() { return null; }
    /** getExplicitContentFilter */
    public final GuildExplicitContentFilter k() { return null; }
    /** getFeatures */
    public final List<GuildFeature> l() { return null; }

    /*
    public final GuildHashes m() {
        return this.guildHashes;
    }
    */

    /** getIcon */
    public final String n() { return null; }

    /** getId */
    public final long o() { return 0; }
    /** getJoinedAt */
    public final String p() { return null; }

    /*
    public final GuildMaxVideoChannelUsers q() {
        return this.maxVideoChannelUsers;
    }
    */
    /** getApproxMemberCount */
    public final int r() { return 0; }
    /** getCachedMembers */
    public final List<GuildMember> s() { return null; }
    /** getMfaLevel */
    public final int t() { return 0; }
    /** getName */
    public final String u() { return null; }
    /** getIsNsfw */
    public final boolean v() { return false; }
    /** getOwnerId */
    public final long w() { return 0; }
    /** getPreferredLocale */
    public final String x() { return null; }
    /** getPremiumSubscriptionCount */
    public final int y() { return 0; }
    /** getPremiumTier */
    public final int z() { return 0; }
}
