package com.discord.models.domain;

import java.util.List;

@SuppressWarnings("unused")
public class ModelAuditLogEntry implements Model {
    public static final int ACTION_ALL = 0;
    public static final int ACTION_BOT_ADD = 28;
    public static final int ACTION_CHANNEL_CREATE = 10;
    public static final int ACTION_CHANNEL_DELETE = 12;
    public static final int ACTION_CHANNEL_OVERWRITE_CREATE = 13;
    public static final int ACTION_CHANNEL_OVERWRITE_DELETE = 15;
    public static final int ACTION_CHANNEL_OVERWRITE_UPDATE = 14;
    public static final int ACTION_CHANNEL_UPDATE = 11;
    public static final int ACTION_EMOJI_CREATE = 60;
    public static final int ACTION_EMOJI_DELETE = 62;
    public static final int ACTION_EMOJI_UPDATE = 61;
    public static final int ACTION_GUILD_UPDATE = 1;
    public static final int ACTION_INTEGRATION_CREATE = 80;
    public static final int ACTION_INTEGRATION_DELETE = 82;
    public static final int ACTION_INTEGRATION_UPDATE = 81;
    public static final int ACTION_INVITE_CREATE = 40;
    public static final int ACTION_INVITE_DELETE = 42;
    public static final int ACTION_INVITE_UPDATE = 41;
    public static final int ACTION_MEMBER_BAN_ADD = 22;
    public static final int ACTION_MEMBER_BAN_REMOVE = 23;
    public static final int ACTION_MEMBER_DISCONNECT = 27;
    public static final int ACTION_MEMBER_KICK = 20;
    public static final int ACTION_MEMBER_MOVE = 26;
    public static final int ACTION_MEMBER_PRUNE = 21;
    public static final int ACTION_MEMBER_ROLE_UPDATE = 25;
    public static final int ACTION_MEMBER_UPDATE = 24;
    public static final int ACTION_MESSAGE_BULK_DELETE = 73;
    public static final int ACTION_MESSAGE_DELETE = 72;
    public static final int ACTION_MESSAGE_PIN = 74;
    public static final int ACTION_MESSAGE_UNPIN = 75;
    public static final int ACTION_ROLE_CREATE = 30;
    public static final int ACTION_ROLE_DELETE = 32;
    public static final int ACTION_ROLE_UPDATE = 31;
    public static final int ACTION_STAGE_INSTANCE_CREATE = 83;
    public static final int ACTION_STAGE_INSTANCE_DELETE = 85;
    public static final int ACTION_STAGE_INSTANCE_UPDATE = 84;
    public static final int ACTION_WEBHOOK_CREATE = 50;
    public static final int ACTION_WEBHOOK_DELETE = 52;
    public static final int ACTION_WEBHOOK_UPDATE = 51;
    public static final String CHANGE_KEY_AFK_CHANNEL_ID = "afk_channel_id";
    public static final String CHANGE_KEY_AFK_TIMEOUT = "afk_timeout";
    public static final String CHANGE_KEY_APPLICATION_ID = "application_id";
    public static final String CHANGE_KEY_AVATAR_HASH = "avatar_hash";
    public static final String CHANGE_KEY_BANNER_HASH = "banner_hash";
    public static final String CHANGE_KEY_BITRATE = "bitrate";
    public static final String CHANGE_KEY_CHANNEL_ID = "channel_id";
    public static final String CHANGE_KEY_CODE = "code";
    public static final String CHANGE_KEY_COLOR = "color";
    public static final String CHANGE_KEY_DEAF = "deaf";
    public static final String CHANGE_KEY_DEFAULT_MESSAGE_NOTIFICATIONS = "default_message_notifications";
    public static final String CHANGE_KEY_DESCRIPTION = "description";
    public static final String CHANGE_KEY_DISCOVERY_SPLASH_HASH = "discovery_splash_hash";
    public static final String CHANGE_KEY_ENABLE_EMOTICONS = "enable_emoticons";
    public static final String CHANGE_KEY_EXPIRE_BEHAVIOR = "expire_behavior";
    public static final String CHANGE_KEY_EXPIRE_GRACE_PERIOD = "expire_grace_period";
    public static final String CHANGE_KEY_EXPLICIT_CONTENT_FILTER = "explicit_content_filter";
    public static final String CHANGE_KEY_HOIST = "hoist";
    public static final String CHANGE_KEY_ICON_HASH = "icon_hash";
    public static final String CHANGE_KEY_ID = "id";
    public static final String CHANGE_KEY_INVITER_ID = "inviter_id";
    public static final String CHANGE_KEY_MAX_AGE = "max_age";
    public static final String CHANGE_KEY_MAX_USES = "max_uses";
    public static final String CHANGE_KEY_MENTIONABLE = "mentionable";
    public static final String CHANGE_KEY_MFA_LEVEL = "mfa_level";
    public static final String CHANGE_KEY_MUTE = "mute";
    public static final String CHANGE_KEY_NAME = "name";
    public static final String CHANGE_KEY_NICK = "nick";
    public static final String CHANGE_KEY_NSFW = "nsfw";
    public static final String CHANGE_KEY_OWNER_ID = "owner_id";
    public static final String CHANGE_KEY_PERMISSIONS = "permissions";
    public static final String CHANGE_KEY_PERMISSIONS_DENIED = "deny";
    public static final String CHANGE_KEY_PERMISSIONS_GRANTED = "allow";
    public static final String CHANGE_KEY_PERMISSIONS_RESET = "reset";
    public static final String CHANGE_KEY_PERMISSION_OVERWRITES = "permission_overwrites";
    public static final String CHANGE_KEY_POSITION = "position";
    public static final String CHANGE_KEY_PREFERRED_LOCALE = "preferred_locale";
    public static final String CHANGE_KEY_PRIVACY_LEVEL = "privacy_level";
    public static final String CHANGE_KEY_PRUNE_DELETE_DAYS = "prune_delete_days";
    public static final String CHANGE_KEY_RATE_LIMIT_PER_USER = "rate_limit_per_user";
    public static final String CHANGE_KEY_REASON = "reason";
    public static final String CHANGE_KEY_REGION = "region";
    public static final String CHANGE_KEY_REGION_OVERRIDE = "rtc_region";
    public static final String CHANGE_KEY_ROLES_ADD = "$add";
    public static final String CHANGE_KEY_ROLES_REMOVE = "$remove";
    public static final String CHANGE_KEY_RULES_CHANNEL_ID = "rules_channel_id";
    public static final String CHANGE_KEY_SPLASH_HASH = "splash_hash";
    public static final String CHANGE_KEY_SYSTEM_CHANNEL_ID = "system_channel_id";
    public static final String CHANGE_KEY_TEMPORARY = "temporary";
    public static final String CHANGE_KEY_TOPIC = "topic";
    public static final String CHANGE_KEY_TYPE = "type";
    public static final String CHANGE_KEY_UPDATES_CHANNEL_ID = "public_updates_channel_id";
    public static final String CHANGE_KEY_USES = "uses";
    public static final String CHANGE_KEY_VANITY_URL_CODE = "vanity_url_code";
    public static final String CHANGE_KEY_VERIFICATION_LEVEL = "verification_level";
    public static final String CHANGE_KEY_VIDEO_QUALITY_MODE = "video_quality_mode";
    public static final String CHANGE_KEY_WIDGET_CHANNEL_ID = "widget_channel_id";
    public static final String CHANGE_KEY_WIDGET_ENABLED = "widget_enabled";
    public enum ActionType {
        ALL,
        CREATE,
        UPDATE,
        DELETE
    }

    public static class Change implements Model {
        public Change() { }
        public Change(String key, Object oldValue, Object newValue) { }
        public String getKey() { return null; }
        public Object getNewValue() { return null; }
        public Object getOldValue() { return null; }
        public Object getValue() { return null; }
    }

    public static class ChangeNameId implements Model {
        public long getId() { return 0; }
        public String getName() { return null; }
    }

    public class Options implements Model {
        public Options() { }
        public long getChannelId() { return 0; }
        public int getCount() { return 0; }
        public int getDeleteMemberDays() { return 0; }
        public long getId() { return 0; }
        public int getMembersRemoved() { return 0; }
        public String getRoleName() { return null; }
        public int getType() { return 0; }
    }

    public enum TargetType {
        ALL,
        UNKNOWN,
        GUILD,
        CHANNEL,
        CHANNEL_OVERWRITE,
        USER,
        ROLE,
        INVITE,
        WEBHOOK,
        EMOJI,
        INTEGRATION,
        STAGE_INSTANCE
    }
    public ModelAuditLogEntry() { }
    public ModelAuditLogEntry(long id, int actionTypeId, long targetId, long userId, List<Change> changes, Options options, long guildId, Long timestampEnd) { }
    public List<Change> getChanges() { return null; }
    public Long getGuildId() { return null; }
    public long getId() { return 0; }
    public Options getOptions() { return null; }
    public String getReason() { return null; }
    public long getTargetId() { return 0; }
    public Long getTimestampEnd() { return null; }
    public long getUserId() { return 0; }
    public int getActionTypeId() { return 0; }
    public static ActionType getActionType(int r1) { return null; }
    public ActionType getActionType() { return null; }
    public TargetType getTargetType() { return null; }
    public static TargetType getTargetType(int i) { return null; }
}
