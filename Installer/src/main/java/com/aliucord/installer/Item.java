package com.aliucord.installer;

import android.graphics.drawable.Drawable;

public class Item {
    final public Drawable icon;
    final public String label;
    final public String version;
    final public boolean markVersionAsIncompatible;

    public Item(Drawable icon, String label, String version, boolean markVersionAsIncompatible) {
        this.icon = icon;
        this.label = label;
        this.version = version;
        this.markVersionAsIncompatible = markVersionAsIncompatible;
    }
}
