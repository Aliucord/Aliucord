/*
 * Copyright 2010-2023 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

// Temporary polyfill until Kotlin 1.8+ stdlib is implemented in Aliucord Manager

@file:JvmName("EnumEntriesKt")
@file:Suppress("unused")

package kotlin.enums

@SinceKotlin("1.9")
interface EnumEntries<E : Enum<E>> : List<E>

@PublishedApi
@SinceKotlin("1.8")
internal fun <E : Enum<E>> enumEntries(entries: Array<E>): EnumEntries<E> = EnumEntriesList(entries)

@SinceKotlin("1.8")
private class EnumEntriesList<T : Enum<T>>(private val entries: Array<T>) : EnumEntries<T>, AbstractList<T>() {
    override val size: Int
        get() = entries.size

    override fun get(index: Int): T {
        return entries[index]
    }
}
