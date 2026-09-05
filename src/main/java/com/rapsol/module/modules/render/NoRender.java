package com.rapsol.module.modules.render;

import com.rapsol.module.Category;
import com.rapsol.module.Module;
import com.rapsol.module.setting.BooleanSetting;
import com.rapsol.utils.printString;

public class NoRender extends Module {

    public static NoRender instance;

    public final BooleanSetting grass = new BooleanSetting(printString.of("Grass"), false);
    public final BooleanSetting bossBar = new BooleanSetting(printString.of("Boss Bar"), false);
    public final BooleanSetting armor = new BooleanSetting(printString.of("Armor"), false);
    public final BooleanSetting fireOverlay = new BooleanSetting(printString.of("Fire Overlay"), false);
    public final BooleanSetting pumpkinOverlay = new BooleanSetting(printString.of("Pumpkin Overlay"), false);
    public final BooleanSetting scoreboard = new BooleanSetting(printString.of("Scoreboard"), false);

    public NoRender() {
        super(printString.of("NoRender"), printString.of("stops things from being rendered"), -1, Category.RENDER);
        addSettings(grass, bossBar, armor, fireOverlay, scoreboard, pumpkinOverlay);
        instance = this;
    }

    @Override
    public void onEnable() {
        super.onEnable();
        if (mc.levelRenderer != null)
            mc.levelRenderer.allChanged();
    }

    @Override
    public void onDisable() {
        super.onDisable();
        if (mc.levelRenderer != null)
            mc.levelRenderer.allChanged();
    }

    public static boolean isActive() {
        return instance != null && instance.isEnabled();
    }
}