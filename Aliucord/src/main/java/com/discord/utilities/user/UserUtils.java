package com.discord.utilities.user;

import com.discord.models.user.CoreUser;
import com.discord.models.user.User;

@SuppressWarnings("unused")
public final class UserUtils {
    public static final UserUtils INSTANCE = new UserUtils();

    public final User getEMPTY_USER() { return new CoreUser(); }
    public final String getDiscriminatorWithPadding(User user) { return ""; }
    public final CharSequence getUserNameWithDiscriminator(User user, Integer color, Float size) { return ""; }
}
