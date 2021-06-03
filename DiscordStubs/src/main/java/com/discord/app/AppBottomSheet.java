package com.discord.app;

import android.os.Bundle;
import android.view.View;

import androidx.annotation.LayoutRes;
import androidx.fragment.app.FragmentManager;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import rx.subjects.Subject;

@SuppressWarnings({"unused", "NullableProblems"})
public abstract class AppBottomSheet extends BottomSheetDialogFragment implements AppComponent {
    @LayoutRes
    public abstract int getContentViewResId();

    public void show(FragmentManager fragmentManager, String s) {}
    public void onViewCreated(View view, Bundle bundle) {}

    @Override
    public Subject<Void, Void> getUnsubscribeSignal() { return null; }
}
