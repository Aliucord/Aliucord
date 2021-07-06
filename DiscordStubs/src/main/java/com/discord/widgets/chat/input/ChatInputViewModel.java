package com.discord.widgets.chat.input;

import android.content.Context;

import com.discord.api.channel.Channel;
import com.discord.api.guild.GuildVerificationLevel;
import com.discord.api.message.MessageReference;
import com.discord.models.member.GuildMember;
import com.discord.models.user.MeUser;
import com.discord.models.user.User;
import com.discord.widgets.chat.MessageContent;
import com.discord.widgets.chat.MessageManager;
import com.lytefast.flexinput.model.Attachment;

import java.util.List;

import kotlin.Unit;
import kotlin.jvm.functions.Function1;

@SuppressWarnings("unused")
public final class ChatInputViewModel {
    public static void sendMessage$default(
            ChatInputViewModel instance,
            Context context,
            MessageManager messageManager,
            MessageContent content,
            List<? extends Attachment<?>> attachments,
            boolean bool,
            Function1<? super Boolean, Unit> fn,
            int i,
            Object obj
    ) {}

    public static abstract class ViewState {
        public static final class Loaded extends ViewState {
            public static abstract class PendingReplyState {
                public static final class Hide extends PendingReplyState { }
                public static final class Replying extends PendingReplyState {
                    public final MessageReference getMessageReference() { return getMessageReference(); }
                    public final User getRepliedAuthor() { return getRepliedAuthor(); }
                    public final GuildMember getRepliedAuthorGuildMember() { return getRepliedAuthorGuildMember(); }
                    public final boolean getShouldMention() { return false; }
                    public final boolean getShowMentionToggle() { return false; }
                }
            }

            public final PendingReplyState getPendingReplyState() { return getPendingReplyState(); }
            public final boolean getAbleToSendMessage() { return false; }
            public final boolean getCanShowStickerPickerNfx() { return false; }
            public final Channel getChannel() { return getChannel(); }
            public final long getChannelId() { return 0; }
            // public final StoreChat.EditingMessage getEditingMessage() { }
            // public final ApplicationStatus getJoinRequestStatus() { }
            public final int getMaxFileSizeMB() { return 0; }
            public final MeUser getMe() { return getMe(); }
            public final boolean getShouldBadgeChatInput() { return false; }
            public final boolean getShouldShowFollow() { return false; }
            public final boolean getShouldShowVerificationGate() { return false; }

            // TODO: -- public final StoreThreadDraft.ThreadDraftState getThreadDraftState() { }
            //       -- public final StoreChannelsSelected.ResolvedSelectedChannel.ThreadDraft getSelectedThreadDraft() { }

            public final GuildVerificationLevel getVerificationLevelTriggered() { return getVerificationLevelTriggered(); }
            public final boolean isBlocked() { return false; }
            public final boolean isEditing() { return false; }
            public final boolean isInputShowing() { return false; }
            public final boolean isLurking() { return false; }
            public final boolean isOnCooldown() { return false; }
            public final boolean isReplying() { return false; }
            public final boolean isSystemDM() { return false; }
            public final boolean isVerificationLevelTriggered() { return false; }
        }
    }
}
