package com.discord.utilities.icon;
import com.discord.api.commands.Application;
import com.discord.models.guild.Guild;
import com.discord.models.member.GuildMember;
import com.discord.models.user.User;

@SuppressWarnings("unused")
public class IconUtils {
    public static final String ANIMATED_IMAGE_EXTENSION = "gif";
    public static final String DEFAULT_ICON = "asset://asset/images/default_icon.jpg";
    public static final String DEFAULT_ICON_BLURPLE = "asset://asset/images/default_icon_selected.jpg";
    public static final IconUtils INSTANCE = new IconUtils();

    public static String getApplicationIcon(Application application, boolean z2) { return null; }
    public static String getApplicationIcon(long id, String imageId, int size) { return null; }

    public final String getForGuildMember(GuildMember guildMember, Integer size, boolean animated) { return null; }
    public final String getForGuildMemberOrUser(User user, GuildMember guildMember, Integer size) { return ""; }
    public static String getForUser(User user) { return ""; }
    public static String getForUser(Long id, String iconHash, Integer discriminator, boolean animated, Integer size) { return ""; }
    public String getForUserBanner(long id, String hash, Integer size, boolean animated) { return null; }

    public final String getDefaultForGroupDM(long id) { return ""; }
    public static String getForChannel(long id, String hash, int channelType, boolean animated, Integer size) { return null; }

    public static String getForGuild(Guild guild) { return null; }
    public static String getForGuild(Long guildId, String iconHash, String defaultIcon, boolean animated, Integer size) { return null; }
    public final String getBannerForGuild(Guild guild, Integer size) { return ""; }
    public final String getBannerForGuild(Long id, String bannerHash, Integer size) { return null; }
    public final String getGuildSplashUrl(long id, String splashHash, Integer size) { return ""; }
    public final String getGiftSplashUrl(long id, String hash, Integer size) { return null; }

    public final boolean isImageHashAnimated(String hash) { return false; }
    public final String getImageExtension(String hash, boolean animated) { return ""; }
    public final int getVoiceRegionIconResourceId(String region) { return 0; }
}
