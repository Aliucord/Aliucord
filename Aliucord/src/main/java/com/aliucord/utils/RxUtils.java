/*
 * Copyright (c) 2021 Juby210
 * Licensed under the Open Software License version 3.0
 */

package com.aliucord.utils;

import android.os.Looper;
import android.util.Pair;

import rx.Observable;
import rx.Subscriber;
import rx.Subscription;
import rx.functions.Action0;
import rx.functions.Action1;

@SuppressWarnings("unused")
public class RxUtils {
    public static <T> Observable<T> onBackpressureBuffer(Observable<T> observable) {
        return observable.K();
    }

    public static <T> Subscription subscribe(Observable<T> observable, Subscriber<? super T> subscriber) {
        return observable.U(subscriber);
    }

    /**
     * Blocks the current thread and waits for the observable to complete, then returns a Pair containing the result, and the error (if any)
     * This must not be called from the Main thread (and will throw an IllegalStateException if done so) as that would freeze the UI
     * @param observable The observable to wait for
     * @return A pair whose first value is the result and whose second value is the error that occurred, if any
     */
    public static <T> Pair<T, Throwable> getResultBlocking(Observable<T> observable) throws IllegalStateException {
        if (Looper.getMainLooper() == Looper.myLooper()) throw new IllegalStateException("getResultBlocking may not be called from the main thread as this would freeze the UI.");

        Object lock = new Object();
        final Object[] result = new Object[3];

        subscribe(observable, new Subscriber<T>() {
            public void onCompleted() {
                result[2] = true; // see comment below
                synchronized (lock) {
                    lock.notify();
                }
            }
            public void onError(Throwable th) {
                result[1] = th;
                result[2] = true; // see comment below
                synchronized (lock) {
                    lock.notify();
                }
            }
            public void onNext(T val) {
                result[0] = val;
            }
        });

        // Sometimes onCompleted is reached before this point so lock.notify gets called before lock.wait resulting in an infinite lock
        // so check whether finished already
        if (result[2] != Boolean.TRUE) {
            synchronized (lock) {
                try {
                    lock.wait();
                } catch (InterruptedException ignored) { }
            }
        }

        T res;
        try {
            res = (T) result[0];
        } catch (Throwable ignored) { res = null; }
        return new Pair<>(res, (Throwable) result[1]);
    }

    /**
     * Creates a subscriber that forwards the onXXX method calls to callbacks.
     *
     * @param onNext An Observable calls this method whenever the Observable emits an item. This method takes as a parameter the item emitted by the Observable.
     * @return ActionSubscriber instance
     * @see <a href="https://github.com/ReactiveX/RxJava/blob/1.x/src/main/java/rx/internal/util/ActionSubscriber.java">ActionSubscriber on github</a>
     */
    public static <T> Subscriber<T> createActionSubscriber(Action1<? super T> onNext) {
        return createActionSubscriber(onNext, null, null);
    }

    /**
     * Creates a subscriber that forwards the onXXX method calls to callbacks.
     *
     * @param onNext An Observable calls this method whenever the Observable emits an item. This method takes as a parameter the item emitted by the Observable.
     * @param onError An Observable calls this method to indicate that it has failed to generate the expected data or has encountered some other error.
     * @param onCompleted An Observable calls this method after it has called <code>onNext</code> for the final time, if it has not encountered any errors.
     * @return ActionSubscriber instance
     * @see <a href="https://github.com/ReactiveX/RxJava/blob/1.x/src/main/java/rx/internal/util/ActionSubscriber.java">ActionSubscriber on github</a>
     */
    public static <T> Subscriber<T> createActionSubscriber(Action1<? super T> onNext, Action1<Throwable> onError, Action0 onCompleted) {
        return new j0.l.e.b<>(onNext, onError == null ? e -> {} : onError, onCompleted == null ? () -> {} : onCompleted);
    }
}
