/*
 * This file is part of Aliucord, an Android Discord client mod.
 * Copyright (c) 2021 Juby210 & Vendicated
 * Licensed under the Open Software License version 3.0
 */

package com.aliucord.coreplugins.slashcommandsfix;

import java.util.Map;
import java.util.Optional;

class ApplicationIndexSourceGuild implements ApplicationIndexSource {
    long guildId;

    public ApplicationIndexSourceGuild(long guildId) {
        this.guildId = guildId;
    }

    @Override
    public String getEndpoint() {
        return String.format("/guilds/%d/application-command-index", this.guildId);
    }

    @Override
    public Optional<ApplicationIndex> getIndex(Map<Long, ApplicationIndex> guildApplicationIndexes, Map<Long, ApplicationIndex> dmApplicationIndexes) {
        return Optional.ofNullable(guildApplicationIndexes.get(this.guildId));
    }

    @Override
    public void putIndex(Map<Long, ApplicationIndex> guildApplicationIndexes, Map<Long, ApplicationIndex> dmApplicationIndexes, ApplicationIndex index) {
        guildApplicationIndexes.put(this.guildId, index);
    }

    @Override
    public void cleanCache(Map<Long, ApplicationIndex> guildApplicationIndexes, Map<Long, ApplicationIndex> dmApplicationIndexes) {
        guildApplicationIndexes.remove(this.guildId);
    }
}
