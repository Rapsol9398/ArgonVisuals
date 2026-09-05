package com.rapsol.module.modules.render;


import com.rapsol.module.Category;
import com.rapsol.module.Module;
import com.rapsol.module.setting.BooleanSetting;
import com.rapsol.module.setting.NumberSetting;
import com.rapsol.utils.printString;

public final class Totem extends Module {
	public final NumberSetting popSize = new NumberSetting(printString.of("Pop Size"), 0.01, 2.0, 0.3, 0.01)
			.setDescription(printString.of("Scales the totem pop animation"));

	public Totem() {
		super(
				printString.of("Totem"),
				printString.of("Customizes the totem pop"),
				-1,
				Category.RENDER
		);

		addSettings(popSize);
	}
}
