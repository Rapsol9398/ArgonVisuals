package com.rapsol.module.modules.render;

import com.rapsol.module.Category;
import com.rapsol.module.Module;
import com.rapsol.utils.printString;

public final class Fullbright extends Module {
	public Fullbright() {
		super(
				printString.of("Fullbright"),
				printString.of("LIGHT EVERYWHERE"),
				-1,
				Category.RENDER
		);
	}

	@Override
	public void onEnable() {}

	@Override
	public void onDisable() {}
}
