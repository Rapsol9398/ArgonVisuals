package com.rapsol.gui.components.settings;

import com.rapsol.gui.components.ModuleButton;
import com.rapsol.module.setting.Setting;
import com.rapsol.utils.ColorUtils;
import com.rapsol.utils.RenderUtils;
import com.rapsol.utils.TextRenderer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;

import java.awt.*;

public abstract class RenderableSetting {
	public Minecraft mc = Minecraft.getInstance();
	public ModuleButton parent;
	public Setting<?> setting;
	public int offset;
	public Color currentColor;
	public boolean mouseOver;
	int x;
	int y;
	int width;
	int height;

	public int getExtraHeight() {
		return 0;
	}

	public RenderableSetting(ModuleButton parent, Setting<?> setting, int offset) {
		this.parent = parent;
		this.setting = setting;
		this.offset = offset;

		this.x = parentX();
		this.y = parentY() + parentOffset() + offset;
		this.width = parentX() + parentWidth();
		this.height = parentY() + parentOffset() + offset + parentHeight();
	}

	public int parentX() {
		return parent.parent.getX();
	}

	public int parentY() {
		return parent.parent.getY();
	}

	public int parentWidth() {
		return parent.parent.getWidth();
	}

	public int parentHeight() {
		return parent.parent.getHeight();
	}

	public int parentOffset() {
		return parent.offset;
	}

	public void render(GuiGraphicsExtractor context, int mouseX, int mouseY, float delta) {
		updateMouseOver(mouseX, mouseY);
		this.x = parentX();
		this.y = parentY() + parentOffset() + offset;
		this.width = parentX() + parentWidth();
		this.height = parentY() + parentOffset() + offset + parentHeight();

		context.fill(x, y, width, height, currentColor.getRGB());
	}

	public void renderPost(GuiGraphicsExtractor context, int mouseX, int mouseY, float delta) {
	}

	private void updateMouseOver(double mouseX, double mouseY) {
		this.mouseOver = isHovered(mouseX, mouseY);
	}

	public void renderDescription(GuiGraphicsExtractor context, int mouseX, int mouseY, float delta) {
		if (isHovered(mouseX, mouseY) && setting.getDescription() != null && !parent.parent.dragging) {
			CharSequence chars = setting.getDescription();

			int tw = TextRenderer.getWidth(chars);

			int parentCenter = mc.getWindow().getScreenWidth() / 2;
			int textCenter = parentCenter - tw / 2;

			RenderUtils.renderRoundedQuad(
					context,
					new Color(100, 100, 100, 100),
					textCenter - 5,
					(mc.getWindow().getScreenHeight() / 2) + 294,
					textCenter + tw + 5,
					(mc.getWindow().getScreenHeight() / 2) + 318,
					3,
					10
			);

			TextRenderer.text(chars, context, textCenter, (mc.getWindow().getScreenHeight() / 2) + 300, Color.WHITE.getRGB());
		}
	}

	public void onGuiClose() {
		this.currentColor = null;
	}

	public void keyPressed(int keyCode, int scanCode, int modifiers) {
	}

	public boolean isHovered(double mouseX, double mouseY) {
		return mouseX > parentX()
				&& mouseX < parentX() + parentWidth()
				&& mouseY > offset + parentOffset() + parentY()
				&& mouseY < offset + parentOffset() + parentY() + parentHeight();
	}

	public void onUpdate() {
		if (currentColor == null)
			currentColor = new Color(0, 0, 0, 0);
		else currentColor = new Color(0, 0, 0, currentColor.getAlpha());

		int toAlpha = 120;

		if (currentColor.getAlpha() != toAlpha)
			currentColor = ColorUtils.smoothAlphaTransition(0.05F, toAlpha, currentColor);
	}

	public void tick(float delta) {
	}

	public void mouseClicked(double mouseX, double mouseY, int button) {
	}

	public void mouseReleased(double mouseX, double mouseY, int button) {
	}

	public void mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
	}
}
