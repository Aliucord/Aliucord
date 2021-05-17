package com.discord.stores;

@SuppressWarnings("unused")
public final class StoreSearch {
    public static final class SearchTarget {
        public enum Type { GUILD, CHANNEL }

        public SearchTarget(Type type, long id) {}
    }

    public final StoreSearchQuery getStoreSearchQuery() { return new StoreSearchQuery(); }
}
