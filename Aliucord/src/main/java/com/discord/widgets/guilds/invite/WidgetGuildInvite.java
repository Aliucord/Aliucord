package com.discord.widgets.guilds.invite;

import android.content.Context;

import com.discord.app.AppFragment;

@SuppressWarnings("unused")
public final class WidgetGuildInvite extends AppFragment {
    public static final class Companion {
        public final void launch(Context context, String code, String location) {}
    }

    public static final Companion Companion = new Companion();
}
