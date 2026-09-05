package com.rapsol.module.modules.render;


import com.rapsol.module.Category;
import com.rapsol.module.Module;
import com.rapsol.module.setting.BooleanSetting;
import com.rapsol.module.setting.ColorSetting;
import com.rapsol.module.setting.NumberSetting;
import com.rapsol.utils.printString;

import java.awt.*;

public final class Sky extends Module {
	public final ColorSetting skyColor = new ColorSetting(printString.of("Sky Color"), new Color(120, 170, 255))
			.setDescription(printString.of("Custom color used for the sky"));
	public final NumberSetting starBrightness = new NumberSetting(printString.of("Star Brightness"), 0.0, 1.0, 0.3, 0.01)
			.setDescription(printString.of("Multiplier for night star brightness"));
	public final BooleanSetting renderSun = new BooleanSetting(printString.of("Sun"), true)
			.setDescription(printString.of("Toggles rendering the sun"));
	public final BooleanSetting renderMoon = new BooleanSetting(printString.of("Moon"), true)
			.setDescription(printString.of("Toggles rendering the moon"));
	public final BooleanSetting renderRain = new BooleanSetting(printString.of("Rain"), true)
			.setDescription(printString.of("Toggles rain and snow rendering"));
	public final BooleanSetting renderRainSplashes = new BooleanSetting(printString.of("Rain Splash"), true)
			.setDescription(printString.of("Toggles rain splash particles"));
	public final BooleanSetting renderClouds = new BooleanSetting(printString.of("Clouds"), true)
			.setDescription(printString.of("Toggles cloud rendering"));

	public Sky() {
		super(
				printString.of("Sky"),
				printString.of("Customizes sky rendering"),
				-1,
				Category.RENDER
		);

		addSettings(skyColor, starBrightness, renderSun, renderMoon, renderRain, renderRainSplashes, renderClouds);
	}
}
