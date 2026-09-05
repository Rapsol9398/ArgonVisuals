package com.rapsol.module.setting;

import java.awt.*;

public final class ColorSetting extends Setting<ColorSetting> {
    private Color value;
    private final Color originalValue;

    public ColorSetting(CharSequence name, Color value) {
        super(name);
        this.value = value;
        this.originalValue = value;
    }

    public Color getValue() {
        return value;
    }

    public Color getOriginalValue() {
        return originalValue;
    }

    public void setValue(Color value) {
        this.value = value;
    }
}