package com.discord.utilities.rest;

import com.discord.api.application.Application;
import com.discord.api.channel.Channel;
import com.discord.api.commands.ApplicationCommandData;
import com.discord.api.guild.Guild;
import com.discord.api.message.Message;
import com.discord.api.role.GuildRole;
import com.discord.models.domain.ModelAuditLog;
import com.discord.models.domain.ModelBan;
import com.discord.models.domain.ModelConnectedAccount;
import com.discord.models.domain.ModelInvite;
import com.discord.models.domain.ModelSearchResponse;
import com.discord.models.domain.ModelUserProfile;
import com.discord.models.domain.emoji.ModelEmojiGuild;
import com.discord.models.user.User;
import com.discord.restapi.RestAPIParams;
import com.discord.utilities.time.Clock;

import java.util.List;

import kotlin.Unit;
import rx.Observable;

@SuppressWarnings("unused")
public final class RestAPI {
    public static final Companion Companion = null;
    public static RestAPI api = new RestAPI();
    public static RestAPI apiSerializeNulls = new RestAPI();
    public static RestAPI apiSpotify = new RestAPI();

    public static final class AppHeadersProvider {
        public String getAuthToken() { return ""; }
        public String getFingerprint() { return ""; }
        public String getLocale() { return ""; }
        public String getSpotifyToken() { return ""; }
        public String getUserAgent() { return ""; }
    }

    public static final class Companion {
        public final RestAPI getApi() { return new RestAPI(); }
        public final RestAPI getApiSerializeNulls() { return new RestAPI(); }
        public final RestAPI getApiSpotify() { return new RestAPI(); }
    }

    public static RestAPI getApi() { return new RestAPI(); }
    public static RestAPI getApiSerializeNulls() { return new RestAPI(); }
    public static RestAPI getApiSpotify() { return new RestAPI(); }

    public Observable<Void> acceptGift(String code) { return new Observable<>(); }

    public Observable<Void> addChannelPin(long j, long j2) {
        return new Observable<>();
    }
    public Observable<Void> addChannelRecipient(long channelId, long userId) { return new Observable<>(); }
    public final Observable<Channel> addGroupRecipients(long groupId, List<? extends User> recipients) { return new Observable<>(); }

    /**
     * The emoji must be URL-Encoded (see {@link java.net.URLEncoder}) or the request will fail with 10014: Unknown Emoji.
     * To use a custom emoji, you must encode it in the format name:id with the emoji name and emoji id.
     */
    public Observable<Void> addReaction(long channelId, long messageId, String emoji) { return new Observable<>(); }

    public final Observable<Void> addRelationship(String location, long j, Integer num, String str2) { return new Observable<>(); }

    public Observable<Void> banGuildMember(long guildId, long userId, RestAPIParams.BanGuildMember banGuildMember) { return new Observable<>(); }

    public Observable<Void> batchUpdateRole(long guildId, List<RestAPIParams.Role> list) { return new Observable<>(); }

    public Observable<Void> changeGuildMember(long guildId, long userId, RestAPIParams.GuildMember guildMember) { return new Observable<>(); }

    public Observable<Void> changeGuildNickname(long guildId, RestAPIParams.Nick nick) { return new Observable<>(); }

    public Observable<Channel> convertDMToGroup(long channelId, long recipientId) { return new Observable<>(); }

    public final Observable<Channel> createGroupDM(List<Long> userIds) { return new Observable<>(); }

    public Observable<Guild> createGuild(RestAPIParams.CreateGuild createGuild) { return new Observable<>(); }

    public Observable<Channel> createGuildChannel(long guildId, RestAPIParams.CreateGuildChannel createGuildChannel) { return new Observable<>(); }

    public final Observable<Channel> createOrFetchDM(long userId) { return new Observable<>(); }

    public Observable<GuildRole> createRole(long guildId) { return new Observable<>(); }

    public Observable<Channel> createThread(long channelId, RestAPIParams.ThreadCreationSettings threadCreationSettings) { return new Observable<>(); }

    public Observable<Channel> createThreadFromMessage(long channelId, long messageId, RestAPIParams.ThreadCreationSettings threadCreationSettings) { return new Observable<>(); }

    public Observable<Void> crosspostMessage(long channelId, Long messageId) { return new Observable<>(); }

    public Observable<Channel> deleteChannel(long channelId) { return new Observable<>(); }

    public Observable<Void> deleteChannelPin(long channelId, long messageId) { return new Observable<>(); }

    public Observable<Void> deleteGuild(long guildId, RestAPIParams.DeleteGuild deleteGuild) { return new Observable<>(); }

    public Observable<Void> deleteGuildEmoji(long guildId, long emojiId) { return new Observable<>(); }

    public Observable<Void> deleteGuildIntegration(long guildId, long integrationId) { return new Observable<>(); }

    public Observable<Void> deleteMessage(long channelId, long messageId) { return new Observable<>(); }

    public Observable<Void> deletePermissionOverwrites(long channelId, long targetId) { return new Observable<>(); }

    public Observable<Void> deleteRole(long guildId, long roleId) { return new Observable<>(); }

    public Observable<Void> disconnectGuildMember(long guildId, long userId, RestAPIParams.GuildMemberDisconnect guildMemberDisconnect) { return new Observable<>(); }

    public Observable<Channel> editGroupDM(long channelId, RestAPIParams.GroupDM groupDM) { return new Observable<>(); }

    public Observable<Message> editMessage(long channelId, long messageId, RestAPIParams.Message message) { return new Observable<>(); }

    public Observable<Channel> editTextChannel(long channelId, RestAPIParams.TextChannel textChannel) { return new Observable<>(); }

    public final Observable<Channel> editTextChannel(long j, String str, Integer num, String str2, Boolean bool, Integer num2) { return new Observable<>(); }

    public Observable<Channel> editThread(long j, RestAPIParams.ThreadSettings threadSettings) { return new Observable<>(); }

    public Observable<Channel> editTopicalChannel(long channelId, RestAPIParams.TopicalChannel topicalChannel) { return new Observable<>(); }

    public Observable<Channel> editVoiceChannel(long channelId, RestAPIParams.VoiceChannel voiceChannel) { return new Observable<>(); }

    public final Observable<Channel> editVoiceChannel(long j, String str, String str2, Integer num, Integer num2, Integer num3, String str3) { return new Observable<>(); }

    public Observable<Unit> endStageInstance(long channelId) { return new Observable<>(); }

    // public Observable<ThreadListing> getAllPrivateArchivedThreads(long channelId, String before) { return new Observable<>(); }
    // public Observable<ThreadListing> getAllPublicArchivedThreads(long channelId, String before) { return new Observable<>(); }

    public Observable<List<Application>> getApplications(long applicationIds) { return new Observable<>(); }

    public Observable<ModelAuditLog> getAuditLogs(long guildId, int limit, Long before, Long userId, Integer actionType) { return new Observable<>(); }

    public final Observable<ModelAuditLog> getAuditLogs(long j, Long l, Long l2, Integer num) { return new Observable<>(); }

    public Observable<List<ModelBan>> getBans(long guildId) { return new Observable<>(); }

    public Observable<List<Message>> getChannelMessages(long channelId, Long before, Long after, Integer limit) { return new Observable<>(); }

    public Observable<List<Message>> getChannelMessagesAround(long channelId, int limit, long around) { return new Observable<>(); }

    public Observable<List<Message>> getChannelPins(long channelId) { return new Observable<>(); }

    public final Observable<Integer> getClientVersion() { return new Observable<>(); }

    // public Observable<ModelConnectionState> getConnectionState(String connection, String pinNumber) { return new Observable<>(); }

    public Observable<List<ModelConnectedAccount>> getConnections() { return new Observable<>(); }

    public Observable<Guild> getEmojiGuild(long emojiId) { return new Observable<>(); }

    public Observable<List<ModelEmojiGuild>> getGuildEmojis(long guilId) { return new Observable<>(); }

    public Observable<List<ModelInvite>> getGuildInvites(long guildId) { return new Observable<>(); }

    public Observable<ApplicationCommandData> getInteractionData(long channelId, long messageId) { return new Observable<>(); }

    public Observable<ModelInvite> getInviteCode(String code, boolean withCounts) { return new Observable<>(); }

    public Observable<List<Message>> getMentions(int limit, boolean includeRoleMentions, boolean includeAtEveryone, Long guildId, Long before) { return new Observable<>(); }

    /**
     * The emoji must be URL-Encoded (see {@link java.net.URLEncoder}) or the request will fail with 10014: Unknown Emoji.
     * To use a custom emoji, you must encode it in the format name:id with the emoji name and emoji id.
     */
    public Observable<List<com.discord.api.user.User>> getReactionUsers(long channelId, long messageId, String emoji, Integer limit) { return new Observable<>(); }

    // public Observable<List<ModelUserRelationship>> getRelationships(long userId) { return new Observable<>(); }

    public Observable<List<Guild>> getUserJoinRequestGuilds() { return new Observable<>(); }

    public Observable<Void> ignoreFriendSuggestion(long userId) { return new Observable<>(); }

    public final Observable<Void> inviteUserToSpeak(Channel channel, long j, Clock clock) { return new Observable<>(); }

    public Observable<Guild> joinGuild(long guildId, boolean lurker, String sessionId) { return new Observable<>(); }

    public Observable<Void> joinGuildFromIntegration(String integrationId) { return new Observable<>(); }

    public Observable<Void> kickGuildMember(long guildId, long userId, String reason) { return new Observable<>(); }

    public Observable<Void> leaveGuild(long guildId) { return new Observable<>(); }

    public Observable<Void> leaveThread(long channelId, String location) { return new Observable<>(); }

    public Observable<ModelEmojiGuild> patchGuildEmoji(long guildId, long emojiId, RestAPIParams.PatchGuildEmoji patchGuildEmoji) { return new Observable<>(); }

    public Observable<ModelInvite> postChannelInvite(long channelId, RestAPIParams.Invite invite) { return new Observable<>(); }

    public Observable<Void> postChannelMessagesAck(long channelId, Long messageId, RestAPIParams.ChannelMessagesAck channelMessagesAck) { return new Observable<>(); }

    public Observable<ModelEmojiGuild> postGuildEmoji(long guildId, RestAPIParams.PostGuildEmoji postGuildEmoji) { return new Observable<>(); }

    public final Observable<ModelInvite> postInviteCode(ModelInvite modelInvite, String str) { return new Observable<>(); }

    public Observable<ModelInvite> postInviteCode(String code, RestAPIParams.EmptyBody emptyBody, String xContextProperties) { return new Observable<>(); }

    public Observable<Void> pruneMembers(long guildId, RestAPIParams.PruneGuild pruneGuild) { return new Observable<>(); }

    public Observable<Void> removeAllReactions(long channelId, long messageId) { return new Observable<>(); }

    public Observable<Void> removeChannelRecipient(long channelId, long recipientId) { return new Observable<>(); }

    /**
     * The emoji must be URL-Encoded (see {@link java.net.URLEncoder}) or the request will fail with 10014: Unknown Emoji.
     * To use a custom emoji, you must encode it in the format name:id with the emoji name and emoji id.
     */
    public Observable<Void> removeReaction(long channelId, long messageId, String reaction, long userId) { return new Observable<>(); }

    public final Observable<Void> removeRelationship(String location, long userId) { return new Observable<>(); }

    /**
     * The emoji must be URL-Encoded (see {@link java.net.URLEncoder}) or the request will fail with 10014: Unknown Emoji.
     * To use a custom emoji, you must encode it in the format name:id with the emoji name and emoji id.
     */
    public Observable<Void> removeSelfReaction(long channelId, long messageId, String emoji) { return new Observable<>(); }

    public final Observable<Void> requestToSpeak(Channel channel, Clock clock) { return new Observable<>(); }

    public Observable<ModelInvite> revokeInvite(String inviteCode) { return new Observable<>(); }

    public Observable<ModelSearchResponse> searchChannelMessages(long channelId, Long maxId, List<String> authorId, List<String> mentions, List<String> has, List<String> content, Integer attempts, Boolean includeNsfw) { return new Observable<>(); }

    public Observable<ModelSearchResponse> searchGuildMessages(long guildId, Long maxId, List<String> authorIds, List<String> mentions, List<String> channelIds, List<String> has, List<String> content, Integer attempts, Boolean includeNsfw) { return new Observable<>(); }

    public Observable<Message> sendMessage(long channelId, RestAPIParams.Message message) { return new Observable<>(); }

    /** Location is something like ContextMenu telling discord via which menu you added the user */
    public final Observable<Void> sendRelationshipRequest(String location, String username, int discriminator) { return new Observable<>(); }

    public final Observable<Void> setMeSuppressed(Channel channel, boolean z2) { return new Observable<>(); }

    public final Observable<Void> setUserSuppressed(Channel channel, long userId, boolean z2) { return new Observable<>(); }

    // public Observable<ModelTypingResponse> setUserTyping(long channelId, RestAPIParams.EmptyBody emptyBody) { return new Observable<>(); }

    public final Observable<Void> stopRinging(long j, long j2, List<Long> list) { return new Observable<>(); }

    public Observable<Void> syncIntegration(long guildId, long integrationId) { return new Observable<>(); }

    public Observable<Void> unbanUser(long guildId, long userId) { return new Observable<>(); }

    public Observable<Guild> updateGuild(long guildId, RestAPIParams.UpdateGuild updateGuild) { return new Observable<>(); }

    public Observable<Void> updatePermissionOverwrites(long channelId, long targetId, RestAPIParams.ChannelPermissionOverwrites channelPermissionOverwrites) { return new Observable<>(); }

    // public Observable<ModelNotificationSettings> updatePrivateChannelSettings(RestAPIParams.UserGuildSettings userGuildSettings) { return new Observable<>(); }

    public Observable<Void> updateRole(long guildId, long roleId, RestAPIParams.Role role) { return new Observable<>(); }

    public Observable<com.discord.api.user.User> userGet(long userId) { return new Observable<>(); }

    public Observable<ModelUserProfile> userProfileGet(long id) { return new Observable<>(); }
}
