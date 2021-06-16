package com.discord.stores;

import java.util.HashMap;
import java.util.Map;

public class StorePermissions {
    public final Map<Long, Long> getGuildPermissions() { return new HashMap<>(); }
    public final Map<Long, Long> getPermissionsByChannel() { return new HashMap<>(); }
}
