/*
 * This file is part of Aliucord, an Android Discord client mod.
 * Copyright (c) 2021 Juby210 & Vendicated
 * Licensed under the Open Software License version 3.0
 */

package com.aliucord.coreplugins.slashcommandsfix;

import java.util.Map;
import java.util.Optional;

class ApplicationIndexSourceDm implements ApplicationIndexSource {
    long channelId;

    public ApplicationIndexSourceDm(long channelId) {
        this.channelId = channelId;
    }

    @Override
    public String getEndpoint() {
        return String.format("/channels/%d/application-command-index", this.channelId);
    }

    @Override
    public Optional<ApplicationIndex> getFromCache(ApplicationIndexCache cache) {
        return Optional.ofNullable(
            cache.dm.get(this.channelId)
        );
    }

    @Override
    public void insertIntoCache(ApplicationIndexCache cache, ApplicationIndex index) {
        cache.dm.put(this.channelId, index);
    }

    @Override
    public void removeFromCache(ApplicationIndexCache cache) {
        cache.dm.remove(this.channelId);
    }
}
