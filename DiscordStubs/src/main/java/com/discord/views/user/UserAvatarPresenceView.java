package com.discord.views.user;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.RelativeLayout;

import com.discord.views.StatusView;

import c.a.i.o1;

@SuppressWarnings("unused")
public final class UserAvatarPresenceView extends RelativeLayout {
    public o1 i;

    public UserAvatarPresenceView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    private StatusView getStatusView() { return null; }
    public final void setAvatarBackgroundColor(int color) {}
}
