package com.discord.app;

import rx.subjects.Subject;

public interface AppComponent {
    Subject<Void, Void> getUnsubscribeSignal();
}
