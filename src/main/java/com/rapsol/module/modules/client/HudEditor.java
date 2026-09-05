package com.rapsol.module.modules.client;

import com.rapsol.Argon;
import com.rapsol.gui.HudEditorScreen;
import com.rapsol.module.Category;
import com.rapsol.module.Module;
import com.rapsol.utils.printString;

public final class HudEditor extends Module {

    public HudEditor() {
        super(
                printString.of("HudEditor"),
                printString.of("Opens the HUD editor to reposition and resize HUD elements"),
                -1,
                Category.CLIENT
        );
    }

    @Override
    public void onEnable() {

        ClickGUI clickGui = Argon.INSTANCE.getModuleManager().getModule(ClickGUI.class);
        if (clickGui != null && clickGui.isEnabled()) {
            clickGui.setEnabledStatus(false);
        }
        mc.execute(() -> mc.setScreen(new HudEditorScreen()));
        super.onEnable();
    }
}