package com.rapsol.module.modules.client;

import com.rapsol.Argon;
import com.rapsol.gui.ClickGui;
import com.rapsol.module.Category;
import com.rapsol.module.Module;
import com.rapsol.module.setting.BooleanSetting;
import com.rapsol.module.setting.ColorSetting;
import com.rapsol.module.setting.ModeSetting;
import com.rapsol.module.setting.NumberSetting;
import com.rapsol.utils.printString;
import org.lwjgl.glfw.GLFW;

import java.awt.*;

public final class ClickGUI extends Module {
	public static final ColorSetting color = new ColorSetting(printString.of("Color"), new Color(255, 0, 50));
	public static final NumberSetting alphaWindow = new NumberSetting(printString.of("Window Alpha"), 0, 255, 170, 1);
	public static final BooleanSetting breathing = new BooleanSetting(printString.of("Breathing"), true)
			.setDescription(printString.of("Color breathing effect (only with rainbow off)"));
	public static final BooleanSetting rainbow = new BooleanSetting(printString.of("Rainbow"), true)
			.setDescription(printString.of("Enables colorful mode"));
	public static final BooleanSetting background = new BooleanSetting(printString.of("Background"), false)
			.setDescription(printString.of("Renders the background of the Click Gui"));
	public static final BooleanSetting customFont = new BooleanSetting(printString.of("Custom Font"), true);
	public static final NumberSetting roundQuads = new NumberSetting(printString.of("Roundness"), 1, 10, 5, 1);
	public static final ModeSetting<AnimationMode> animationMode = new ModeSetting<>(printString.of("Animations"), AnimationMode.Normal, AnimationMode.class);

	public enum AnimationMode {
		Normal, Positive, Off;
	}

	public ClickGUI() {
		super(
				printString.of("ArgonVisuals"),
				printString.of("Settings"),
				GLFW.GLFW_KEY_RIGHT_SHIFT,
				Category.CLIENT
		);

		addSettings(color, alphaWindow, breathing, rainbow, background, customFont, roundQuads, animationMode);
	}

	@Override
	public void onEnable() {
		Argon.INSTANCE.previousScreen = mc.screen;
		mc.setScreen(Argon.INSTANCE.clickGui);
		super.onEnable();
	}

	@Override
	public void onDisable() {
		if (mc.screen instanceof ClickGui) {
			Argon.INSTANCE.clickGui.onClose();
			mc.setScreen(Argon.INSTANCE.previousScreen);
			Argon.INSTANCE.clickGui.onGuiClose();
		}
		super.onDisable();
	}
}
