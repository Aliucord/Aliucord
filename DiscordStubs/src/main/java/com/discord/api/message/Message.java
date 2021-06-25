package com.discord.api.message;

import com.discord.api.application.Application;
import com.discord.api.guildmember.GuildMember;
import com.discord.api.interaction.Interaction;
import com.discord.api.message.activity.MessageActivity;
import com.discord.api.message.allowedmentions.MessageAllowedMentions;
import com.discord.api.message.attachment.MessageAttachment;
import com.discord.api.message.call.MessageCall;
import com.discord.api.message.embed.MessageEmbed;
import com.discord.api.message.reaction.MessageReaction;
import com.discord.api.sticker.Sticker;
import com.discord.api.sticker.StickerPartial;
import com.discord.api.user.User;
import com.discord.api.utcdatetime.UtcDateTime;

import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Obfuscated class with regularly changing method names.
 * Do not use this directly, use {@link com.aliucord.wrappers.messages.MessageWrapper} instead.
 */
public final class Message {
    /**
     * getMessageReference
     * @deprecated Do not use this directly, use {@link com.aliucord.wrappers.messages.MessageWrapper} instead.
     */
    @Deprecated
    public final MessageReference A() { return null; }
    /**
     * getRetries
     * @deprecated Do not use this directly, use {@link com.aliucord.wrappers.messages.MessageWrapper} instead.
     */
    @Deprecated
    public final Integer C() { return null; }
    /**
     * isPinned
     * @deprecated Do not use this directly, use {@link com.aliucord.wrappers.messages.MessageWrapper} instead.
     */
    @Deprecated
    public final Boolean D() { return false; }
    /**
     * getReactions
     * @deprecated Do not use this directly, use {@link com.aliucord.wrappers.messages.MessageWrapper} instead.
     */
    @Deprecated
    public final List<MessageReaction> E() { return Collections.emptyList(); }
    /**
     * getReactionsMap
     * @deprecated Do not use this directly, use {@link com.aliucord.wrappers.messages.MessageWrapper} instead.
     */
    @Deprecated
    public final Map<String, MessageReaction> F() { return Collections.emptyMap(); }
    /**
     * getReferencedMessage
     * @deprecated Do not use this directly, use {@link com.aliucord.wrappers.messages.MessageWrapper} instead.
     */
    @Deprecated
    public final Message G() { return null; }
    /**
     * getStickerItems
     * @deprecated Do not use this directly, use {@link com.aliucord.wrappers.messages.MessageWrapper} instead.
     */
    @Deprecated
    public final List<StickerPartial> H() { return Collections.emptyList(); }
    /**
     * getStickers
     * @deprecated Do not use this directly, use {@link com.aliucord.wrappers.messages.MessageWrapper} instead.
     */
    @Deprecated
    public final List<Sticker> I() { return Collections.emptyList(); }
    /**
     * getTimestamp
     * @deprecated Do not use this directly, use {@link com.aliucord.wrappers.messages.MessageWrapper} instead.
     */
    @Deprecated
    public final UtcDateTime K() { return new UtcDateTime(0); }
    /**
     * isTTS
     * @deprecated Do not use this directly, use {@link com.aliucord.wrappers.messages.MessageWrapper} instead.
     */
    @Deprecated
    public final Boolean L() { return false; }
    /**
     * getType
     * @deprecated Do not use this directly, use {@link com.aliucord.wrappers.messages.MessageWrapper} instead.
     */
    @Deprecated
    public final Integer M() { return 0; }
    /**
     * getWebhookId
     * @deprecated Do not use this directly, use {@link com.aliucord.wrappers.messages.MessageWrapper} instead.
     */
    @Deprecated
    public final Long N() { return null; }
    /**
     * getActivity
     * @deprecated Do not use this directly, use {@link com.aliucord.wrappers.messages.MessageWrapper} instead.
     */
    @Deprecated
    public final MessageActivity b() { return null; }
    /**
     * getAllowedMentions
     * @deprecated Do not use this directly, use {@link com.aliucord.wrappers.messages.MessageWrapper} instead.
     */
    @Deprecated
    public final MessageAllowedMentions c() { return null; }
    /**
     * getApplication
     * @deprecated Do not use this directly, use {@link com.aliucord.wrappers.messages.MessageWrapper} instead.
     */
    @Deprecated
    public final Application d() { return null; }
    /**
     * getApplicationId
     * @deprecated Do not use this directly, use {@link com.aliucord.wrappers.messages.MessageWrapper} instead.
     */
    @Deprecated
    public final Long e() { return null; }
    /**
     * getAttachments
     * @deprecated Do not use this directly, use {@link com.aliucord.wrappers.messages.MessageWrapper} instead.
     */
    public final List<MessageAttachment> f() { return Collections.emptyList(); }
    /**
     * getAuthor
     * @deprecated Do not use this directly, use {@link com.aliucord.wrappers.messages.MessageWrapper} instead.
     */
    @Deprecated
    public final User g() { return null; }
    /**
     * getCall
     * @deprecated Do not use this directly, use {@link com.aliucord.wrappers.messages.MessageWrapper} instead.
     */
    @Deprecated
    public final MessageCall h() { return null; }
    /**
     * getChannelId
     * @deprecated Do not use this directly, use {@link com.aliucord.wrappers.messages.MessageWrapper} instead.
     */
    @Deprecated
    public final long i() { return 0; }
    /**
     * getContent
     * @deprecated Do not use this directly, use {@link com.aliucord.wrappers.messages.MessageWrapper} instead.
     */
    @Deprecated
    public final String k() { return null; }
    /**
     * getEditedTimestamp
     * @deprecated Do not use this directly, use {@link com.aliucord.wrappers.messages.MessageWrapper} instead.
     */
    @Deprecated
    public final UtcDateTime l() { return null; }
    /**
     * getEmbeds
     * @deprecated Do not use this directly, use {@link com.aliucord.wrappers.messages.MessageWrapper} instead.
     */
    @Deprecated
    public final List<MessageEmbed> m() { return Collections.emptyList(); }
    /**
     * getFlags
     * @deprecated Do not use this directly, use {@link com.aliucord.wrappers.messages.MessageWrapper} instead.
     */
    @Deprecated
    public final Long n() { return null; }
    /**
     * getGuildId
     * @deprecated Do not use this directly, use {@link com.aliucord.wrappers.messages.MessageWrapper} instead.
     */
    @Deprecated
    public final Long o() { return null; }
    /**
     * hasLocalUploads
     * @deprecated Do not use this directly, use {@link com.aliucord.wrappers.messages.MessageWrapper} instead.
     */
    @Deprecated
    public final boolean p() { return false; }
    /**
     * getHit
     * @deprecated Do not use this directly, use {@link com.aliucord.wrappers.messages.MessageWrapper} instead.
     */
    @Deprecated
    public final Boolean q() { return null; }
    /**
     * getId
     * @deprecated Do not use this directly, use {@link com.aliucord.wrappers.messages.MessageWrapper} instead.
     */
    @Deprecated
    public final long r() { return 0; }
    /**
     * getInitialAttemptTimestamp
     * @deprecated Do not use this directly, use {@link com.aliucord.wrappers.messages.MessageWrapper} instead.
     */
    @Deprecated
    public final Long s() { return null; }
    /**
     * getInteraction
     * @deprecated Do not use this directly, use {@link com.aliucord.wrappers.messages.MessageWrapper} instead.
     */
    @Deprecated
    public final Interaction t() { return null; }
    /**
     * getLastManualAttemptTimestamp
     * @deprecated Do not use this directly, use {@link com.aliucord.wrappers.messages.MessageWrapper} instead.
     */
    @Deprecated
    public final Long u() { return null; }
    /**
     * getLocalAttachments
     * @deprecated Do not use this directly, use {@link com.aliucord.wrappers.messages.MessageWrapper} instead.
     */
    @Deprecated
    public final List<LocalAttachment> v() { return Collections.emptyList(); }
    /**
     * getMember
     * @deprecated Do not use this directly, use {@link com.aliucord.wrappers.messages.MessageWrapper} instead.
     */
    @Deprecated
    public final GuildMember w() { return null; }
    /**
     * getMentionEveryone
     * @deprecated Do not use this directly, use {@link com.aliucord.wrappers.messages.MessageWrapper} instead.
     */
    @Deprecated
    public final Boolean x() { return false; }
    /**
     * getMentionRoles
     * @deprecated Do not use this directly, use {@link com.aliucord.wrappers.messages.MessageWrapper} instead.
     */
    @Deprecated
    public final List<Long> y() { return Collections.emptyList(); }
    /**
     * getMentions
     * @deprecated Do not use this directly, use {@link com.aliucord.wrappers.messages.MessageWrapper} instead.
     */
    @Deprecated
    public final List<User> z() { return Collections.emptyList(); }
}
