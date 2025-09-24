/*
 * This file is part of Aliucord, an Android Discord client mod.
 * Copyright (c) 2021 Juby210 & Vendicated
 * Licensed under the Open Software License version 3.0
 */

package com.aliucord.utils

import android.os.Looper
import j0.l.a.c
import rx.*
import rx.functions.*
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicReference

@Suppress("unused")
object RxUtils {
    /**
     * Instructs an Observable that is emitting items faster than its observer can consume them to buffer these
     * items indefinitely until they can be emitted.
     *
     * [preview image](https://raw.github.com/wiki/ReactiveX/RxJava/images/rx-operators/bp.obp.buffer.png "preview image")
     *
     * **`Backpressure`**:
     *
     * The operator honors backpressure from downstream and consumes the source [Observable] in an unbounded
     * manner (i.e., not applying backpressure to it).
     *
     * **`Scheduler`**:
     *
     * [onBackpressureBuffer] does not operate by default on a particular [Scheduler].
     *
     * @return the source [Observable] modified to buffer items to the extent system resources allow
     * @see [ReactiveX operators documentation: backpressure operators](http://reactivex.io/documentation/operators/backpressure.html)
     */
    @JvmStatic
    fun <T> Observable<T>.onBackpressureBuffer(): Observable<T> = K()

    /**
     * Subscribe to the [Observable]
     *
     * @param subscriber the [Subscriber] that will handle emissions and notifications from the Observable
     * @return created [Subscription]
     * @see [ReactiveX operators documentation: Subscribe](http://reactivex.io/documentation/operators/subscribe.html)
     */
    @JvmStatic
    fun <T> Observable<T>.subscribe(subscriber: Subscriber<in T>): Subscription = this.U(subscriber)

    /**
     * Subscribe to the [Observable]. This is equivalent to subscribe(createActionSubscriber(onNext))
     *
     * @param onNext the callback that will be fired once the Observable emits onNext
     * @return created [Subscription]
     * @see [ReactiveX operators documentation: Subscribe](http://reactivex.io/documentation/operators/subscribe.html)
     */
    @JvmStatic
    fun <T> Observable<T>.subscribe(onNext: T.() -> Unit) = subscribe(createActionSubscriber(onNext))

    /**
     * Blocks the current thread and waits for the [Observable] to complete, then returns a [Pair] containing the result, and the error (if any)
     * This must not be called from the Main thread (and will throw an [IllegalStateException] if done so) as that would freeze the UI
     * @return A [Pair] whose first value is the result and whose second value is the error that occurred, if any
     */
    @JvmStatic
    @Throws(IllegalStateException::class)
    fun <T> Observable<T>.await(): Pair<T?, Throwable?> {
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

        if (latch.count != 0L)
            latch.await()

        return resRef.get() to throwableRef.get()
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
    @JvmStatic
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
    @JvmStatic
    fun timer(delay: Long, unit: TimeUnit?): Observable<Long> = Observable.e0(delay, unit, j0.p.a.a())

    /**
     * Runs the callback after the specified delay
     *
     * @param delay the delay before running the callback
     * @param unit time unit to use for the [delay]
     * @param callback the callback to run after the delay
     */
    @JvmStatic
    fun schedule(delay: Long, unit: TimeUnit?, callback: Long.() -> Unit) =
        timer(delay, unit).subscribe(callback)

    /**
     * Combines a list of source Observables by emitting an item that aggregates the latest values of each of
     * the source Observables each time an item is received from any of the source Observables, where this
     * aggregation is defined by a specified function.
     *
     * @param T the common base type of source values
     * @param R the result type
     * @param sources the list of source Observables
     * @param combineFunction the aggregation function used to combine the items emitted by the source Observables
     * @return an Observable that emits items that are the result of combining the items emitted by the source
     * Observables by means of the given aggregation function
     * @see [ReactiveX operators documentation: CombineLatest](http://reactivex.io/documentation/operators/combinelatest.html)
     */
    @JvmStatic
    fun <T, R> combineLatest(sources: List<Observable<T>>, combineFunction: FuncN<R>): Observable<R> = Observable.b(sources, combineFunction)

    /**
     * Returns an Observable that applies a specified function to each item emitted by the source Observable and
     * emits the results of these function applications.
     *
     * @param R the output type
     * @param func a function to apply to each item emitted by the Observable
     * @return an Observable that emits the items from the source Observable, transformed by the specified [func]
     * @see [ReactiveX operators documentation: Map](http://reactivex.io/documentation/operators/map.html)
     */
    @JvmStatic
    fun <T, R> Observable<T>.map(func: (T) -> R): Observable<R> = G { func(it) }

    /**
     * Returns a new Observable by applying a function that you supply to each item emitted by the source
     * Observable that returns an Observable, and then emitting the items emitted by the most recently emitted
     * of these Observables.
     *
     * The resulting Observable completes if both the upstream Observable and the last inner Observable, if any, complete.
     * If the upstream Observable signals an onError, the inner Observable is unsubscribed and the error delivered in-sequence.
     *
     * @param R the element type of the inner Observables and the output
     * @param func a function that, when applied to an item emitted by the source Observable, returns an Observable
     * @return an Observable that emits the items emitted by the Observable returned from applying [func] to the most recently
     * emitted item emitted by the source Observable
     * @see [ReactiveX operators documentation: FlatMap](http://reactivex.io/documentation/operators/flatmap.html)
     */
    @JvmStatic
    fun <T, R> Observable<T>.switchMap(func: (T) -> Observable<R>): Observable<R> = Y { func(it) }

    /**
     * Creates a new cold Observable. When the observable is subscribed to, [onSubscribe] is called
     * with the subscriber as a parameter, and should start emitting to the subscriber using [Subscriber.onNext].
     *
     * [onSubscribe] should always end with either [Subscriber.onError] or [Subscriber.onCompleted].
     *
     * Note that the resulting Observable is *not* backpressure-aware.
     *
     * @param T the type of data that the subscriber will receive
     * @param onSubscribe a function that is called when the observable is subscribed to
     * @return a cold Observable
     * @see [Observable.unsafeCreate in RxJava docs](https://reactivex.io/RxJava/javadoc/rx/Observable.html#unsafeCreate-rx.Observable.OnSubscribe-)
     */
    @JvmStatic
    fun <T> create(onSubscribe: (subscriber: Subscriber<in T>) -> Unit): Observable<T> = Observable.h0(onSubscribe)

    @JvmStatic
    val empty: Observable<Any> = c.k
}
