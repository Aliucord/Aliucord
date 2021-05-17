package com.discord.widgets.chat.input;

import android.content.Context;

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
}
