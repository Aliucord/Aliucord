package com.discord.utilities.rx;

import android.content.Context;

import com.discord.utilities.error.Error;

import kotlin.Unit;
import kotlin.jvm.functions.Function0;
import kotlin.jvm.functions.Function1;
import rx.Observable;
import rx.Subscription;

@SuppressWarnings("unused")
public final class ObservableExtensionsKt {
    public static <T> void appSubscribe$default(
            Observable<T> observable,
            Context context,
            String errorTag,
            Function1<? extends Subscription, Unit> onSubscription,
            Function1<? super T, Unit> onNext,
            Function1<? super Error, Unit> onError,
            Function0<Unit> onCompleted,
            Function0<Unit> onTerminated,
            int flags,
            Object obj
    ) {}
}
