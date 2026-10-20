package com.rapsol.module;

import com.rapsol.utils.printString;

public enum Category {
	RENDER(printString.of("Render")),
	CLIENT(printString.of("Client")),
	HUD(printString.of("HUD"));
	public final CharSequence name;

	Category(CharSequence name) {
		this.name = name;
	}
}
