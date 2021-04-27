package rx;

import i0.g;

@SuppressWarnings("unused")
public abstract class Subscriber<T> implements g<T>, Subscription {
    public boolean isUnsubscribed() { return false; }
    public void unsubscribe() {}
}
