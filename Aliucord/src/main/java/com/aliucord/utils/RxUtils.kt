/*
 * This file is part of Aliucord, an Android Discord client mod.
 * Copyright (c) 2021 Juby210 & Vendicated
 * Licensed under the Open Software License version 3.0
 */

@file:Suppress("unused")
@file:JvmName("RxUtils")
package com.aliucord.utils

import android.os.Looper
import android.util.Pair
import rx.Observable
import rx.Subscriber
import rx.Subscription
import rx.functions.Action0
import rx.functions.Action1
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicReference

fun <T> Observable<T>.onBackpressureBuffer(): Observable<T> = J()

/** Subscribe to the [Observable] */
fun <T> Observable<T>.subscribe(subscriber: Subscriber<in T>): Subscription = T(subscriber)

/**
 * Blocks the current thread and waits for the [Observable] to complete, then returns a [Pair] containing the result, and the error (if any)
 * This must not be called from the Main thread (and will throw an [IllegalStateException] if done so) as that would freeze the UI
 * @return A [Pair] whose first value is the result and whose second value is the error that occurred, if any
 */
@Throws(IllegalStateException::class)
fun <T> Observable<T>.getResultBlocking(): Pair<T?, Throwable?> {
  if (Looper.getMainLooper() == Looper.myLooper()) throw IllegalStateException("getResultBlocking may not be called from the main thread as this would freeze the UI.")

  val latch = CountDownLatch(1)
  val resRef = AtomicReference<T?>()
  val throwableRef = AtomicReference<Throwable?>()

  subscribe(object : Subscriber<T>() {
    override fun onCompleted() = latch.countDown()

    override fun onError(th: Throwable) {
      throwableRef.set(th)
      latch.countDown()
    }

    override fun onNext(value: T) = resRef.set(value)
  })

  if (latch.count != 0L) try {
    latch.await()
  } catch (ignored: InterruptedException) {}

  return Pair(resRef.get(), throwableRef.get())
}

/**
 * Creates a subscriber that forwards the onXXX method calls to callbacks.
 *
 * @param onNext An Observable calls this method whenever the Observable emits an item. This method takes as a parameter the item emitted by the Observable.
 * @param onError An Observable calls this method to indicate that it has failed to generate the expected data or has encountered some other error.
 * @param onCompleted An Observable calls this method after it has called [onNext] for the final time, if it has not encountered any errors.
 * @return ActionSubscriber instance
 * @see [ActionSubscriber on github](https://github.com/ReactiveX/RxJava/blob/1.x/src/main/java/rx/internal/util/ActionSubscriber.java)
 */
@JvmOverloads
fun <T> createActionSubscriber(
  onNext: Action1<in T>,
  onError: Action1<Throwable> = Action1 {},
  onCompleted: Action0 = Action0 {}
): Subscriber<T> = j0.l.e.b(onNext, onError, onCompleted)

/**
 * Returns an [Observable] that emits `0L` after a specified delay, and then completes.
 *
 * @param delay the initial delay before emitting a single `0L`
 * @param unit time units to use for [delay]
 * @return an [Observable] that emits one item after a specified delay, and then completes
 * @see [ReactiveX operators documentation: Timer](http://reactivex.io/documentation/operators/timer.html)
 */
fun timer(delay: Long, unit: TimeUnit?): Observable<Long> = Observable.d0(delay, unit, j0.p.a.a())
