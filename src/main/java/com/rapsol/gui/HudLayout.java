package com.rapsol.gui;

import java.util.EnumMap;
import java.util.Map;

public final class HudLayout {
    private static final Map<HudElement, HudPoint> POSITIONS = new EnumMap<>(HudElement.class);

    static {
        for (HudElement e : HudElement.values()) {
            POSITIONS.put(e, new HudPoint(e.defaultX, e.defaultY, e.defaultScale));
        }
    }

    private HudLayout() {}

    public static HudPoint get(HudElement element) {
        return POSITIONS.get(element);
    }

    public static void setScale(HudElement element, double scale) {
        POSITIONS.get(element).scale = scale;
    }

    public static Map<HudElement, HudPoint> all() {
        return POSITIONS;
    }
}