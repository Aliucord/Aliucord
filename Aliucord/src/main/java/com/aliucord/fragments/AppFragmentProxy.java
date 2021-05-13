package com.aliucord.fragments;

import androidx.fragment.app.Fragment;

import com.discord.app.AppActivity;

public class AppFragmentProxy extends FragmentProxy {
    private Fragment mFragment;

    @Override
    public Fragment getmFragment() {
        if (mFragment == null) {
            String id = ((AppActivity) getActivity()).d().getStringExtra("AC_FRAGMENT_ID");
            mFragment = FragmentProxy.fragments.get(id);
            FragmentProxy.fragments.remove(id);
        }
        if (mFragment != null) return mFragment;
        return super.getmFragment();
    }
}
