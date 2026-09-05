package com.rapsol.module.modules.render;

import com.rapsol.module.Category;
import com.rapsol.module.Module;
import com.rapsol.module.setting.BooleanSetting;
import com.rapsol.module.setting.ModeSetting;
import com.rapsol.module.setting.NumberSetting;
import com.rapsol.utils.printString;

public final class ItemPhysics extends Module {

    public ItemPhysics() {super(printString.of("ItemPhysics"), printString.of("Custom item physics"), -1, Category.RENDER);
        addSettings(scale, rotationSpeed, bounce, facingMode, rotationAxis);
    }

    public enum FacingMode {player, flat, none}
    public enum RotationAxis {x, y, z, none}

    public final NumberSetting scale = new NumberSetting(printString.of("Scale"), 0.1, 3.0, 1.0, 0.01);
    public final NumberSetting rotationSpeed = new NumberSetting(printString.of("Rotation Speed"), 0.0, 2.0, 1.0, 0.1);
    public final BooleanSetting bounce = new BooleanSetting(printString.of("Bounce"), true);
    public final ModeSetting<FacingMode> facingMode = new ModeSetting<>(printString.of("Facing"), FacingMode.none, FacingMode.class);
    public final ModeSetting<RotationAxis> rotationAxis = new ModeSetting<>(printString.of("Rotation Axis"), RotationAxis.y, RotationAxis.class);
}