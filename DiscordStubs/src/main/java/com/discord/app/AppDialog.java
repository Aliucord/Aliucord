package com.discord.app;

import android.os.Bundle;
import android.view.View;

import androidx.annotation.CallSuper;
import androidx.annotation.LayoutRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.FragmentManager;

import rx.subjects.Subject;

@SuppressWarnings("unused")
public abstract class AppDialog extends DialogFragment implements AppComponent {
    public AppDialog() {}
    public AppDialog(@LayoutRes int id) {}

    @Override
    public void show(@NonNull FragmentManager fragmentManager, String s) {}

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {}

    @CallSuper
    public void onViewBound(View view) {}

    @CallSuper
    public void onViewBoundOrOnResume() {}

    @Override
    public Subject<Void, Void> getUnsubscribeSignal() { return null; }
}
