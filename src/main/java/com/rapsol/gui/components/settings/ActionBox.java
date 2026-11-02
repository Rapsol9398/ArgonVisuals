package com.rapsol.gui.components.settings;

import com.rapsol.gui.components.ModuleButton;
import com.rapsol.module.setting.ActionSetting;
import com.rapsol.module.setting.Setting;
import com.rapsol.utils.ColorUtils;
import com.rapsol.utils.TextRenderer;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import org.lwjgl.glfw.GLFW;

import java.awt.*;

public final class ActionBox extends RenderableSetting {
	private final ActionSetting setting;
	private Color currentAlpha;

	public ActionBox(ModuleButton parent, Setting<?> setting, int offset) {
		super(parent, setting, offset);
		this.setting = (ActionSetting) setting;
	}

	@Override
	public void render(GuiGraphicsExtractor context, int mouseX, int mouseY, float delta) {
		super.render(context, mouseX, mouseY, delta);

		TextRenderer.centeredText(setting.getName(), context, parentX() + parentWidth() / 2, (parentY() + parentOffset() + offset) + 9, new Color(245, 245, 245, 255).getRGB());

		if (!parent.parent.dragging) {
			int toHoverAlpha = isHovered(mouseX, mouseY) ? 15 : 0;

			if (currentAlpha == null)
				currentAlpha = new Color(255, 255, 255, toHoverAlpha);
			else currentAlpha = new Color(255, 255, 255, currentAlpha.getAlpha());

			if (currentAlpha.getAlpha() != toHoverAlpha)
				currentAlpha = ColorUtils.smoothAlphaTransition(0.05F, toHoverAlpha, currentAlpha);

			context.fill(parentX(), parentY() + parentOffset() + offset, parentX() + parentWidth(), parentY() + parentOffset() + offset + parentHeight(), currentAlpha.getRGB());
		}
	}

	@Override
	public void mouseClicked(double mouseX, double mouseY, int button) {
		if (isHovered(mouseX, mouseY) && button == GLFW.GLFW_MOUSE_BUTTON_LEFT)
			setting.run();

		super.mouseClicked(mouseX, mouseY, button);
	}
}
