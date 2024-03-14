/*
 * This file is part of Aliucord, an Android Discord client mod.
 * Copyright (c) 2021 Juby210 & Vendicated
 * Licensed under the Open Software License version 3.0
 */

package com.aliucord.coreplugins.slashcommandsfix;

import java.util.Map;

interface ApplicationIndexSource {
    String getEndpoint();
    ApplicationIndex getIndex(Map<Long, ApplicationIndex> guildApplicationIndexes, Map<Long, ApplicationIndex> dmApplicationIndexes);
    void putIndex(Map<Long, ApplicationIndex> guildApplicationIndexes, Map<Long, ApplicationIndex> dmApplicationIndexes, ApplicationIndex index);
}