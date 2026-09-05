package com.rapsol.module.modules.render;


import com.rapsol.module.Category;
import com.rapsol.module.Module;
import com.rapsol.module.setting.BooleanSetting;
import com.rapsol.utils.printString;

public final class Particle extends Module {
    public final BooleanSetting disableAll = new BooleanSetting(printString.of("Disable All"), false).setDescription(printString.of("Disables all particles"));
    public final BooleanSetting firework = new BooleanSetting(printString.of("firework"), true).setDescription(printString.of("firework"));
    public final BooleanSetting potion = new BooleanSetting(printString.of("potion"), true).setDescription(printString.of("potion"));
    public final BooleanSetting totem = new BooleanSetting(printString.of("totem"), true).setDescription(printString.of("totem"));
    public final BooleanSetting blockBreak = new BooleanSetting(printString.of("blockBreak"), true).setDescription(printString.of("blockBreak"));
    public final BooleanSetting smoke = new BooleanSetting(printString.of("smoke"), true).setDescription(printString.of("smoke"));
    public final BooleanSetting explosion = new BooleanSetting(printString.of("explosion"), true).setDescription(printString.of("explosion"));
    public final BooleanSetting snow = new BooleanSetting(printString.of("snow"), true).setDescription(printString.of("snow"));

    public Particle() {
        super(printString.of("Particle"), printString.of("toggle particle rendering"), -1, Category.RENDER);
        addSettings(disableAll, potion, firework, totem, blockBreak, smoke, explosion, snow);
    }
}