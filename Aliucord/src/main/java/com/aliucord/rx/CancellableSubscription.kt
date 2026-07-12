package com.aliucord.rx

import com.aliucord.Logger
import rx.Subscription
import rx.functions.Cancellable
import java.util.concurrent.atomic.AtomicReference

class CancellableSubscription(cancellable: Cancellable?) : AtomicReference<Cancellable?>(cancellable), Subscription {
    val logger = Logger("CancellableSubscription")

    override fun isUnsubscribed(): Boolean {
        return get() == null
    }

    override fun unsubscribe() {
        if (this.get() != null) {
            try {
                this.getAndSet(null)?.cancel()
            } catch (e: Exception) {
                logger.error(e)
            }
        }
    }
}
