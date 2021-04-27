package com.discord.utilities.search.network;

import com.discord.models.domain.ModelSearchResponse;
import com.discord.stores.StoreSearch;

import rx.Observable;

@SuppressWarnings("unused")
public class SearchFetcher {
    public static boolean access$isIndexing(SearchFetcher instance, ModelSearchResponse response) { return false; }

    public Observable<ModelSearchResponse> makeQuery(StoreSearch.SearchTarget target, Long l, SearchQuery query) { return new Observable<>(); }
}
