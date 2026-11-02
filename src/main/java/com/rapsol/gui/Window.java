package com.rapsol.gui;


import com.rapsol.Argon;
import com.rapsol.gui.components.ModuleButton;
import com.rapsol.module.Category;
import com.rapsol.module.Module;
import com.rapsol.module.modules.client.ClickGUI;
import com.rapsol.utils.ColorUtils;
import com.rapsol.utils.MathUtils;
import com.rapsol.utils.RenderUtils;
import com.rapsol.utils.TextRenderer;
import com.rapsol.utils.Utils;
import net.minecraft.client.gui.GuiGraphicsExtractor;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public final class Window {
	public List<ModuleButton> moduleButtons = new ArrayList<>();
	public int x;
	public int y;
	private final int width, height;
	public Color currentColor;
	private final Category category;
	public boolean dragging, extended;
	private int dragX, dragY;
	private int prevX, prevY;
	public ClickGui parent;

	public Window(int x, int y, int width, int height, Category category, ClickGui parent) {
		this.x = x;
		this.y = y;
		this.width = width;
		this.dragging = false;
		this.extended = true;
		this.height = height;
		this.category = category;
		this.parent = parent;

		this.prevX = x;
		this.prevY = y;

		int offset = height;
		List<Module> sortedModules = new ArrayList<>(Argon.INSTANCE.getModuleManager().getModulesInCategory(category));

		for (Module module : sortedModules) {
			moduleButtons.add(new ModuleButton(this, module, offset));
			offset += height;
		}
	}

	public void render(GuiGraphicsExtractor context, int mouseX, int mouseY, float delta) {
		int toAlpha = ClickGUI.alphaWindow.getValueInt();

		if (currentColor == null)
			currentColor = new Color(0, 0, 0, 0);
		else currentColor = new Color(0, 0, 0, currentColor.getAlpha());

		if (currentColor.getAlpha() != toAlpha)
			currentColor = ColorUtils.smoothAlphaTransition(0.05F, toAlpha, currentColor);

		RenderUtils.renderRoundedQuad(context, currentColor, prevX, prevY, prevX + width, prevY + height, ClickGUI.roundQuads.getValueInt(), ClickGUI.roundQuads.getValueInt(), 0, 0, 50);
		context.fill(prevX, prevY + (height - 2), prevX + width, prevY + height, Utils.getMainColor(255, moduleButtons.indexOf(moduleButtons.get(0))).getRGB());

		int charOffset = (prevX + (width / 2));
		int totalWidth = TextRenderer.getWidth(category.name);
		int startX = charOffset - (totalWidth / 2);

		TextRenderer.text(category.name, context, startX, prevY + 6, Color.WHITE.getRGB());

		updateButtons(delta);

		if (category == Category.CLIENT)
			renderSearch(context);

		for (ModuleButton moduleButton : visibleButtons())
			moduleButton.render(context, mouseX, mouseY, delta);
	}

	private void renderSearch(GuiGraphicsExtractor context) {
		SearchField search = parent.search;
		int top = prevY + height;
		context.fill(prevX, top, prevX + width, top + height, new Color(0, 0, 0, 170).getRGB());

		int fx0 = prevX + 6, fy0 = top + 4, fx1 = prevX + width - 6, fy1 = top + height - 4;
		RenderUtils.renderRoundedQuad(context, new Color(0, 0, 0, 120), fx0, fy0, fx1, fy1, 4, 10);
		RenderUtils.renderRoundedOutline(context, search.focused() ? Utils.getMainColor(255, 0) : new Color(50, 50, 50, 255), fx0, fy0, fx1, fy1, 4, 4, 4, 4, 1, 10);
		RenderUtils.renderSearchIcon(context, new Color(150, 150, 150, 255), fx0 + 7, top + height / 2 - 4);

		int textX = fx0 + 23;
		String text = search.text();
		if (text.isEmpty() && !search.focused()) {
			TextRenderer.text("Search...", context, textX, top + 8, new Color(150, 150, 150, 255).getRGB());
			return;
		}

		while (!text.isEmpty() && TextRenderer.getWidth(text) > fx1 - 6 - textX)
			text = text.substring(1);
		TextRenderer.text(text, context, textX, top + 8, new Color(245, 245, 245, 255).getRGB());
		if (search.focused() && (System.currentTimeMillis() / 500) % 2 == 0) {
			int caretX = textX + TextRenderer.getWidth(text) + 1;
			context.fill(caretX, top + 8, caretX + 1, top + height - 8, new Color(245, 245, 245, 255).getRGB());
		}
	}

	public boolean isSearchHovered(double mouseX, double mouseY) {
		return category == Category.CLIENT && mouseX > prevX && mouseX < prevX + width && mouseY > prevY + height && mouseY < prevY + 2 * height;
	}

	public List<ModuleButton> visibleButtons() {
		return moduleButtons.stream().filter(ModuleButton::isVisible).toList();
	}


	public void keyPressed(int keyCode, int scanCode, int modifiers) {
		for (ModuleButton moduleButton : moduleButtons)
			moduleButton.keyPressed(keyCode, scanCode, modifiers);
	}

	public void onGuiClose() {
		currentColor = null;

		for (ModuleButton moduleButton : moduleButtons)
			moduleButton.onGuiClose();

		dragging = false;
	}


	public boolean isDraggingAlready() {
		for(Window window : parent.windows)
			if(window.dragging)
				return true;

		return false;
	}

	public void mouseClicked(double mouseX, double mouseY, int button) {
		if (isHovered(mouseX, mouseY)) {
			switch (button) {
				case 0: {
					if(!parent.isDraggingAlready()) {
						dragging = true;
						dragX = (int) (mouseX - x);
						dragY = (int) (mouseY - y);
					}
					break;
				}
				case 1: {
					if (!dragging) {
						//extended = !extended;
					}
					break;
				}
			}
		}
		if (extended)
			for (ModuleButton moduleButton : visibleButtons())
				moduleButton.mouseClicked(mouseX, mouseY, button);
	}

	public void mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
		if (extended) {
			for (ModuleButton moduleButton : visibleButtons()) {
				moduleButton.mouseDragged(mouseX, mouseY, button, deltaX, deltaY);
			}
		}
	}

	public void updateButtons(float delta) {
		int offset = category == Category.CLIENT ? height * 2 : height;

		for(ModuleButton moduleButton : visibleButtons()) {
			moduleButton.updateLayout(delta);
			moduleButton.animation.animate(0.5 * delta, moduleButton.isExtended() ? moduleButton.getExpandedHeight() : height);

			double supHeight = moduleButton.animation.getValue();
			moduleButton.offset = offset;

			offset += (int) supHeight;
		}
	}

	public void mouseReleased(double mouseX, double mouseY, int button) {
		if (button == 0 && dragging) {
			dragging = false;
			Argon.INSTANCE.getProfileManager().markDirty();
		}

		for (ModuleButton moduleButton : moduleButtons)
			moduleButton.mouseReleased(mouseX, mouseY, button);
	}

	public void mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
		prevX = x;
		prevY = y;

		this.prevY = (int) (prevY + (verticalAmount * 20));
		this.setY((int) (y + (verticalAmount * 20)));
	}

	public int getX() {
		return prevX;
	}

	public int getY() {
		return prevY;
	}

	public void setY(int y) {
		this.y = y;
	}

	public void setX(int x) {
		this.x = x;
	}

	public Category getCategory() {
		return category;
	}

	public int getWidth() {
		return width;
	}

	public int getHeight() {
		return height;
	}

	public boolean isHovered(double mouseX, double mouseY) {
		return ((mouseX > x && mouseX < x + width) && (mouseY > y && mouseY < y + height));
	}

	public boolean contains(double mouseX, double mouseY) {
		int bottom = prevY + height;
		for (ModuleButton moduleButton : visibleButtons())
			bottom = Math.max(bottom, prevY + moduleButton.offset + (int) moduleButton.animation.getValue());
		return mouseX > prevX && mouseX < prevX + width && mouseY > prevY && mouseY < bottom;
	}

	public boolean isPrevHovered(double mouseX, double mouseY) {
		return ((mouseX > prevX && mouseX < prevX + width) && (mouseY > prevY && mouseY < prevY + height));
	}

	public void updatePosition(double mouseX, double mouseY, float delta) {
		prevX = x;
		prevY = y;

		if (dragging) {
			x = (int) MathUtils.goodLerp((float) 0.3 * delta, isHovered(mouseX, mouseY) ? x : prevX, mouseX - dragX);
			y = (int) MathUtils.goodLerp((float) 0.3 * delta, isHovered(mouseX, mouseY) ? y : prevY, mouseY - dragY);
        }
	}
}
