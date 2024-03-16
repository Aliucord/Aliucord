/*
 * This file is part of Aliucord, an Android Discord client mod.
 * Copyright (c) 2021 Juby210 & Vendicated
 * Licensed under the Open Software License version 3.0
 */

package com.aliucord.coreplugins.slashcommandsfix;

import java.util.Map;

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
    public ApplicationIndex getIndex(Map<Long, ApplicationIndex> guildApplicationIndexes, Map<Long, ApplicationIndex> dmApplicationIndexes) {
        return dmApplicationIndexes.get(this.channelId);
    }

    @Override
    public void putIndex(Map<Long, ApplicationIndex> guildApplicationIndexes, Map<Long, ApplicationIndex> dmApplicationIndexes, ApplicationIndex index) {
        dmApplicationIndexes.put(this.channelId, index);
    }

    @Override
    public void cleanCache(Map<Long, ApplicationIndex> guildApplicationIndexes, Map<Long, ApplicationIndex> dmApplicationIndexes) {
        dmApplicationIndexes.remove(this.channelId);
    }
}
