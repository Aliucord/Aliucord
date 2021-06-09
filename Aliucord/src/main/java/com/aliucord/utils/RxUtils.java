/*
 * Copyright (c) 2021 Juby210
 * Licensed under the Open Software License version 3.0
 */

package com.aliucord.utils;

import rx.*;
import rx.functions.Action0;
import rx.functions.Action1;

@SuppressWarnings("unused")
public class RxUtils {
    public static <T> Observable<T> onBackpressureBuffer(Observable<T> observable) {
        return observable.K();
    }

    public static <T> Subscription subscribe(Observable<T> observable, Subscriber<? super T> subscriber) {
        return observable.V(subscriber);
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
