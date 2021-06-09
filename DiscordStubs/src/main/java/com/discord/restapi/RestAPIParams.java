package com.discord.restapi;

import com.discord.api.botuikit.ComponentType;
import com.discord.api.commands.ApplicationCommandData;
import com.discord.api.guild.GuildExplicitContentFilter;
import com.discord.api.guild.GuildFeature;
import com.discord.api.guild.GuildVerificationLevel;
import com.discord.api.message.activity.MessageActivityType;
import com.discord.api.presence.ClientStatus;
import com.discord.api.role.GuildRole;
import com.discord.api.utcdatetime.UtcDateTime;
import com.discord.models.domain.ModelGuildFolder;
import com.discord.models.domain.ModelMuteConfig;

import java.util.Collection;
import java.util.List;
import java.util.Map;

@SuppressWarnings("unused")
public final class RestAPIParams {
    public static final class ApplicationCommand {
        public ApplicationCommand(long type, String channelId, String applicationId, String guildId, ApplicationCommandData applicationCommandData, String nonce) { }
        public final String getApplicationId() { return null; }
        public final String getChannelId() { return null; }
        public final ApplicationCommandData getData() { return null; }
        public final String getGuildId() { return null; }
        public final String getNonce() { return null; }
        public final long getType() { return 0; }
    }

    public static final class BanGuildMember {
        public BanGuildMember() {}
        public BanGuildMember(Integer deleteDays, String reason) {}
    }

    public static final class ChannelMessagesAck {
        public ChannelMessagesAck(Boolean manual, Integer mentionCount) {}
    }


    public static final class ChannelPermissionOverwrites {
        // NOTE: why is this private bruh
        private ChannelPermissionOverwrites(Integer type, String id, Long allow, Long deny) { }
        public static ChannelPermissionOverwrites createForMember(long userId, Long allow, Long deny) { return null; }
        public static ChannelPermissionOverwrites createForRole(long roleId, Long allow, Long deny) { return null; }
    }


    public static final class ChannelPosition {
        public static final long PARENT_ID_NONE = -1;
        public ChannelPosition(long id, int position, Long parentId) { }
        public final long getId() { return 0; }
        public final Long getParentId() { return null; }
        public final int getPosition() { return 0; }
    }

    public static final class ChannelVoiceStateUpdate {
        public ChannelVoiceStateUpdate(long channelId, Boolean suppress, UtcDateTime requestToSpeakTimestamp) { }
        public final long getChannelId() { return 0; }
        public final UtcDateTime getRequestToSpeakTimestamp() { return null; }
        public final Boolean getSuppress() { return null; }
    }

    public static final class ComponentInteraction {
        public ComponentInteraction(long type, long channelId, long appId, Long guildId, long messageId, Long messageFlags, ComponentInteractionData componentInteractionData, String nonce) { }
        public final long getApplicationId() { return 0; }
        public final long getChannelId() { return 0; }
        public final ComponentInteractionData getData() { return null; }
        public final Long getGuildId() { return null; }
        public final Long getMessageFlags() { return null; }
        public final long getMessageId() { return 0; }
        public final String getNonce() { return null; }
        public final long getType() { return 0; }
    }


    public static abstract class ComponentInteractionData {
        public static final class ButtonComponentInteractionData extends ComponentInteractionData {
            public final ComponentType getComponentType() { return null; }
            public final String getCustomId() { return null; }
            public ButtonComponentInteractionData(ComponentType componentType2, String customId) { }
        }

        public static final class SelectComponentInteractionData extends ComponentInteractionData {
            public final ComponentType getComponentType() { return null; }
            public final String getCustomId() { return null; }
            public final List<String> getValues() { return null; }
            public SelectComponentInteractionData(ComponentType componentType2, String customId, List<String> values) { }
        }
    }

    public static final class ConnectedAccount {
        public ConnectedAccount(
                boolean friendSync,
                String id,
                String name,
                boolean revoked,
                boolean showActivity,
                String type,
                boolean verified,
                int visibility) { }
    }

    public static final class ConnectedAccountContacts {
        public ConnectedAccountContacts(String name, boolean friendSync) { }
    }

    public static final class ConnectionState {
        public ConnectionState(String code, String state, Boolean fromContinuation, Boolean insecure) { }
    }

    public static final class CreateGuild {
        public CreateGuild(String name, String icon, List<CreateGuildChannel> channels, Long systemChannelId) { }
    }

    public static final class CreateGuildChannel {
        public CreateGuildChannel(int type, Long id, String name, Long parentId, List<ChannelPermissionOverwrites> permissionOverwrites, String topic) { }
    }

    public static final class DeleteGuild {
        public DeleteGuild(String code) { }
    }

    public static final class EmptyBody { }

    public static final class EnableIntegration {
        public EnableIntegration(String type, String id) { }
    }

    public static final class GroupDM {
        public GroupDM(String name, String icon) { }
    }

    public static final class GuildIntegration {
        public GuildIntegration(int expireBehaviour, int expireGracePeriod, boolean enableEmoticons) { }
    }


    public static final class GuildMember {
        public GuildMember() { }
        public GuildMember(String nick, List<Long> roles, Boolean mute, Boolean deaf, Long channelId) { }
        public static GuildMember createWithChannelId(long j) { return null; }
        public static GuildMember createWithDeaf(boolean z2) { return null; }
        public static GuildMember createWithMute(boolean z2) { return null; }
        public static GuildMember createWithNick(String str) { return null; }
        public static GuildMember createWithRoles(List<Long> list) { return null; }
    }


    public static final class GuildMemberDisconnect {
        public GuildMemberDisconnect() { }
        public GuildMemberDisconnect(Long l) { }
    }

    public static final class Invite {
        public Invite(int maxAge, int maxUses, boolean temporary, String regenerate) { }
    }

    public static final class LeaveGuildBody {
        public LeaveGuildBody() { }
        public LeaveGuildBody(boolean lurking) { }
        public final boolean getLurking() { return false; }
    }

    public static final class Message {
        public static final class Activity {
            public Activity(MessageActivityType messageActivityType, String partyId, String sessionId) { }
            public final String getPartyId() { return null; }
            public final String getSessionId() { return null; }
            public final MessageActivityType getType() { return null; }
        }

        public static final class AllowedMentions {
            public AllowedMentions(List<String> parse, List<Long> users, List<Long> roles, Boolean repliedUser) { }
            public final List<String> getParse() { return null; }
            public final Boolean getRepliedUser() { return null; }
            public final List<Long> getRoles() { return null; }
            public final List<Long> getUsers() { return null; }
        }

        public static final class MessageReference {
            public MessageReference(Long guildId, long channelId, Long messageId) { }
            public final long getChannelId() { return 0; }
            public final Long getGuildId() { return null; }
            public final Long getMessageId() { return null; }
        }

        public Message(String content, String nonce, Long applicationId, Activity activity, List<Long> stickerIds, MessageReference messageReference2, AllowedMentions allowedMentions) { }
        public final Activity getActivity() { return null; }
        public final AllowedMentions getAllowedMentions() { return null; }
        public final Long getApplicationId() { return null; }
        public final String getContent() { return null; }
        public final MessageReference getMessageReference() { return null; }
        public final String getNonce() { return null; }
        public final List<Long> getStickerIds() { return null; }
    }


    public static final class Nick {
        public Nick(String nick) { }
    }

    public static final class PatchGuildEmoji {
        public PatchGuildEmoji(String name) { }
    }

    public static final class PostGuildEmoji {
        public PostGuildEmoji(String name, String image) { }
    }

    public static final class PruneGuild {
        public PruneGuild() { }
        public PruneGuild(Integer days, Boolean computePruneCount) { }
    }

    public static final class Role {
        public Role() { }
        public Role(Boolean hoist, String name, Boolean mentionable, Integer color, Integer position, Long permissions, long id) { }

        public static Role createForPosition(long j, int i) { return null; }
        public static Role createWithRole(GuildRole guildRole) { return null; }
        public final Integer getColor() { return null; }
        public final Boolean getHoist() { return null; }
        public final long getId() { return 0; }
        public final Boolean getMentionable() { return null; }
        public final String getName() { return null; }
        public final Long getPermissions() { return null; }
        public final Integer getPosition() { return null; }
        public final void setColor(Integer num) { }
        public final void setHoist(Boolean bool) { }
        public final void setId(long j) { }
        public final void setMentionable(Boolean bool) { }
        public final void setName(String str) { }
        public final void setPermissions(Long l) { }
        public final void setPosition(Integer num) { }
    }


    /*
    public static final class StartStageInstanceBody {
        public StartStageInstanceBody(long channelId, String topic, StageInstancePrivacyLevel stageInstancePrivacyLevel) { }

        public final long getChannelId() { return 0; }

        public final StageInstancePrivacyLevel getPrivacyLevel() { return null; }

        public final String getTopic() { return null; }
    }
    */

        /*
    public static final class UpdateStageInstanceBody {
        public UpdateStageInstanceBody() { }
        public UpdateStageInstanceBody(String topic, StageInstancePrivacyLevel stageInstancePrivacyLevel) { }

        public final StageInstancePrivacyLevel getPrivacyLevel() { return null; }
        public final String getTopic() { return null; }
    }
    */

    public static final class TextChannel {
        public TextChannel(String name, Integer type, String topic, Boolean nsfw, Integer rateLimit) { }
    }


    public static final class ThreadCreationSettings {
        public ThreadCreationSettings(String name, Integer autoArchiveDuration) { }
    }

    public static final class ThreadMemberSettings {
        public ThreadMemberSettings(int flags) {
        }
        public final int getFlags() { return 0; }
    }


    public static final class ThreadSettings {
        public ThreadSettings() { }
        public ThreadSettings(Boolean archived, Integer autoArchiveDuration) { }
    }


    public static final class Thumbnail {
        public Thumbnail(String thumb) { }
    }


    public static final class TopicalChannel {
        public TopicalChannel(String name, Integer type, String topic) { }
    }


    public static final class TransferGuildOwnership {
        public TransferGuildOwnership(long ownerId, String code) { }
    }


    public static final class UpdateGuild {
        public UpdateGuild() { }
        public UpdateGuild(
                Long afkChannelId,
                Integer afkTimeout,
                Long systemChannelId,
                Integer defaultMessageNotifiations,
                String icon,
                String name,
                String region,
                GuildVerificationLevel guildVerificationLevel,
                GuildExplicitContentFilter guildExplicitContentFilter,
                String splash,
                String banner,
                Integer systemChannelFlags,
                List<? extends GuildFeature> features,
                Long rulesChannelId,
                Long publicUpdatesChannelId,
                String preferredLocale) {
        }
        public static UpdateGuild createForExplicitContentFilter(GuildExplicitContentFilter guildExplicitContentFilter) { return null; }
        public static UpdateGuild createForVerificationLevel(GuildVerificationLevel guildVerificationLevel) { return null; }
        public final List<GuildFeature> getFeatures() { return null; }
        public final String getPreferredLocale() { return null; }
        public final Long getPublicUpdatesChannelId() { return null; }
        public final Long getRulesChannelId() { return null; }
    }


    public static final class UserBulkRelationship {
        public UserBulkRelationship(List<Long> userIds, String token) { }
    }



    public static final class UserGuildSettings {
        public static final class ChannelOverride {
            public ChannelOverride(Boolean muted, ModelMuteConfig modelMuteConfig, Integer messageNotifications) { }
            public final Integer getMessageNotifications() { return null; }
            public final ModelMuteConfig getMuteConfig() { return null; }
            public final Boolean getMuted() { return null; }
        }

        public UserGuildSettings() { }

        public UserGuildSettings(
                Boolean suppressAtEveryone,
                Boolean suppressRoleMentions,
                Boolean muted,
                ModelMuteConfig modelMuteConfig,
                Boolean mobilePush,
                Integer messageNotifications,
                Map<Long, ChannelOverride> channelOverrides) { }

        public final Map<Long, ChannelOverride> getChannelOverrides() { return null; }
        public final Integer getMessageNotifications() { return null; }
        public final Boolean getMobilePush() { return null; }
        public final ModelMuteConfig getMuteConfig() { return null; }
        public final Boolean getMuted() { return null; }
        public final Boolean getSuppressEveryone() { return null; }
        public final Boolean getSuppressRoles() { return null; }

        public UserGuildSettings(long categoryIdIThink, ChannelOverride channelOverride) { }
    }

    public static final class UserNoteUpdate {
        public UserNoteUpdate(String note) { }
    }


    public static final class UserRelationship {
        public static final class Add {
            public Add(String name, int discriminator) {
            }
        }
        public UserRelationship(Integer type, String friendToken) { }
    }


    public static final class UserSettings {
        public static final class FriendSourceFlags {
            public FriendSourceFlags(Boolean all, Boolean mutualGuilds, Boolean mutualFriends) { }
        }
        private UserSettings(
                String theme,
                Boolean developerMode,
                Boolean renderEmbeds,
                Boolean inlineEmbedMedia,
                Boolean inlineAttachmentMedia,
                Boolean blockedMessageBar,
                String locale,
                Collection<Long> restrictedGuilds,
                String status,
                Boolean showCurrentGame,
                Collection<ModelGuildFolder> guildFolders,
                Boolean defaultGuildsRestricted,
                FriendSourceFlags friendSourceFlags,
                Integer explicitContentFilter,
                Boolean animateEmojis,
                Boolean allowAccessibilityDetection,
                Integer animateStickers,
                Integer friendDiscoveryFlags) { }

        public static UserSettings createWithAllowAccessibilityDetection(Boolean bool) { return null; }
        public static UserSettings createWithAllowAnimatedEmojis(Boolean bool) { return null; }
        public static UserSettings createWithBlockedMessageBar(boolean z2) { return null; }
        public static UserSettings createWithDeveloperMode(boolean z2) { return null; }
        public static UserSettings createWithExplicitContentFilter(int i) { return null; }
        public static UserSettings createWithFriendDiscoveryFlags(Integer num) { return null; }
        public static UserSettings createWithFriendSourceFlags(Boolean bool, Boolean bool2, Boolean bool3) { return null; }
        public static UserSettings createWithGuildFolders(List<ModelGuildFolder> list) { return null; }
        public static UserSettings createWithInlineAttachmentMedia(boolean z2) { return null; }
        public static UserSettings createWithInlineEmbedMedia(boolean z2) { return null; }
        public static UserSettings createWithLocale(String str) { return null; }
        public static UserSettings createWithRenderEmbeds(boolean z2) { return null; }
        public static UserSettings createWithRestrictedGuilds(Boolean bool, Collection<Long> collection) { return null; }
        public static UserSettings createWithShowCurrentGame(boolean z2) { return null; }
        public static UserSettings createWithStatus(ClientStatus clientStatus) { return null; }
        public static UserSettings createWithStickerAnimationSettings(Integer num) { return null; }
        public static UserSettings createWithTheme(String str) { return null; }
    }

    /*
    public static final class UserSettingsCustomStatus {
        public UserSettingsCustomStatus(ModelCustomStatusSetting modelCustomStatusSetting) { }
    }
    */

    public static final class VanityUrl {
        public VanityUrl() { }
        public VanityUrl(String code) { }
    }

    public static final class VoiceChannel {
        public VoiceChannel(String name, String topic, Integer type, Integer userLimit, Integer bitrate, String rtcRegion) { }
    }

    public static final class CreateChannel {
        public CreateChannel(List<Long> recipients) { }
        public CreateChannel(long recipient) { }
    }
}