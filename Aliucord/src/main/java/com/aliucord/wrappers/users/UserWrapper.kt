/*
 * This file is part of Aliucord, an Android Discord client mod.
 * Copyright (c) 2025 Juby210 & Vendicated
 * Licensed under the Open Software License version 3.0
 */

package com.aliucord.wrappers.users

import com.discord.api.user.User
import com.discord.models.user.CoreUser
import com.discord.models.user.MeUser
import java.lang.reflect.Field
import java.lang.reflect.Method
import com.discord.models.user.User as ModelUser

private val apiGlobalNameField: Field = User::class.java.getDeclaredField("globalName")
private val coreGlobalNameField: Field = CoreUser::class.java.getDeclaredField("globalName")
private val meGlobalNameField: Field = MeUser::class.java.getDeclaredField("globalName")
private val modelGetGlobalName: Method = ModelUser::class.java.getDeclaredMethod("getGlobalName")

var User.globalName
    get() = apiGlobalNameField[this] as String?
    set(it) = apiGlobalNameField.set(this, it)

var CoreUser.globalName
    get() = coreGlobalNameField[this] as String?
    set(it) = coreGlobalNameField.set(this, it)

var MeUser.globalName
    get() = meGlobalNameField[this] as String?
    set(it) = meGlobalNameField.set(this, it)

val ModelUser.globalName
    get() = modelGetGlobalName(this) as String?
