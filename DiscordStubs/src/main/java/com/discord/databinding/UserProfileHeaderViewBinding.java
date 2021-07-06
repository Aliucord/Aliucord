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
    /** avatar edit */
    public CardView b;
    /** banner */
    public SimpleDraweeView c;
    /** banner edit */
    public CardView d;
    /** betaTag */
    public CardView e;
    /** avatar */
    public UserAvatarPresenceView f;
    /** badges */
    public RecyclerView g;
    /** custom status */
    public SimpleDraweeSpanTextView h;
    /** username */
    public UsernameView i;
    /** secondary name */
    public SimpleDraweeSpanTextView j;

    @NonNull
    @Override
    public View getRoot() {
        return a;
    }
}
