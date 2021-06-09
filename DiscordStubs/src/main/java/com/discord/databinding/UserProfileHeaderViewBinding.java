package com.discord.databinding;

import android.view.View;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewbinding.ViewBinding;

import com.discord.utilities.view.text.SimpleDraweeSpanTextView;
import com.discord.views.UsernameView;
import com.discord.views.user.UserAvatarPresenceView;
import com.facebook.drawee.view.SimpleDraweeView;

@SuppressWarnings("unused")
public final class UserProfileHeaderViewBinding implements ViewBinding {
    /** root */
    public ConstraintLayout a;
    /** banner */
    public SimpleDraweeView b;
    /** betaTag */
    public CardView c;
    /** avatar */
    public UserAvatarPresenceView d;
    /** badges */
    public RecyclerView e;
    /** custom status */
    public SimpleDraweeSpanTextView f;
    /** username */
    public UsernameView g;
    /** secondary name */
    public SimpleDraweeSpanTextView h;

    @NonNull
    @Override
    public View getRoot() {
        return a;
    }
}
