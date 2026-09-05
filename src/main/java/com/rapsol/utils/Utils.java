package com.rapsol.utils;

import com.rapsol.module.modules.client.ClickGUI;

import java.awt.*;

public final class Utils {
	public static Color getMainColor(int alpha, int increment) {
		Color c = ClickGUI.color.getValue();

		if (ClickGUI.rainbow.getValue()) {
			return ColorUtils.getBreathingRGBColor(increment, alpha);
		}

		if (ClickGUI.breathing.getValue()) {
			return ColorUtils.getMainColor(new Color(c.getRed(), c.getGreen(), c.getBlue(), alpha), increment, 20);
		}

		return new Color(c.getRed(), c.getGreen(), c.getBlue(), alpha);
	}
}