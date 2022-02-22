/*
 * Copyright (c) 2021 Juby210 & Vendicated
 * Licensed under the Open Software License version 3.0
 */

package com.aliucord.fragments;

import android.animation.Animator;
import android.app.Activity;
import android.content.*;
import android.content.res.Configuration;
import android.os.Bundle;
import android.util.AttributeSet;
import android.view.*;
import android.view.animation.Animation;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.SharedElementCallback;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.*;
import androidx.loader.app.LoaderManager;

import com.aliucord.Main;
import com.aliucord.Utils;
import com.discord.app.AppComponent;

import java.io.FileDescriptor;
import java.io.PrintWriter;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

import rx.subjects.Subject;

@SuppressWarnings({"CommentedOutCode", "deprecation"})
public class FragmentProxy extends Fragment implements AppComponent {
    public static final Map<String, Fragment> fragments = new HashMap<>();

    private Fragment mFragment;

//    @Override
//    public void onViewBound(View view) {
//        if (mFragment != null) mFragment.onViewBound(view);
//        super.onViewBound(view);
//    }

    @Override
    public Subject<Void, Void> getUnsubscribeSignal() {
        Fragment fragment = getmFragment();
        if (fragment instanceof AppComponent) return ((AppComponent) fragment).getUnsubscribeSignal();
        return null;
    }

    @NonNull
    @Override
    public Lifecycle getLifecycle() {
        return getmFragment().getLifecycle();
    }

    @NonNull
    @Override
    public LifecycleOwner getViewLifecycleOwner() {
        return getmFragment().getViewLifecycleOwner();
    }

    @NonNull
    @Override
    public LiveData<LifecycleOwner> getViewLifecycleOwnerLiveData() {
        return getmFragment().getViewLifecycleOwnerLiveData();
    }

    @NonNull
    @Override
    public ViewModelStore getViewModelStore() {
        return super.getViewModelStore();
    }

    @Override
    public void setArguments(@Nullable Bundle args) {
        getmFragment().setArguments(args);
    }

    @Override
    public void setInitialSavedState(@Nullable SavedState state) {
        getmFragment().setInitialSavedState(state);
    }

    @Override
    public void setTargetFragment(@Nullable Fragment fragment, int requestCode) {
        getmFragment().setTargetFragment(fragment, requestCode);
    }

    @Nullable
    @Override
    public Context getContext() {
        return getmFragment().getContext();
    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        getmFragment().onHiddenChanged(hidden);
    }

    @Override
    public void setRetainInstance(boolean retain) {
        getmFragment().setRetainInstance(retain);
    }

    @Override
    public void setHasOptionsMenu(boolean hasMenu) {
        getmFragment().setHasOptionsMenu(hasMenu);
    }

    @Override
    public void setMenuVisibility(boolean menuVisible) {
        getmFragment().setMenuVisibility(menuVisible);
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        getmFragment().setUserVisibleHint(isVisibleToUser);
    }

    @Override
    public boolean getUserVisibleHint() {
        return getmFragment().getUserVisibleHint();
    }

    @NonNull
    @Override
    public LoaderManager getLoaderManager() {
        return getmFragment().getLoaderManager();
    }

    @Override
    public void startActivity(Intent intent) {
        getmFragment().startActivity(intent);
    }

    @Override
    public void startActivity(Intent intent, @Nullable Bundle options) {
        getmFragment().startActivity(intent, options);
    }

    @Override
    public void startActivityForResult(Intent intent, int requestCode) {
        getmFragment().startActivityForResult(intent, requestCode);
    }

    @Override
    public void startActivityForResult(Intent intent, int requestCode, @Nullable Bundle options) {
        getmFragment().startActivityForResult(intent, requestCode, options);
    }

    @Override
    public void startIntentSenderForResult(IntentSender intent, int requestCode, @Nullable Intent fillInIntent, int flagsMask, int flagsValues, int extraFlags, @Nullable Bundle options) throws IntentSender.SendIntentException {
        getmFragment().startIntentSenderForResult(intent, requestCode, fillInIntent, flagsMask, flagsValues, extraFlags, options);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        getmFragment().onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        getmFragment().onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    @Override
    public boolean shouldShowRequestPermissionRationale(@NonNull String permission) {
        return getmFragment().shouldShowRequestPermissionRationale(permission);
    }

//    @NonNull
//    @Override
//    public LayoutInflater onGetLayoutInflater(@Nullable Bundle savedInstanceState) {
////        return mFragment.onGetLayoutInflater(savedInstanceState);
//        try {
//            return super.onGetLayoutInflater(savedInstanceState);
//        } catch (Exception e) {
//            return null;
//        }
//    }
//
//    @NonNull
//    @Override
//    @SuppressLint("RestrictedApi")
//    public LayoutInflater getLayoutInflater(@Nullable Bundle savedFragmentState) {
//        return mFragment.getLayoutInflater(savedFragmentState);
//    }

    @Override
    public void onInflate(@NonNull Context context, @NonNull AttributeSet attrs, @Nullable Bundle savedInstanceState) {
        getmFragment().onInflate(context, attrs, savedInstanceState);
        super.onInflate(context, attrs, savedInstanceState);
    }

    @Override
    public void onInflate(@NonNull Activity activity, @NonNull AttributeSet attrs, @Nullable Bundle savedInstanceState) {
        getmFragment().onInflate(activity, attrs, savedInstanceState);
        super.onInflate(activity, attrs, savedInstanceState);
    }

    @Override
    public void onAttachFragment(@NonNull Fragment childFragment) {
        getmFragment().onAttachFragment(childFragment);
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        getmFragment().onAttach(context);
    }

    @Override
    public void onAttach(@NonNull Activity activity) {
        super.onAttach(activity);
        getmFragment().onAttach(activity);
    }

    @Nullable
    @Override
    public Animation onCreateAnimation(int transit, boolean enter, int nextAnim) {
        return getmFragment().onCreateAnimation(transit, enter, nextAnim);
    }

    @Nullable
    @Override
    public Animator onCreateAnimator(int transit, boolean enter, int nextAnim) {
        return getmFragment().onCreateAnimator(transit, enter, nextAnim);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        if (mFragment != null) mFragment.onCreate(savedInstanceState);
        super.onCreate(savedInstanceState);
    }

    private View mView;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        Fragment fragment = getmFragment();
        try {
            Field field = Fragment.class.getDeclaredField("mFragmentManager");
            field.setAccessible(true);
            field.set(fragment, getFragmentManager());
            Field hostField = Fragment.class.getDeclaredField("mHost");
            hostField.setAccessible(true);
            hostField.set(fragment, hostField.get(this));
        } catch (Exception e) { Main.logger.error(e); }
        mView = fragment.onCreateView(inflater, container, savedInstanceState);
        return mView;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        getmFragment().onViewCreated(view, savedInstanceState);
    }

    @Nullable
    @Override
    public View getView() {
        View v = getmFragment().getView();
        if (v == null) return mView;
        return v;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        getmFragment().onActivityCreated(savedInstanceState);
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public void onViewStateRestored(@Nullable Bundle savedInstanceState) {
        getmFragment().onViewStateRestored(savedInstanceState);
        super.onViewStateRestored(savedInstanceState);
    }

    @Override
    public void onStart() {
        getmFragment().onStart();
        super.onStart();
    }

    @Override
    public void onResume() {
        getmFragment().onResume();
        super.onResume();
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        getmFragment().onSaveInstanceState(outState);
    }

    @Override
    public void onMultiWindowModeChanged(boolean isInMultiWindowMode) {
        getmFragment().onMultiWindowModeChanged(isInMultiWindowMode);
    }

    @Override
    public void onPictureInPictureModeChanged(boolean isInPictureInPictureMode) {
        getmFragment().onPictureInPictureModeChanged(isInPictureInPictureMode);
    }

    @Override
    public void onConfigurationChanged(@NonNull Configuration newConfig) {
        getmFragment().onConfigurationChanged(newConfig);
        super.onConfigurationChanged(newConfig);
    }

    @Override
    public void onPrimaryNavigationFragmentChanged(boolean isPrimaryNavigationFragment) {
        getmFragment().onPrimaryNavigationFragmentChanged(isPrimaryNavigationFragment);
    }

    @Override
    public void onPause() {
        getmFragment().onPause();
        super.onPause();
    }

    @Override
    public void onStop() {
        getmFragment().onStop();
        super.onStop();
    }

    @Override
    public void onLowMemory() {
        getmFragment().onLowMemory();
        super.onLowMemory();
    }

    @Override
    public void onDestroyView() {
        getmFragment().onDestroyView();
        super.onDestroyView();
    }

    @Override
    public void onDestroy() {
        getmFragment().onDestroy();
        super.onDestroy();
    }

    @Override
    public void onDetach() {
        getmFragment().onDetach();
        super.onDetach();
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        getmFragment().onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public void onPrepareOptionsMenu(@NonNull Menu menu) {
        getmFragment().onPrepareOptionsMenu(menu);
    }

    @Override
    public void onDestroyOptionsMenu() {
        getmFragment().onDestroyOptionsMenu();
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        return getmFragment().onOptionsItemSelected(item);
    }

    @Override
    public void onOptionsMenuClosed(@NonNull Menu menu) {
        getmFragment().onOptionsMenuClosed(menu);
    }

    @Override
    public void onCreateContextMenu(@NonNull ContextMenu menu, @NonNull View v, @Nullable ContextMenu.ContextMenuInfo menuInfo) {
        getmFragment().onCreateContextMenu(menu, v, menuInfo);
    }

    @Override
    public void registerForContextMenu(@NonNull View view) {
        getmFragment().registerForContextMenu(view);
    }

    @Override
    public void unregisterForContextMenu(@NonNull View view) {
        getmFragment().unregisterForContextMenu(view);
    }

    @Override
    public boolean onContextItemSelected(@NonNull MenuItem item) {
        return getmFragment().onContextItemSelected(item);
    }

    @Override
    public void setEnterSharedElementCallback(@Nullable SharedElementCallback callback) {
        getmFragment().setEnterSharedElementCallback(callback);
    }

    @Override
    public void setExitSharedElementCallback(@Nullable SharedElementCallback callback) {
        getmFragment().setExitSharedElementCallback(callback);
    }

    @Override
    public void setEnterTransition(@Nullable Object transition) {
        getmFragment().setEnterTransition(transition);
    }

    @Nullable
    @Override
    public Object getEnterTransition() {
        return getmFragment().getEnterTransition();
    }

    @Override
    public void setReturnTransition(@Nullable Object transition) {
        getmFragment().setReturnTransition(transition);
    }

    @Nullable
    @Override
    public Object getReturnTransition() {
        return getmFragment().getReturnTransition();
    }

    @Override
    public void setExitTransition(@Nullable Object transition) {
        getmFragment().setExitTransition(transition);
    }

    @Nullable
    @Override
    public Object getExitTransition() {
        return getmFragment().getExitTransition();
    }

    @Override
    public void setReenterTransition(@Nullable Object transition) {
        getmFragment().setReenterTransition(transition);
    }

    @Nullable
    @Override
    public Object getReenterTransition() {
        return getmFragment().getReenterTransition();
    }

    @Override
    public void setSharedElementEnterTransition(@Nullable Object transition) {
        getmFragment().setSharedElementEnterTransition(transition);
    }

    @Nullable
    @Override
    public Object getSharedElementEnterTransition() {
        return getmFragment().getSharedElementEnterTransition();
    }

    @Override
    public void setSharedElementReturnTransition(@Nullable Object transition) {
        getmFragment().setSharedElementReturnTransition(transition);
    }

    @Nullable
    @Override
    public Object getSharedElementReturnTransition() {
        return getmFragment().getSharedElementReturnTransition();
    }

    @Override
    public void setAllowEnterTransitionOverlap(boolean allow) {
        getmFragment().setAllowEnterTransitionOverlap(allow);
    }

    @Override
    public boolean getAllowEnterTransitionOverlap() {
        return getmFragment().getAllowEnterTransitionOverlap();
    }

    @Override
    public void setAllowReturnTransitionOverlap(boolean allow) {
        getmFragment().setAllowReturnTransitionOverlap(allow);
    }

    @Override
    public boolean getAllowReturnTransitionOverlap() {
        return getmFragment().getAllowReturnTransitionOverlap();
    }

    @Override
    public void postponeEnterTransition() {
        getmFragment().postponeEnterTransition();
    }

    @Override
    public void startPostponedEnterTransition() {
        getmFragment().startPostponedEnterTransition();
    }

    @Override
    public void dump(@NonNull String prefix, @Nullable FileDescriptor fd, @NonNull PrintWriter writer, @Nullable String[] args) {
        getmFragment().dump(prefix, fd, writer, args);
    }

    private boolean didHack = false;
    public Fragment getmFragment() {
        if (mFragment == null) {
            Bundle bundle = getArguments();
            if (bundle != null) {
                String id = bundle.getString("AC_FRAGMENT_ID");
                mFragment = fragments.get(id);
                fragments.remove(id);
            }
        }

        // Horrible hack but hey it is better than crash
        if (mFragment == null) {
            if (!didHack) {
                didHack = true;
                Utils.getAppActivity().onBackPressed();
            }
            return new Fragment();
        }

        return mFragment;
    }
}

