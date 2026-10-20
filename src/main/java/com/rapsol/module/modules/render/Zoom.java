package com.rapsol.module.modules.render;

import com.rapsol.module.Category;
import com.rapsol.module.Module;
import com.rapsol.module.setting.BooleanSetting;
import com.rapsol.module.setting.KeybindSetting;
import com.rapsol.module.setting.NumberSetting;
import com.rapsol.utils.KeyUtils;
import com.rapsol.utils.printString;
import net.minecraft.util.Mth;
import org.lwjgl.glfw.GLFW;

public final class Zoom extends Module {

	public static Zoom instance;

	public final KeybindSetting holdKey = new KeybindSetting(printString.of("Hold Key"), GLFW.GLFW_KEY_C, false)
			.setDescription(printString.of("Zooms while held, unbound the module key toggles zoom"));
	public final NumberSetting zoom = new NumberSetting(printString.of("Zoom"), 1.5, 20.0, 4.0, 0.1)
			.setDescription(printString.of("How far you zoom in"));
	public final BooleanSetting scroll = new BooleanSetting(printString.of("Scroll"), true)
			.setDescription(printString.of("Scroll while zooming to zoom further in or out"));
	public final BooleanSetting smooth = new BooleanSetting(printString.of("Smooth"), true)
			.setDescription(printString.of("Fades in and out instead of jumping"));
	public final BooleanSetting lowerSensitivity = new BooleanSetting(printString.of("Lower Sensitivity"), true)
			.setDescription(printString.of("Turns slower the further you zoom in"));

	private float level = 4;
	private float factor = 1;
	private boolean zooming;
	private long lastTime;

	public Zoom() {
		super(
				printString.of("Zoom"),
				printString.of("Zooms in while holding the hold key"),
				-1,
				Category.RENDER
		);

		addSettings(holdKey, zoom, scroll, smooth, lowerSensitivity);
		instance = this;
	}

	public static boolean isActive() {
		return instance != null && instance.isEnabled();
	}

	public float applyFov(float fov) {
		long now = System.nanoTime();
		float delta = lastTime == 0 ? 0 : (now - lastTime) / 1e9f;
		lastTime = now;

		boolean held = isEnabled() && (holdKey.getKey() == GLFW.GLFW_KEY_UNKNOWN || mc.screen == null && KeyUtils.isKeyPressed(holdKey.getKey()));
		if (held && !zooming)
			level = zoom.getValueFloat();
		zooming = held;

		float target = zooming ? level : 1;
		if (smooth.getValue())
			factor = (float) Math.exp(Mth.lerp(1 - Math.exp(-12 * delta), Math.log(factor), Math.log(target)));
		else
			factor = target;
		if (Math.abs(factor - target) < 0.001f)
			factor = target;

		return fov / factor;
	}

	public boolean onScroll(double amount) {
		if (!zooming || !scroll.getValue() || amount == 0) return false;

		level = Mth.clamp(level * (float) Math.pow(1.15, Math.signum(amount)), 1.1f, 50);
		return true;
	}

	public double sensitivity() {
		return lowerSensitivity.getValue() ? 1 / factor : 1;
	}
}
