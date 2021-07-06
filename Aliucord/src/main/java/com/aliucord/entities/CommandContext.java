/*
 * Copyright (c) 2021 Juby210 & Vendicated
 * Licensed under the Open Software License version 3.0
 */

package com.aliucord.entities;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.aliucord.wrappers.ChannelWrapper;
import com.discord.api.message.LocalAttachment;
import com.discord.api.message.MessageReference;
import com.discord.models.member.GuildMember;
import com.discord.models.message.Message;
import com.discord.models.user.MeUser;
import com.discord.models.user.User;
import com.discord.stores.StoreStream;
import com.discord.utilities.SnowflakeUtils;
import com.discord.utilities.attachments.AttachmentUtilsKt;
import com.discord.widgets.chat.input.ChatInputViewModel;
import com.discord.widgets.chat.input.WidgetChatInput$configureSendListeners$2;
import com.discord.widgets.chat.input.WidgetChatInput$configureSendListeners$6$1;
import com.lytefast.flexinput.model.Attachment;

import java.util.*;

@SuppressWarnings({ "unused"})
public class CommandContext {
    private final Map<String, ?> args;
    private final WidgetChatInput$configureSendListeners$2 _this;
    private final ChatInputViewModel.ViewState.Loaded viewState;
    private List<Attachment<?>> attachments;

    public CommandContext(Map<String, ?> args, WidgetChatInput$configureSendListeners$2 _this, Object[] _args) {
        this.args = args;
        this._this = _this;
        this.attachments = (List<Attachment<?>>) _args[0];
        viewState = ((WidgetChatInput$configureSendListeners$6$1) _args[2]).this$0.$viewState;
    }

    private static <T> T requireNonNull(String key, T val) {
        return Objects.requireNonNull(val, String.format("Required argument %s was null", key));
    }

    @NonNull
    public ChatInputViewModel.ViewState.Loaded getViewState() {
        return viewState;
    }

    public Context getContext() {
        return _this.$context;
    }

    public int getMaxFileSizeMB() {
        return viewState.getMaxFileSizeMB();
    }

    @Nullable
    public ChatInputViewModel.ViewState.Loaded.PendingReplyState.Replying getReplyingState() {
        ChatInputViewModel.ViewState.Loaded.PendingReplyState state = viewState.getPendingReplyState();
        if (state instanceof  ChatInputViewModel.ViewState.Loaded.PendingReplyState.Replying) return (ChatInputViewModel.ViewState.Loaded.PendingReplyState.Replying) state;
        return null;
    }

    @Nullable
    public MessageReference getMessageReference() {
        ChatInputViewModel.ViewState.Loaded.PendingReplyState.Replying state = getReplyingState();
        return state != null ? state.getMessageReference() : null;
    }

    @Nullable
    public User getReferencedMessageAuthor() {
        ChatInputViewModel.ViewState.Loaded.PendingReplyState.Replying state = getReplyingState();
        return state != null ? state.getRepliedAuthor() : null;
    }

    @Nullable
    public GuildMember getReferencedMessageAuthorGuildMember() {
        ChatInputViewModel.ViewState.Loaded.PendingReplyState.Replying state = getReplyingState();
        return state != null ? state.getRepliedAuthorGuildMember() : null;
    }

    @Nullable
    public Message getReferencedMessage() {
        MessageReference ref = getMessageReference();
        if (ref == null) return null;
        return StoreStream.getMessages().getMessage(ref.a(), ref.c());
    }

    @Nullable
    public String getReferencedMessageLink() {
        MessageReference ref = getMessageReference();
        if (ref == null) return null;
        String guildId = ref.b() != null ? String.valueOf(ref.b()) : "@me";
        return String.format(Locale.ENGLISH, "https://discord.com/channels/%s/%d/%d", guildId, ref.a(), ref.c());
    }

    public long getChannelId() {
        return _this.$chatInput.getChannelId();
    }

    public void setChannelId(long id) {
        _this.$chatInput.setChannelId(id);
    }

    @NonNull
    public ChannelWrapper getChannel() {
        return new ChannelWrapper(viewState.getChannel());
    }

    public String getRawContent() {
        return _this.$chatInput.getMatchedContentWithMetaData().getTextContent();
    }

    @NonNull
    public List<Attachment<?>> getAttachments() {
        return attachments;
    }

    public void addAttachment(String uri, String displayName) {
        addAttachment(new LocalAttachment(SnowflakeUtils.fromTimestamp(System.currentTimeMillis()), uri, displayName));
    }

    public void addAttachment(LocalAttachment attachment) {
        addAttachment(AttachmentUtilsKt.toAttachment(attachment));
    }

    public void addAttachment(Attachment<?> attachment) {
        if (!(attachments instanceof ArrayList)) attachments = new ArrayList<>(attachments);
        attachments.add(attachment);
    }

    public List<User> getMentionedUsers() {
        return _this.$chatInput.getMatchedContentWithMetaData().getMentionedUsers();
    }

    @NonNull
    public MeUser getMe() {
        return StoreStream.getUsers().getMe();
    }

    @NonNull
    public Map<String, ?> getRawArgs() {
        return args;
    }

    public boolean containsArg(String key) {
        return args.containsKey(key);
    }

    @Nullable
    public Map<String, ?> getSubCommandArgs(String key) {
        return (Map<String, ?>) args.get(key);
    }

    @Nullable
    public Object get(String key) {
        return args.get(key);
    }

    @NonNull
    public Object getRequired(String key) {
        return requireNonNull(key, get(key));
    }

    @NonNull
    public Object getOrDefault(String key, Object defaultValue) {
        Object val = get(key);
        return val != null ? val : defaultValue;
    }

    @Nullable
    public String getString(String key) {
        return (String) args.get(key);
    }

    @NonNull
    public String getRequiredString(String key) {
        return requireNonNull(key, getString(key));
    }

    @NonNull
    public String getStringOrDefault(String key, @NonNull String defaultValue) {
        String val = getString(key);
        return val != null ? val : defaultValue;
    }

    @Nullable
    public Integer getInt(String key) {
        Object val = get(key);
        if (val == null) return null;
        if (val instanceof Integer) return (Integer) val;
        if (val instanceof String) return Integer.valueOf((String) val);
        throw new ClassCastException(String.format("Argument %s is of type %s which cannot be cast to Integer.", key, val.getClass().getSimpleName()));
    }

    public int getRequiredInt(String key) {
        return requireNonNull(key, getInt(key));
    }

    public int getIntOrDefault(String key, int defaultValue) {
        Integer val = getInt(key);
        return val != null ? val : defaultValue;
    }

    @Nullable
    public Long getLong(String key) {
        Object val = get(key);
        if (val == null) return null;
        if (val instanceof Long) return (Long) val;
        if (val instanceof String) return Long.valueOf((String) val);
        throw new ClassCastException(String.format("Argument %s is of type %s which cannot be cast to Long.", key, val.getClass().getSimpleName()));
    }

    public long getRequiredLong(String key) {
        return requireNonNull(key, getLong(key));
    }

    public long getLongOrDefault(String key, long defaultValue) {
        Long val = getLong(key);
        return val != null ? val : defaultValue;
    }

    @Nullable
    public Boolean getBool(String key) {
        Object val = get(key);
        if (val == null) return null;
        if (val instanceof Boolean) return (Boolean) val;
        if (val instanceof String) return Boolean.valueOf((String) val);
        throw new ClassCastException(String.format("Argument %s is of type %s which cannot be cast to Boolean.", key, val.getClass().getSimpleName()));

    }

    public boolean getRequiredBool(String key) {
        return requireNonNull(key, getBool(key));
    }

    public boolean getBoolOrDefault(String key, boolean defaultValue) {
        Boolean val = getBool(key);
        return val != null ? val : defaultValue;
    }

    @Nullable
    public User getUser(String key) {
        Long id = getLong(key);
        return id != null ? StoreStream.getUsers().getUsers().get(id) : null;
    }

    @NonNull
    public User getRequiredUser(String key) {
        return requireNonNull(key, getUser(key));
    }

    @NonNull
    public User getUserOrDefault(String key, User defaultValue) {
        User val = getUser(key);
        return val != null ? val : defaultValue;
    }
}
