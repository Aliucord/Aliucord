/*
 * This file is part of Aliucord, an Android Discord client mod.
 * Copyright (c) 2021 Juby210 & Vendicated
 * Licensed under the Open Software License version 3.0
 */

package com.aliucord.coreplugins.slashcommandsfix;

import java.util.Map;

class ApplicationIndexSourceDm implements ApplicationIndexSource {
    long userId;

    public ApplicationIndexSourceDm(long userId) {
        this.userId = userId;
    }

    @Override
    public String getEndpoint() {
        return String.format("/channels/%d/application-command-index", this.userId);
    }

    @Override
    public ApplicationIndex getIndex(Map<Long, ApplicationIndex> guildApplicationIndexes, Map<Long, ApplicationIndex> dmApplicationIndexes) {
        return dmApplicationIndexes.get(this.userId);
    }

    @Override
    public void putIndex(Map<Long, ApplicationIndex> guildApplicationIndexes, Map<Long, ApplicationIndex> dmApplicationIndexes, ApplicationIndex index) {
        dmApplicationIndexes.put(this.userId, index);
    }
}
