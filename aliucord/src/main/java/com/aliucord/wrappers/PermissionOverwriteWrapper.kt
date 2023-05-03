/*
 * This file is part of Aliucord, an Android Discord client mod.
 * Copyright (c) 2021 Juby210 & Vendicated
 * Licensed under the Open Software License version 3.0
 */

package com.aliucord.wrappers

import com.discord.api.permission.PermissionOverwrite

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
    val PermissionOverwrite.id
      get() = a()

    @JvmStatic
    val PermissionOverwrite.allowed
      get() = c()

    @JvmStatic
    val PermissionOverwrite.denied
      get() = d()

    @JvmStatic
    val PermissionOverwrite.type: PermissionOverwrite.Type
      get() = f()
  }
}
