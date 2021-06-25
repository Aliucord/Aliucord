/*
 * Copyright (c) 2021 Juby210
 * Licensed under the Open Software License version 3.0
 */

package com.aliucord.wrappers.messages;

import com.discord.api.application.Application;
import com.discord.api.guildmember.GuildMember;
import com.discord.api.interaction.Interaction;
import com.discord.api.message.LocalAttachment;
import com.discord.api.message.Message;
import com.discord.api.message.MessageReference;
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

import java.util.List;
import java.util.Map;

/**
 * Wraps the obfuscated {@link MessageReference} class to provide nice method names and require only one central
 * update if method names change after an update
 */
@SuppressWarnings({"unused", "deprecation"})
public class MessageWrapper {
    private final Message message;

    public MessageWrapper(Message message) {
        this.message = message;
    }

    /** Returns the raw (obfuscated) {@link Message} Object associated with this wrapper */
    public final Message raw() {
        return message;
    }

    public final MessageReference getMessageReference() {
        return getMessageReference(message);
    }

    public final Integer getRetries() {
        return getRetries(message);
    }

    public final Boolean isPinned() {
        return isPinned(message);
    }

    public final List<MessageReaction> getReactions() {
        return getReactions(message);
    }

    public final Map<String, MessageReaction> getReactionsMap() {
        return getReactionsMap(message);
    }

    public final Message getReferencedMessage() {
        return getReferencedMessage(message);
    }

    public final List<StickerPartial> getStickerItems() {
        return getStickerItems(message);
    }

    public final List<Sticker> getStickers() {
        return getStickers(message);
    }

    public final UtcDateTime getTimestamp() {
        return getTimestamp(message);
    }

    public final Boolean isTTS() {
        return isTTS(message);
    }

    public final Integer getType() {
        return getType(message);
    }

    public final Long getWebhookId() {
        return getWebhookId(message);
    }

    public final MessageActivity getActivity() {
        return getActivity(message);
    }

    public final MessageAllowedMentions getAllowedMentions() {
        return getAllowedMentions(message);
    }

    public final Application getApplication() {
        return getApplication(message);
    }

    public final Long getApplicationId() {
        return getApplicationId(message);
    }

    public final List<MessageAttachment> getAttachments() {
        return getAttachments(message);
    }

    public final User getAuthor() {
        return getAuthor(message);
    }

    public final MessageCall getCall() {
        return getCall(message);
    }

    public final long getChannelId() {
        return getChannelId(message);
    }

    public final String getContent() {
        return getContent(message);
    }

    public final UtcDateTime getEditedTimestamp() {
        return getEditedTimestamp(message);
    }

    public final List<MessageEmbed> getEmbeds() {
        return getEmbeds(message);
    }

    public final Long getFlags() {
        return getFlags(message);
    }

    public final Long getGuildId() {
        return getGuildId(message);
    }

    public final boolean hasLocalUploads() {
        return hasLocalUploads(message);
    }

    public final Boolean getHit() {
        return getHit(message);
    }

    public final long getId() {
        return getId(message);
    }

    public final Long getInitialAttemptTimestamp() {
        return getInitialAttemptTimestamp(message);
    }

    public final Interaction getInteraction() {
        return getInteraction(message);
    }

    public final Long getLastManualAttemptTimestamp() {
        return getLastManualAttemptTimestamp(message);
    }

    public final List<LocalAttachment> getLocalAttachments() {
        return getLocalAttachments(message);
    }

    public final GuildMember getMember() {
        return getMember(message);
    }

    public final Boolean getMentionEveryone() {
        return getMentionEveryone(message);
    }

    public final List<Long> getMentionRoles() {
        return getMentionRoles(message);
    }

    public final List<User> getMentions() {
        return getMentions(message);
    }



    public static MessageReference getMessageReference(Message message) {
        return message.A();
    }

    public static Integer getRetries(Message message) {
        return message.C();
    }

    public static Boolean isPinned(Message message) {
        return message.D();
    }

    public static List<MessageReaction> getReactions(Message message) {
        return message.E();
    }

    public static Map<String, MessageReaction> getReactionsMap(Message message) {
        return message.F();
    }

    public static Message getReferencedMessage(Message message) {
        return message.G();
    }

    public static List<StickerPartial> getStickerItems(Message message) {
        return message.H();
    }

    public static List<Sticker> getStickers(Message message) {
        return message.I();
    }

    public static UtcDateTime getTimestamp(Message message) {
        return message.K();
    }

    public static Boolean isTTS(Message message) {
        return message.L();
    }

    public static Integer getType(Message message) {
        return message.M();
    }

    public static Long getWebhookId(Message message) {
        return message.N();
    }

    public static MessageActivity getActivity(Message message) {
        return message.b();
    }

    public static MessageAllowedMentions getAllowedMentions(Message message) {
        return message.c();
    }

    public static Application getApplication(Message message) {
        return message.d();
    }

    public static Long getApplicationId(Message message) {
        return message.e();
    }

    public static List<MessageAttachment> getAttachments(Message message) {
        return message.f();
    }

    public static User getAuthor(Message message) {
        return message.g();
    }

    public static MessageCall getCall(Message message) {
        return message.h();
    }

    public static long getChannelId(Message message) {
        return message.i();
    }

    public static String getContent(Message message) {
        return message.k();
    }

    public static UtcDateTime getEditedTimestamp(Message message) {
        return message.l();
    }

    public static List<MessageEmbed> getEmbeds(Message message) {
        return message.m();
    }

    public static Long getFlags(Message message) {
        return message.n();
    }

    public static Long getGuildId(Message message) {
        return message.o();
    }

    public static boolean hasLocalUploads(Message message) {
        return message.p();
    }

    public static Boolean getHit(Message message) {
        return message.q();
    }

    public static long getId(Message message) {
        return message.r();
    }

    public static Long getInitialAttemptTimestamp(Message message) {
        return message.s();
    }

    public static Interaction getInteraction(Message message) {
        return message.t();
    }

    public static Long getLastManualAttemptTimestamp(Message message) {
        return message.u();
    }

    public static List<LocalAttachment> getLocalAttachments(Message message) {
        return message.v();
    }

    public static GuildMember getMember(Message message) {
        return message.w();
    }

    public static Boolean getMentionEveryone(Message message) {
        return message.x();
    }

    public static List<Long> getMentionRoles(Message message) {
        return message.y();
    }

    public static List<User> getMentions(Message message) {
        return message.z();
    }
}
