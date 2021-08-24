/*
 * This file is part of Aliucord, an Android Discord client mod.
 * Copyright (c) 2021 Juby210 & Vendicated
 * Licensed under the Open Software License version 3.0
 */

@file:JvmName("StaticPermissionOverwriteWrapper")
package com.aliucord.wrappers

import com.discord.api.permission.PermissionOverwrite

val PermissionOverwrite.id
  get() = a()

val PermissionOverwrite.allowed
  get() = c()

val PermissionOverwrite.denied
  get() = d()

val PermissionOverwrite.type: PermissionOverwrite.Type
  get() = f()

/**
 * Wraps the obfuscated [PermissionOverwrite] class to provide nice method names and require only one central
 * update if method names change after an update
 */
@Suppress("unused")
class PermissionOverwriteWrapper(private val overwrite: PermissionOverwrite) {
  /** Returns the raw (obfuscated) [PermissionOverwrite] Object associated with this wrapper */
  fun raw(): PermissionOverwrite = overwrite

  val id
    get() = overwrite.id

  val allowed
    get() = overwrite.allowed

  val denied
    get() = overwrite.denied

  val type
    get() = overwrite.type

  companion object {
    @JvmStatic
    @Deprecated(
      "For java: use StaticPermissionOverwriteWrapper.getId\nFor kotlin: use overwrite.id",
      ReplaceWith("overwrite.id"),
      DeprecationLevel.ERROR,
    )
    fun getId(overwrite: PermissionOverwrite) = overwrite.id

    @JvmStatic
    @Deprecated(
      "For java: use StaticPermissionOverwriteWrapper.getAllowed\nFor kotlin: use overwrite.allowed",
      ReplaceWith("overwrite.allowed"),
      DeprecationLevel.ERROR,
    )
    fun getAllowed(overwrite: PermissionOverwrite) = overwrite.allowed

    @JvmStatic
    @Deprecated(
      "For java: use StaticPermissionOverwriteWrapper.getDenied\nFor kotlin: use overwrite.denied",
      ReplaceWith("overwrite.denied"),
      DeprecationLevel.ERROR,
    )
    fun getDenied(overwrite: PermissionOverwrite) = overwrite.denied

    @JvmStatic
    @Deprecated(
      "For java: use StaticPermissionOverwriteWrapper.getType\nFor kotlin: use overwrite.type",
      ReplaceWith("overwrite.type"),
      DeprecationLevel.ERROR,
    )
    fun getType(overwrite: PermissionOverwrite) = overwrite.type
  }
}
