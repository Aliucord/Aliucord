package com.discord.models.domain.emoji;

import c.d.b.a.a;
import com.discord.api.user.User;
import com.discord.models.domain.ModelAuditLogEntry;
import d0.b0.d.m;
import java.util.List;
import kotlin.Metadata;

@Metadata(bv = {1, 0, 3}, d1 = {"\u00008\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0002\u0010\t\n\u0002\b\u0002\n\u0002\u0010\u000e\n\u0002\b\u0002\n\u0002\u0010\u000b\n\u0002\b\u0002\n\u0002\u0010 \n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0002\b\u000f\n\u0002\u0010\b\n\u0002\b\u0015\b\b\u0018\u00002\u00020\u0001BM\u0012\u0006\u0010\u0014\u001a\u00020\u0002\u0012\u0006\u0010\u0015\u001a\u00020\u0005\u0012\u0006\u0010\u0016\u001a\u00020\b\u0012\f\u0010\u0017\u001a\b\u0012\u0004\u0012\u00020\u00020\u000b\u0012\u0006\u0010\u0018\u001a\u00020\b\u0012\u0006\u0010\u0019\u001a\u00020\u000f\u0012\u0006\u0010\u001a\u001a\u00020\b\u0012\u0006\u0010\u001b\u001a\u00020\b¢\u0006\u0004\b2\u00103J\u0010\u0010\u0003\u001a\u00020\u0002HÆ\u0003¢\u0006\u0004\b\u0003\u0010\u0004J\u0010\u0010\u0006\u001a\u00020\u0005HÆ\u0003¢\u0006\u0004\b\u0006\u0010\u0007J\u0010\u0010\t\u001a\u00020\bHÆ\u0003¢\u0006\u0004\b\t\u0010\nJ\u0016\u0010\f\u001a\b\u0012\u0004\u0012\u00020\u00020\u000bHÆ\u0003¢\u0006\u0004\b\f\u0010\rJ\u0010\u0010\u000e\u001a\u00020\bHÆ\u0003¢\u0006\u0004\b\u000e\u0010\nJ\u0010\u0010\u0010\u001a\u00020\u000fHÆ\u0003¢\u0006\u0004\b\u0010\u0010\u0011J\u0010\u0010\u0012\u001a\u00020\bHÆ\u0003¢\u0006\u0004\b\u0012\u0010\nJ\u0010\u0010\u0013\u001a\u00020\bHÆ\u0003¢\u0006\u0004\b\u0013\u0010\nJf\u0010\u001c\u001a\u00020\u00002\b\b\u0002\u0010\u0014\u001a\u00020\u00022\b\b\u0002\u0010\u0015\u001a\u00020\u00052\b\b\u0002\u0010\u0016\u001a\u00020\b2\u000e\b\u0002\u0010\u0017\u001a\b\u0012\u0004\u0012\u00020\u00020\u000b2\b\b\u0002\u0010\u0018\u001a\u00020\b2\b\b\u0002\u0010\u0019\u001a\u00020\u000f2\b\b\u0002\u0010\u001a\u001a\u00020\b2\b\b\u0002\u0010\u001b\u001a\u00020\bHÆ\u0001¢\u0006\u0004\b\u001c\u0010\u001dJ\u0010\u0010\u001e\u001a\u00020\u0005HÖ\u0001¢\u0006\u0004\b\u001e\u0010\u0007J\u0010\u0010 \u001a\u00020\u001fHÖ\u0001¢\u0006\u0004\b \u0010!J\u001a\u0010#\u001a\u00020\b2\b\u0010\"\u001a\u0004\u0018\u00010\u0001HÖ\u0003¢\u0006\u0004\b#\u0010$R\u0019\u0010\u0016\u001a\u00020\b8\u0006@\u0006¢\u0006\f\n\u0004\b\u0016\u0010%\u001a\u0004\b&\u0010\nR\u0019\u0010\u0018\u001a\u00020\b8\u0006@\u0006¢\u0006\f\n\u0004\b\u0018\u0010%\u001a\u0004\b'\u0010\nR\u0019\u0010\u0019\u001a\u00020\u000f8\u0006@\u0006¢\u0006\f\n\u0004\b\u0019\u0010(\u001a\u0004\b)\u0010\u0011R\u001f\u0010\u0017\u001a\b\u0012\u0004\u0012\u00020\u00020\u000b8\u0006@\u0006¢\u0006\f\n\u0004\b\u0017\u0010*\u001a\u0004\b+\u0010\rR\u0019\u0010\u001b\u001a\u00020\b8\u0006@\u0006¢\u0006\f\n\u0004\b\u001b\u0010%\u001a\u0004\b,\u0010\nR\u0019\u0010\u0014\u001a\u00020\u00028\u0006@\u0006¢\u0006\f\n\u0004\b\u0014\u0010-\u001a\u0004\b.\u0010\u0004R\u0019\u0010\u0015\u001a\u00020\u00058\u0006@\u0006¢\u0006\f\n\u0004\b\u0015\u0010/\u001a\u0004\b0\u0010\u0007R\u0019\u0010\u001a\u001a\u00020\b8\u0006@\u0006¢\u0006\f\n\u0004\b\u001a\u0010%\u001a\u0004\b1\u0010\n¨\u00064"}, d2 = {"Lcom/discord/models/domain/emoji/ModelEmojiGuild;", "", "", "component1", "()J", "", "component2", "()Ljava/lang/String;", "", "component3", "()Z", "", "component4", "()Ljava/util/List;", "component5", "Lcom/discord/api/user/User;", "component6", "()Lcom/discord/api/user/User;", "component7", "component8", ModelAuditLogEntry.CHANGE_KEY_ID, ModelAuditLogEntry.CHANGE_KEY_NAME, "managed", "roles", "requiredColons", "user", "animated", "available", "copy", "(JLjava/lang/String;ZLjava/util/List;ZLcom/discord/api/user/User;ZZ)Lcom/discord/models/domain/emoji/ModelEmojiGuild;", "toString", "", "hashCode", "()I", "other", "equals", "(Ljava/lang/Object;)Z", "Z", "getManaged", "getRequiredColons", "Lcom/discord/api/user/User;", "getUser", "Ljava/util/List;", "getRoles", "getAvailable", "J", "getId", "Ljava/lang/String;", "getName", "getAnimated", "<init>", "(JLjava/lang/String;ZLjava/util/List;ZLcom/discord/api/user/User;ZZ)V", "app_models_release"}, k = 1, mv = {1, 4, 2})
/* compiled from: ModelEmojiGuild.kt */
public final class ModelEmojiGuild {
    private final boolean animated;
    private final boolean available;

    /* renamed from: id  reason: collision with root package name */
    private final long f2042id;
    private final boolean managed;
    private final String name;
    private final boolean requiredColons;
    private final List<Long> roles;
    private final User user;

    public ModelEmojiGuild(long j, String str, boolean z2, List<Long> list, boolean z3, User user2, boolean z4, boolean z5) {
        m.checkNotNullParameter(str, ModelAuditLogEntry.CHANGE_KEY_NAME);
        m.checkNotNullParameter(list, "roles");
        m.checkNotNullParameter(user2, "user");
        this.f2042id = j;
        this.name = str;
        this.managed = z2;
        this.roles = list;
        this.requiredColons = z3;
        this.user = user2;
        this.animated = z4;
        this.available = z5;
    }

    public static /* synthetic */ ModelEmojiGuild copy$default(ModelEmojiGuild modelEmojiGuild, long j, String str, boolean z2, List list, boolean z3, User user2, boolean z4, boolean z5, int i, Object obj) {
        return modelEmojiGuild.copy((i & 1) != 0 ? modelEmojiGuild.f2042id : j, (i & 2) != 0 ? modelEmojiGuild.name : str, (i & 4) != 0 ? modelEmojiGuild.managed : z2, (i & 8) != 0 ? modelEmojiGuild.roles : list, (i & 16) != 0 ? modelEmojiGuild.requiredColons : z3, (i & 32) != 0 ? modelEmojiGuild.user : user2, (i & 64) != 0 ? modelEmojiGuild.animated : z4, (i & 128) != 0 ? modelEmojiGuild.available : z5);
    }

    public final long component1() {
        return this.f2042id;
    }

    public final String component2() {
        return this.name;
    }

    public final boolean component3() {
        return this.managed;
    }

    public final List<Long> component4() {
        return this.roles;
    }

    public final boolean component5() {
        return this.requiredColons;
    }

    public final User component6() {
        return this.user;
    }

    public final boolean component7() {
        return this.animated;
    }

    public final boolean component8() {
        return this.available;
    }

    public final ModelEmojiGuild copy(long j, String str, boolean z2, List<Long> list, boolean z3, User user2, boolean z4, boolean z5) {
        m.checkNotNullParameter(str, ModelAuditLogEntry.CHANGE_KEY_NAME);
        m.checkNotNullParameter(list, "roles");
        m.checkNotNullParameter(user2, "user");
        return new ModelEmojiGuild(j, str, z2, list, z3, user2, z4, z5);
    }

    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof ModelEmojiGuild)) {
            return false;
        }
        ModelEmojiGuild modelEmojiGuild = (ModelEmojiGuild) obj;
        return this.f2042id == modelEmojiGuild.f2042id && m.areEqual(this.name, modelEmojiGuild.name) && this.managed == modelEmojiGuild.managed && m.areEqual(this.roles, modelEmojiGuild.roles) && this.requiredColons == modelEmojiGuild.requiredColons && m.areEqual(this.user, modelEmojiGuild.user) && this.animated == modelEmojiGuild.animated && this.available == modelEmojiGuild.available;
    }

    public final boolean getAnimated() {
        return this.animated;
    }

    public final boolean getAvailable() {
        return this.available;
    }

    public final long getId() {
        return this.f2042id;
    }

    public final boolean getManaged() {
        return this.managed;
    }

    public final String getName() {
        return this.name;
    }

    public final boolean getRequiredColons() {
        return this.requiredColons;
    }

    public final List<Long> getRoles() {
        return this.roles;
    }

    public final User getUser() {
        return this.user;
    }

    public int hashCode() {
        long j = this.f2042id;
        int i = ((int) (j ^ (j >>> 32))) * 31;
        String str = this.name;
        int i2 = 0;
        int hashCode = (i + (str != null ? str.hashCode() : 0)) * 31;
        boolean z2 = this.managed;
        int i3 = 1;
        if (z2) {
            z2 = true;
        }
        int i4 = z2 ? 1 : 0;
        int i5 = z2 ? 1 : 0;
        int i6 = z2 ? 1 : 0;
        int i7 = (hashCode + i4) * 31;
        List<Long> list = this.roles;
        int hashCode2 = (i7 + (list != null ? list.hashCode() : 0)) * 31;
        boolean z3 = this.requiredColons;
        if (z3) {
            z3 = true;
        }
        int i8 = z3 ? 1 : 0;
        int i9 = z3 ? 1 : 0;
        int i10 = z3 ? 1 : 0;
        int i11 = (hashCode2 + i8) * 31;
        User user2 = this.user;
        if (user2 != null) {
            i2 = user2.hashCode();
        }
        int i12 = (i11 + i2) * 31;
        boolean z4 = this.animated;
        if (z4) {
            z4 = true;
        }
        int i13 = z4 ? 1 : 0;
        int i14 = z4 ? 1 : 0;
        int i15 = z4 ? 1 : 0;
        int i16 = (i12 + i13) * 31;
        boolean z5 = this.available;
        if (!z5) {
            i3 = z5 ? 1 : 0;
        }
        return i16 + i3;
    }

    public String toString() {
        StringBuilder L = a.L("ModelEmojiGuild(id=");
        L.append(this.f2042id);
        L.append(", name=");
        L.append(this.name);
        L.append(", managed=");
        L.append(this.managed);
        L.append(", roles=");
        L.append(this.roles);
        L.append(", requiredColons=");
        L.append(this.requiredColons);
        L.append(", user=");
        L.append(this.user);
        L.append(", animated=");
        L.append(this.animated);
        L.append(", available=");
        return a.G(L, this.available, ")");
    }
}
