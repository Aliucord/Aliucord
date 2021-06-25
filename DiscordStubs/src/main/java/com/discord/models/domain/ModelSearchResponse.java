package com.discord.models.domain;

import com.discord.api.message.Message;

import java.util.List;

@SuppressWarnings("unused")
public final class ModelSearchResponse {
    public final Integer getDocumentIndexed() { return null; }
    public final Integer getErrorCode() { return null; }
    public final List<Message> getHits() { return null; }
    public final String getMessage() { return ""; }
    public final List<List<Message>> getMessages() { return null; }
    public final long getRetryMillis() { return 0; }
    public final int getTotalResults() { return 0; }
}
