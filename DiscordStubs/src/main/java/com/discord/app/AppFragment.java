package com.discord.app;

import android.content.Intent;
import android.view.View;

import androidx.annotation.CallSuper;
import androidx.annotation.LayoutRes;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;

import kotlin.Unit;
import rx.subjects.Subject;

@SuppressWarnings({"unused"})
public abstract class AppFragment extends Fragment implements AppComponent {
    public AppFragment() {}
    public AppFragment(@LayoutRes int id) {}

    @CallSuper
    public void onViewBound(View view) {}

    @CallSuper
    public void onViewBoundOrOnResume() {}

    public final Unit setActionBarTitle(int id) { return null; }
    public final Unit setActionBarTitle(CharSequence text) { return null; }
    public final Unit setActionBarSubtitle(int id) { return null; }
    public final Unit setActionBarSubtitle(CharSequence text) { return null; }
    public final Toolbar setActionBarDisplayHomeAsUpEnabled() { return null; }

    public final AppActivity getAppActivity() { return null; }
    public final Intent getMostRecentIntent() { return null; }

    @Override
    public Subject<Void, Void> getUnsubscribeSignal() { return null; }
}
