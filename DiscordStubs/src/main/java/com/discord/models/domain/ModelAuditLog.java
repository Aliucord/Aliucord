package com.discord.models.domain;

import com.discord.api.user.User;

import java.util.List;

public class ModelAuditLog implements Model {
    public List<ModelAuditLogEntry> getAuditLogEntries() { return null; }
    public List<User> getUsers() { return null; }
    // public List<ModelGuildIntegration> getIntegrations() { return null; }
    // public List<ModelWebhook> getWebhooks() { return null; }
}
