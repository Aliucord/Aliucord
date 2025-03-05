/*
 * This file is part of Aliucord, an Android Discord client mod.
 * Copyright (c) 2021 Juby210 & Vendicated
 * Licensed under the Open Software License version 3.0
 */

package com.aliucord.coreplugins.slashcommandsfix;

import java.util.Map;
import java.util.Optional;

class ApiPermissions {
    public Boolean user;
    public Map<Long, Boolean> roles;
    public Map<Long, Boolean> channels;

    public ApiPermissions() {
        this.user = null;
        this.roles = null;
        this.channels = null;
    }

    public Permissions toModel(Optional<Long> defaultMemberPermissions) {
        return new Permissions(Optional.ofNullable(user), roles, channels, defaultMemberPermissions);
    }
}
