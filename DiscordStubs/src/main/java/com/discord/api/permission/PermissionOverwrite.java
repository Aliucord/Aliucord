package com.discord.api.permission;

@SuppressWarnings("unused")
public class PermissionOverwrite {
    public enum Type {
        ROLE,
        MEMBER
    }
    public PermissionOverwrite(long id, Type type, long allowed, long denied) { }

    /**
     * getId
     * @deprecated Do not use this directly, use {@link com.aliucord.wrappers.PermissionOverwriteWrapper} instead.
     */
    @Deprecated
    public final long a() { return 0; }

    /**
     * getAllowed
     * @deprecated Do not use this directly, use {@link com.aliucord.wrappers.PermissionOverwriteWrapper} instead.
     */
    @Deprecated
    public final long c() { return 0; }

    /**
     * getDenied
     * @deprecated Do not use this directly, use {@link com.aliucord.wrappers.PermissionOverwriteWrapper} instead.
     */
    @Deprecated
    public final long d() { return 0; }

    /**
     * getId 2: Electric Boogaloo
     * @deprecated Do not use this directly, use {@link com.aliucord.wrappers.PermissionOverwriteWrapper} instead.
     */
    @Deprecated
    public final long e() { return 0; }

    /**
     * getType
     * @deprecated Do not use this directly, use {@link com.aliucord.wrappers.PermissionOverwriteWrapper} instead.
     */
    @Deprecated
    public final Type f() { return Type.ROLE; }
}
