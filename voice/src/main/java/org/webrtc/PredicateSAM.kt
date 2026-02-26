package org.webrtc

// Gradle gets confused between Discord's Predicate and WebRTC's Predicate :/
fun interface PredicateSAM<T> : Predicate<T> {
    override fun test(arg: T): Boolean

    override fun and(predicate: Predicate<in T>): Predicate<T> {
        return PredicateSAM { test(it) && predicate.test(it) }
    }

    override fun negate(): Predicate<T> {
        return PredicateSAM { !test(it) }
    }

    override fun or(predicate: Predicate<in T>): Predicate<T> {
        return PredicateSAM { test(it) || predicate.test(it) }
    }
}
