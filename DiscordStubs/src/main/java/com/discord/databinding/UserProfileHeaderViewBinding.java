package com.discord.databinding;

import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewbinding.ViewBinding;

import com.discord.utilities.view.text.SimpleDraweeSpanTextView;
import com.discord.views.UsernameView;
import com.discord.views.user.UserAvatarPresenceView;

public final class UserProfileHeaderViewBinding implements ViewBinding {
    /** root */
    public ConstraintLayout a;
    /** avatar */
    public UserAvatarPresenceView b;
    /** badges */
    public RecyclerView c;
    /** custom status */
    public SimpleDraweeSpanTextView d;
    /** username */
    public UsernameView e;
    /** secondary name */
    public TextView f;

    @NonNull
    @Override
    public View getRoot() {
        return a;
    }
}
