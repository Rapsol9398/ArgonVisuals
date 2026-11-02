package com.rapsol.module.setting;

public final class ActionSetting extends Setting<ActionSetting> {
	private final Runnable action;

	public ActionSetting(CharSequence name, Runnable action) {
		super(name);
		this.action = action;
	}

	public void run() {
		action.run();
	}
}
