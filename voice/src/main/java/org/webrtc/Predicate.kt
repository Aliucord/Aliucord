package org.webrtc

fun interface Predicate<T> {
    fun test(arg: T): Boolean

    fun and(predicate: Predicate<in T>): Predicate<T> {
        return Predicate { test(it) && predicate.test(it) }
    }

    fun negate(): Predicate<T> {
        return Predicate { !test(it) }
    }

    fun or(predicate: Predicate<in T>): Predicate<T> {
        return Predicate { test(it) || predicate.test(it) }
    }
}
