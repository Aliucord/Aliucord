package org.webrtc

/* loaded from: classes2.dex */
interface Predicate<T> {
    fun and(predicate: Predicate<in T>): Predicate<T> {
        return object : Predicate<T> {
            override fun test(t10: T): Boolean {
                return this@Predicate.test(t10) && predicate.test(t10)
            }
        }
    }

    fun negate(): Predicate<T> {
        return object : Predicate<T> {
            override fun test(t10: T): Boolean {
                return !this@Predicate.test(t10)
            }
        }
    }

    fun or(predicate: Predicate<in T>): Predicate<T> {
        return object : Predicate<T> {
            override fun test(t10: T): Boolean {
                return !(!this@Predicate.test(t10) && !predicate.test(t10))
            }
        }
    }

    fun test(t10: T): Boolean
}
