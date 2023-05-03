/*
 * Copyright (c) 2021 Juby210 & Vendicated
 * Licensed under the Open Software License version 3.0
 */

package com.aliucord.fragments;

import androidx.fragment.app.Fragment;

import com.discord.app.AppActivity;

public class AppFragmentProxy extends FragmentProxy {
    private Fragment mFragment;

    @Override
    public Fragment getmFragment() {
        if (mFragment == null) {
            var activity = (AppActivity) getActivity();
            if (activity != null) {
                String id = activity.c().getStringExtra("AC_FRAGMENT_ID");
                mFragment = FragmentProxy.fragments.get(id);
                FragmentProxy.fragments.remove(id);
            }
        }
        if (mFragment != null) return mFragment;
        return super.getmFragment();
    }
}
