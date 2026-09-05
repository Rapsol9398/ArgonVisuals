package com.rapsol.module.modules.render;

import com.rapsol.module.Category;
import com.rapsol.module.Module;
import com.rapsol.module.setting.ColorSetting;
import com.rapsol.utils.printString;

import java.awt.*;

public final class VoidColor extends Module {
	public final ColorSetting overworldColor = new ColorSetting(printString.of("Overworld Color"), new Color(0, 0, 0))
			.setDescription(printString.of("Void color below the overworld horizon"));
	public final ColorSetting endColor = new ColorSetting(printString.of("End Color"), new Color(38, 10, 56))
			.setDescription(printString.of("Void color in the end"));

	public VoidColor() {
		super(
				printString.of("Void"),
				printString.of("Customizes the void (nothing)"),
				-1,
				Category.RENDER
		);

		addSettings(overworldColor, endColor);
	}
}
