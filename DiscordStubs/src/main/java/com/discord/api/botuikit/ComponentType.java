package com.discord.api.botuikit;

public enum ComponentType {
    UNKNOWN(0, Component.class),
    ACTION_ROW(1, ActionRowComponent.class),
    BUTTON(2, ButtonComponent.class),
    SELECT(3, SelectComponent.class);
    private ComponentType(int i, Class<?> cls) { }
    public final Class<? extends Component> getClazz() { return null; }
    public final int getType() { return 0; }
}
