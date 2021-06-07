package j0.l.e;

import rx.Subscriber;
import rx.functions.Action0;
import rx.functions.Action1;

/** rx.internal.util.ActionSubscriber */
/** https://github.com/ReactiveX/RxJava/blob/1.x/src/main/java/rx/internal/util/ActionSubscriber.java */
@SuppressWarnings("unused")
public final class b<T> extends Subscriber<T> {
    public b(Action1<? super T> onNext, Action1<Throwable> onError, Action0 onCompleted) {}

    @Override
    public void onCompleted() {}

    @Override
    public void onError(Throwable th) {}

    @Override
    public void onNext(T t) {}
}
