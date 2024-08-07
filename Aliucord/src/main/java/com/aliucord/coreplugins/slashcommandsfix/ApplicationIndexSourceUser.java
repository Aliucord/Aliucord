/*
 * This file is part of Aliucord, an Android Discord client mod.
 * Copyright (c) 2021 Juby210 & Vendicated
 * Licensed under the Open Software License version 3.0
 */

package com.aliucord.coreplugins.slashcommandsfix;

import java.util.Optional;

class ApplicationIndexSourceUser implements ApplicationIndexSource {
    public ApplicationIndexSourceUser() {}

    @Override
    public String getEndpoint() {
        return "/users/@me/application-command-index";
    }

    @Override
    public Optional<ApplicationIndex> getFromCache(ApplicationIndexCache cache) {
        return cache.user;
    }

    @Override
    public void insertIntoCache(ApplicationIndexCache cache, ApplicationIndex index) {
        cache.user = Optional.of(index);
    }

    @Override
    public void removeFromCache(ApplicationIndexCache cache) {
        cache.user = Optional.empty();
    }
}
