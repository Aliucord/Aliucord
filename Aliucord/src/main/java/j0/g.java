package j0;

// rx.Observer
// https://github.com/ReactiveX/RxJava/blob/1.x/src/main/java/rx/Observer.java
@SuppressWarnings("unused")
public interface g<T> {
    void onCompleted();
    void onError(Throwable th);
    void onNext(T t);
}
