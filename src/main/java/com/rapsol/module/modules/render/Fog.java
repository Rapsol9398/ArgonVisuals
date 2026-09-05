package com.rapsol.module.modules.render;

import com.rapsol.module.Category;
import com.rapsol.module.Module;
import com.rapsol.module.setting.BooleanSetting;
import com.rapsol.module.setting.ColorSetting;
import com.rapsol.module.setting.NumberSetting;
import com.rapsol.utils.printString;

import java.awt.*;

public final class Fog extends Module {
    public final BooleanSetting disableAll = new BooleanSetting(printString.of("Disable All"), false)
            .setDescription(printString.of("Disables fog completely in every environment"));
    public final ColorSetting overworldColor = new ColorSetting(printString.of("Overworld Color"), new Color(197, 225, 255))
            .setDescription(printString.of("Fog color in the overworld"));
    public final NumberSetting overworldDistance = new NumberSetting(printString.of("Overworld Distance"), 8.0, 1000.0, 200.0, 1.0)
            .setDescription(printString.of("Fog visibility distance in the overworld"));
    public final ColorSetting netherColor = new ColorSetting(printString.of("Nether Color"), new Color(120, 32, 32))
            .setDescription(printString.of("Fog color in the nether"));
    public final NumberSetting netherDistance = new NumberSetting(printString.of("Nether Distance"), 4.0, 500.0, 100.0, 1.0)
            .setDescription(printString.of("Fog visibility distance in the nether"));
    public final ColorSetting endColor = new ColorSetting(printString.of("End Color"), new Color(160, 128, 255))
            .setDescription(printString.of("Fog color in the end"));
    public final NumberSetting endDistance = new NumberSetting(printString.of("End Distance"), 8.0, 512.0, 192.0, 1.0)
            .setDescription(printString.of("Fog visibility distance in the end"));
    public final ColorSetting waterColor = new ColorSetting(printString.of("Water Color"), new Color(57, 92, 152))
            .setDescription(printString.of("Fog color while underwater"));
    public final NumberSetting waterDistance = new NumberSetting(printString.of("Water Distance"), 2.0, 192.0, 48.0, 1.0)
            .setDescription(printString.of("Fog visibility distance while underwater"));
    public final ColorSetting lavaColor = new ColorSetting(printString.of("Lava Color"), new Color(255, 96, 0))
            .setDescription(printString.of("Fog color while in lava"));
    public final NumberSetting lavaDistance = new NumberSetting(printString.of("Lava Distance"), 1.0, 96.0, 16.0, 1.0)
            .setDescription(printString.of("Fog visibility distance while in lava"));

    public Fog() {
        super(
                printString.of("Fog"),
                printString.of("Customizes fog color and visibility"),
                -1,
                Category.RENDER
        );

        addSettings(
                disableAll,
                overworldColor,
                overworldDistance,
                netherColor,
                netherDistance,
                endColor,
                endDistance,
                waterColor,
                waterDistance,
                lavaColor,
                lavaDistance
        );
    }
}
