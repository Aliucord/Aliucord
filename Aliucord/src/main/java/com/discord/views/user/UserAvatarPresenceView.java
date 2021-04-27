package com.discord.views.user;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.RelativeLayout;

import com.discord.views.StatusView;

import c.a.h.g1;

@SuppressWarnings("unused")
public final class UserAvatarPresenceView extends RelativeLayout {
    public g1 h;

    public UserAvatarPresenceView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    private StatusView getStatusView() { return null; }
    public final void setAvatarBackgroundColor(int color) {}
}
