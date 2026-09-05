package com.rapsol.module.modules.render;

import com.rapsol.module.Category;
import com.rapsol.module.Module;
import com.rapsol.module.setting.KeybindSetting;
import com.rapsol.utils.printString;
import net.minecraft.client.Minecraft;
import org.lwjgl.glfw.GLFW;

public final class ShulkerTooltip extends Module {

    public static ShulkerTooltip instance;

    public final KeybindSetting holdKey = new KeybindSetting(printString.of("hold"), GLFW.GLFW_KEY_LEFT_SHIFT, false);

    public ShulkerTooltip() {
        super(printString.of("ShulkerTooltip"), printString.of("preview shulker box content"), -1, Category.CLIENT);
        instance = this;
        addSetting(holdKey);
    }

    public static boolean isActive() {
        if (instance == null || !instance.isEnabled()) return false;
        if (instance.holdKey.getKey() == GLFW.GLFW_KEY_UNKNOWN) return true;
        long window = Minecraft.getInstance().getWindow().handle();
        return GLFW.glfwGetKey(window, instance.holdKey.getKey()) == GLFW.GLFW_PRESS;
    }
}