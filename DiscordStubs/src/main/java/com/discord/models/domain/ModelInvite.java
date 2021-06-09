package com.discord.models.domain;

import android.content.res.Resources;
import com.discord.api.channel.Channel;
import com.discord.api.guild.Guild;
import com.discord.api.user.User;
import java.io.Serializable;

public class ModelInvite implements Serializable {
    public static final String URL_FORMAT = "%1$s/%2$s";
    public static class Settings {
        public static final int HALF_HOUR = 1800;
        public static final int[] MAX_USES_ARRAY = {0, 1, 10, 100};
        public static final int NEVER = 0;
        public static final int ONE_DAY = 86400;
        public static final int ONE_HOUR = 3600;
        public static final int SEVEN_DAYS = 604800;
        public static final int SIX_HOURS = 21600;
        public static final int TWELVE_HOURS = 43200;
        public static final int[] EXPIRES_AFTER_ARRAY = {0, SEVEN_DAYS, ONE_DAY, TWELVE_HOURS, SIX_HOURS, ONE_HOUR, HALF_HOUR};
        public Settings(int maxAge, int maxUses, boolean temporary) { }
        public Settings(int maxAge) { }
        public int getMaxAge() { return 0; }
        public int getMaxUses() { return 0; }
        public boolean isTemporary() { return false; }
        public Settings mergeMaxAge(int i) { return null; }
        public Settings mergeMaxUses(int i) { return null; }
        public Settings mergeTemporary(boolean z2) { return null; }
    }
    private ModelInvite(Channel channel2, String str, Guild guild2) { }
    public static ModelInvite createForStaticUrl(String str, Guild guild2) { return null; }
    public static ModelInvite createForTesting(Channel channel2) { return null; }
    public int getApproximateMemberCount() { return 0; }
    public int getApproximatePresenceCount() { return 0; }
    public Channel getChannel() { return null; }
    public String getCode() { return null; }
    public long getCreatedAt() { return 0; }
    public long getExpirationTime() { return 0; }
    public Guild getGuild() { return null; }
    public User getInviter() { return null; }
    public int getMaxAge() { return 0; }
    public int getMaxUses() { return 0; }
    // public ModelMemberVerificationForm getMemberVerificationForm() { return null; }
    public long getTimeToExpirationMillis() { return 0; }
    public int getUses() { return 0; }
    public boolean isNewMember() { return false; }
    public boolean isRevoked() { return false; }
    public boolean isStatic() { return false; }
    public boolean isTemporary() { return false; }
    public String toLink(Resources resources, String str) { return null; }
}
