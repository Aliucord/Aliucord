/*
 * This file is part of Aliucord, an Android Discord client mod.
 * Copyright (c) 2021 Juby210 & Vendicated
 * Licensed under the Open Software License version 3.0
 */

package com.aliucord.coreplugins.slashcommandsfix;

import java.util.Map;
import java.util.Optional;

interface ApplicationIndexSource {
    String getEndpoint();
    Optional<ApplicationIndex> getFromCache(ApplicationIndexCache cache);
    void insertIntoCache(ApplicationIndexCache cache, ApplicationIndex index);
    void removeFromCache(ApplicationIndexCache cache);
}
