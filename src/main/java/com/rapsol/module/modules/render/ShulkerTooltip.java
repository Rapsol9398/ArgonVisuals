package com.rapsol.module.modules.render;

import com.rapsol.module.Category;
import com.rapsol.module.Module;
import com.rapsol.module.setting.KeybindSetting;
import com.rapsol.utils.KeyUtils;
import com.rapsol.utils.printString;
import com.mojang.blaze3d.platform.InputConstants;

public final class ShulkerTooltip extends Module {

    public static ShulkerTooltip instance;

    public final KeybindSetting holdKey = new KeybindSetting(printString.of("hold"), InputConstants.KEY_LSHIFT, false);

    public ShulkerTooltip() {
        super(printString.of("ShulkerTooltip"), printString.of("preview shulker box content"), -1, Category.CLIENT);
        instance = this;
        addSetting(holdKey);
    }

    public static boolean isActive() {
        if (instance == null || !instance.isEnabled()) return false;
        if (instance.holdKey.getKey() == -1) return true;
        return KeyUtils.isKeyPressed(instance.holdKey.getKey());
    }
}