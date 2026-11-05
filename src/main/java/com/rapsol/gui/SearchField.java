package com.rapsol.gui;

import static com.rapsol.Argon.mc;

public final class SearchField {
	private final StringBuilder text = new StringBuilder();
	private boolean focused;

	public String text() {
		return text.toString();
	}

	public String query() {
		return text.toString().strip().toLowerCase();
	}

	public boolean focused() {
		return focused;
	}

	public void focus() {
		focused = true;
		mc.textInputManager().startTextInput(this);
	}

	public void blur() {
		focused = false;
		mc.textInputManager().stopTextInput(this);
	}

	public void type(String chars) {
		if (focused && text.length() + chars.length() <= 48)
			text.append(chars);
	}

	public void backspace() {
		if (focused && !text.isEmpty())
			text.setLength(text.length() - 1);
	}

	public boolean escape() {
		boolean had = focused || !text.isEmpty();
		text.setLength(0);
		blur();
		return had;
	}
}
