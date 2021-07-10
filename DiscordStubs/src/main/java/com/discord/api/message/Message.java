package com.discord.api.message;

import com.discord.api.application.Application;
import com.discord.api.botuikit.Component;
import com.discord.api.channel.Channel;
import com.discord.api.guildmember.GuildMember;
import com.discord.api.interaction.Interaction;
import com.discord.api.message.activity.MessageActivity;
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

/**
 * Obfuscated class with regularly changing method names.
 * Do not use this directly, use {@link com.discord.models.message.Message} instead.
 */
public final class Message {
    /**
     * getStickers
     * @deprecated Do not use this directly, use {@link com.discord.models.message.Message} instead.
     */
    @Deprecated
    public final List<Sticker> A() { return Collections.emptyList(); }
    /**
     * getThread
     * @deprecated Do not use this directly, use {@link com.discord.models.message.Message} instead.
     */
    @Deprecated
    public final Channel B() { return null; }
    /**
     * getTimestamp
     * @deprecated Do not use this directly, use {@link com.discord.models.message.Message} instead.
     */
    @Deprecated
    public final UtcDateTime C() { return new UtcDateTime(0); }
    /**
     * isTTS
     * @deprecated Do not use this directly, use {@link com.discord.models.message.Message} instead.
     */
    @Deprecated
    public final Boolean D() { return false; }
    /**
     * getType
     * @deprecated Do not use this directly, use {@link com.discord.models.message.Message} instead.
     */
    @Deprecated
    public final Integer E() { return 0; }
    /**
     * getWebhookId
     * @deprecated Do not use this directly, use {@link com.discord.models.message.Message} instead.
     */
    @Deprecated
    public final Long F() { return null; }
    /**
     * getActivity
     * @deprecated Do not use this directly, use {@link com.discord.models.message.Message} instead.
     */
    @Deprecated
    public final MessageActivity a() { return null; }
    /**
     * getApplication
     * @deprecated Do not use this directly, use {@link com.discord.models.message.Message} instead.
     */
    @Deprecated
    public final Application b() { return null; }
    /**
     * getApplicationId
     * @deprecated Do not use this directly, use {@link com.discord.models.message.Message} instead.
     */
    @Deprecated
    public final Long c() { return null; }
    /**
     * getAttachments
     * @deprecated Do not use this directly, use {@link com.discord.models.message.Message} instead.
     */
    public final List<MessageAttachment> d() { return Collections.emptyList(); }
    /**
     * getAuthor
     * @deprecated Do not use this directly, use {@link com.discord.models.message.Message} instead.
     */
    @Deprecated
    public final User e() { return null; }
    /**
     * getCall
     * @deprecated Do not use this directly, use {@link com.discord.models.message.Message} instead.
     */
    @Deprecated
    public final MessageCall f() { return null; }
    /**
     * getChannelId
     * @deprecated Do not use this directly, use {@link com.discord.models.message.Message} instead.
     */
    @Deprecated
    public final long g() { return 0; }
    /**
     * getComponents
     * @deprecated Do not use this directly, use {@link com.discord.models.message.Message} instead.
     */
    @Deprecated
    public final List<Component> h() { return Collections.emptyList(); }
    /**
     * getContent
     * @deprecated Do not use this directly, use {@link com.discord.models.message.Message} instead.
     */
    @Deprecated
    public final String i() { return null; }
    /**
     * getEditedTimestamp
     * @deprecated Do not use this directly, use {@link com.discord.models.message.Message} instead.
     */
    @Deprecated
    public final UtcDateTime j() { return null; }
    /**
     * getEmbeds
     * @deprecated Do not use this directly, use {@link com.discord.models.message.Message} instead.
     */
    @Deprecated
    public final List<MessageEmbed> k() { return Collections.emptyList(); }
    /**
     * getFlags
     * @deprecated Do not use this directly, use {@link com.discord.models.message.Message} instead.
     */
    @Deprecated
    public final Long l() { return null; }
    /**
     * getGuildId
     * @deprecated Do not use this directly, use {@link com.discord.models.message.Message} instead.
     */
    @Deprecated
    public final Long m() { return null; }
    /**
     * getHit
     * @deprecated Do not use this directly, use {@link com.discord.models.message.Message} instead.
     */
    @Deprecated
    public final Boolean n() { return null; }
    /**
     * getId
     * @deprecated Do not use this directly, use {@link com.discord.models.message.Message} instead.
     */
    @Deprecated
    public final long o() { return 0; }
    /**
     * getInteraction
     * @deprecated Do not use this directly, use {@link com.discord.models.message.Message} instead.
     */
    @Deprecated
    public final Interaction p() { return null; }
    /**
     * getMember
     * @deprecated Do not use this directly, use {@link com.discord.models.message.Message} instead.
     */
    @Deprecated
    public final GuildMember q() { return null; }
    /**
     * getMentionEveryone
     * @deprecated Do not use this directly, use {@link com.discord.models.message.Message} instead.
     */
    @Deprecated
    public final Boolean r() { return false; }
    /**
     * getMentionRoles
     * @deprecated Do not use this directly, use {@link com.discord.models.message.Message} instead.
     */
    @Deprecated
    public final List<Long> s() { return Collections.emptyList(); }
    /**
     * getMentions
     * @deprecated Do not use this directly, use {@link com.discord.models.message.Message} instead.
     */
    @Deprecated
    public final List<User> t() { return Collections.emptyList(); }
    /**
     * getMessageReference
     * @deprecated Do not use this directly, use {@link com.discord.models.message.Message} instead.
     */
    @Deprecated
    public final MessageReference u() { return null; }
    /**
     * isPinned
     * @deprecated Do not use this directly, use {@link com.discord.models.message.Message} instead.
     */
    @Deprecated
    public final Boolean w() { return false; }
    /**
     * getReactions
     * @deprecated Do not use this directly, use {@link com.discord.models.message.Message} instead.
     */
    @Deprecated
    public final List<MessageReaction> x() { return Collections.emptyList(); }
    /**
     * getReferencedMessage
     * @deprecated Do not use this directly, use {@link com.discord.models.message.Message} instead.
     */
    @Deprecated
    public final Message y() { return null; }
    /**
     * getStickerItems
     * @deprecated Do not use this directly, use {@link com.discord.models.message.Message} instead.
     */
    @Deprecated
    public final List<StickerPartial> z() { return Collections.emptyList(); }
}
