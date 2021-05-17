package com.discord.stores;

import com.discord.api.commands.ApplicationCommandData;

import kotlin.jvm.internal.DefaultConstructorMarker;

@SuppressWarnings("unused")
public final class StoreApplicationInteractions {
    public static abstract class State {
        public static final class Failure extends State {
            public static final Failure INSTANCE = new Failure();

            private Failure() { super(null); }
        }

        public static final class Loaded extends State {
            public Loaded(ApplicationCommandData data) { super(null); }
        }

        public State(DefaultConstructorMarker defaultConstructorMarker) {}
    }
}
