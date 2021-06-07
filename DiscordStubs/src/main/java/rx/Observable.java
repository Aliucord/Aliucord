package rx;

/** https://github.com/ReactiveX/RxJava/blob/1.x/src/main/java/rx/Observable.java */
@SuppressWarnings("unused")
public class Observable<T> {
    /** onBackpressureBuffer */
    public final Observable<T> I() { return this; }
    /** subscribe */
    public final Subscription T(Subscriber<? super T> subscriber) { return subscriber; }
}
