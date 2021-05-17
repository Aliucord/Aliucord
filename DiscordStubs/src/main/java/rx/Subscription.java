package rx;

@SuppressWarnings("unused")
public interface Subscription {
    boolean isUnsubscribed();
    void unsubscribe();
}
