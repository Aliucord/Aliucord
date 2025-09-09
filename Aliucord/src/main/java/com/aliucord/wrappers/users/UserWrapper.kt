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

var User.globalName by accessField<User, String?>()
var CoreUser.globalName by accessField<CoreUser, String?>()
var MeUser.globalName by accessField<MeUser, String?>()
val ModelUser.globalName by accessGetter<ModelUser, String?>()

var User.avatarDecorationData by accessField<User, AvatarDecoration?>()
var CoreUser.avatarDecorationData by accessField<CoreUser, AvatarDecoration?>()
var MeUser.avatarDecorationData by accessField<MeUser, AvatarDecoration?>()
val ModelUser.avatarDecorationData by accessGetter<ModelUser, AvatarDecoration?>()

var User.collectibles by accessField<User, Collectibles?>()
var CoreUser.collectibles by accessField<CoreUser, Collectibles?>()
var MeUser.collectibles by accessField<MeUser, Collectibles?>()
val ModelUser.collectibles by accessGetter<ModelUser, Collectibles?>()

var User.displayNameStyles by accessField<User, DisplayNameStyle?>()
var CoreUser.displayNameStyles by accessField<CoreUser, DisplayNameStyle?>()
var MeUser.displayNameStyles by accessField<MeUser, DisplayNameStyle?>()
val ModelUser.displayNameStyles by accessGetter<ModelUser, DisplayNameStyle?>()

var User.primaryGuild by accessField<User, PrimaryGuild?>()
var CoreUser.primaryGuild by accessField<CoreUser, PrimaryGuild?>()
var MeUser.primaryGuild by accessField<MeUser, PrimaryGuild?>()
val ModelUser.primaryGuild by accessGetter<ModelUser, PrimaryGuild?>()
