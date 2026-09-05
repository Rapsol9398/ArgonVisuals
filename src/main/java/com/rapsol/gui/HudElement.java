package com.rapsol.gui;

public enum HudElement {
    ARMOR_HUD  ("Armor HUD",     10.0, 10.0, 1.0, 0.5, 4.0, true),
    INFO_BAR   ("Info Display",   5.0,  6.0, 0.4, 0.2, 2.0, true),
    MODULE_LIST("Array List",     0.0, 55.0, 0.4, 0.2, 2.0, true);

    public final String displayName;
    public final double defaultX, defaultY, defaultScale, minScale, maxScale;
    public final boolean resizable;

    HudElement(String displayName, double defaultX, double defaultY, double defaultScale,
               double minScale, double maxScale, boolean resizable) {
        this.displayName = displayName;
        this.defaultX = defaultX;
        this.defaultY = defaultY;
        this.defaultScale = defaultScale;
        this.minScale = minScale;
        this.maxScale = maxScale;
        this.resizable = resizable;
    }
}