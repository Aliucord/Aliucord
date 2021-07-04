package rx;

/** https://github.com/ReactiveX/RxJava/blob/1.x/src/main/java/rx/Observable.java */
@SuppressWarnings("unused")
public class Observable<T> {
    /** onBackpressureBuffer */
    public final Observable<T> K() { return this; }
    /** subscribe */
    public final Subscription U(Subscriber<? super T> subscriber) { return subscriber; }
}
