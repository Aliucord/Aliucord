package com.discord.stores;

import com.discord.utilities.search.query.node.QueryNode;

import java.util.Collection;
import java.util.List;
import rx.Observable;

@SuppressWarnings("unused")
public final class StoreSearch {
    public static final class SearchTarget {
        public enum Type { GUILD, CHANNEL }

        public SearchTarget(Type type, long id) {}
    }

    public final StoreSearchQuery getStoreSearchQuery() { return new StoreSearchQuery(); }
    public final void clearHistory() { }
    public final Observable<Collection<List<QueryNode>>> getHistory() { return null; }
}
