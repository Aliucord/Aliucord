package com.discord.models.domain;

import androidx.annotation.NonNull;

import java.util.List;

public final class ModelConnectedAccount implements Model {
    public static final int HIDDEN = 0;
    public static final int VISIBLE = 1;
    public ModelConnectedAccount() { }
    @NonNull
    public String getId() { return ""; }
    public List<ModelConnectedAccountIntegration> getIntegrations() { return null; }
    public String getType() { return null; }
    public String getUsername() { return null; }
    public int getVisibility() { return 0; }
    public boolean isFriendSync() { return false; }
    public boolean isRevoked() { return false; }
    public boolean isShowActivity() { return false; }
    public boolean isVerified() { return false; }
    public ModelConnectedAccount(List<ModelConnectedAccountIntegration> list) { }
}
