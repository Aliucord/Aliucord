package com.discord.stores;

import com.discord.models.message.Message;

import kotlin.jvm.internal.DefaultConstructorMarker;

@SuppressWarnings("unused")
public final class StoreMessageReplies extends StoreV2 {
    public static abstract class MessageState {
        public static final class Deleted extends MessageState {
            public static final Deleted INSTANCE = new Deleted();

            private Deleted() { super(null); }
        }

        public static final class Loaded extends MessageState {
            public Loaded(Message message) { super(null); }

            public final Message getMessage() { return null; }
        }

        public static final class Unloaded extends MessageState {
            public static final Unloaded INSTANCE = new Unloaded();

            private Unloaded() { super(null); }
        }

        public MessageState(DefaultConstructorMarker defaultConstructorMarker) {}
    }
}
