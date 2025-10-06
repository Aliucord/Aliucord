/*
 * This file is part of Aliucord, an Android Discord client mod.
 * Copyright (c) 2025 Juby210 & Vendicated
 * Licensed under the Open Software License version 3.0
 */

package com.aliucord.wrappers.users

import com.aliucord.utils.accessField
import com.aliucord.utils.accessGetter
import com.discord.api.user.*
import com.discord.models.user.CoreUser
import com.discord.models.user.MeUser
import com.discord.models.user.User as ModelUser

var User.globalName by accessField<String?>()
var CoreUser.globalName by accessField<String?>()
var MeUser.globalName by accessField<String?>()
val ModelUser.globalName by accessGetter<String?>()

var User.avatarDecorationData by accessField<AvatarDecoration?>()
var CoreUser.avatarDecorationData by accessField<AvatarDecoration?>()
var MeUser.avatarDecorationData by accessField<AvatarDecoration?>()
val ModelUser.avatarDecorationData by accessGetter<AvatarDecoration?>()

var User.collectibles by accessField<Collectibles?>()
var CoreUser.collectibles by accessField<Collectibles?>()
var MeUser.collectibles by accessField<Collectibles?>()
val ModelUser.collectibles by accessGetter<Collectibles?>()

var User.displayNameStyles by accessField<DisplayNameStyle?>()
var CoreUser.displayNameStyles by accessField<DisplayNameStyle?>()
var MeUser.displayNameStyles by accessField<DisplayNameStyle?>()
val ModelUser.displayNameStyles by accessGetter<DisplayNameStyle?>()

var User.primaryGuild by accessField<PrimaryGuild?>()
var CoreUser.primaryGuild by accessField<PrimaryGuild?>()
var MeUser.primaryGuild by accessField<PrimaryGuild?>()
val ModelUser.primaryGuild by accessGetter<PrimaryGuild?>()
