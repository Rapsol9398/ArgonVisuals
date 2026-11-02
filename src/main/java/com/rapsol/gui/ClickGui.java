package com.rapsol.gui;

import com.rapsol.Argon;
import com.rapsol.module.Category;
import com.rapsol.module.modules.client.ClickGUI;
import com.rapsol.utils.ColorUtils;
import com.rapsol.utils.RenderUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.CharacterEvent;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import com.mojang.blaze3d.platform.InputConstants;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

import static com.rapsol.Argon.mc;

public final class ClickGui extends Screen {
	public List<Window> windows = new ArrayList<>();
	public final Window hudWindow;
	public final SearchField search = new SearchField();
	public ParticlePanel panel;
	public Color currentColor;
	private static final StackWalker sw = StackWalker.getInstance(StackWalker.Option.RETAIN_CLASS_REFERENCE);

	public ClickGui() {
		super(Component.empty());

		int offsetX = 50;
		for (Category category : Category.values()) {
			if (category == Category.HUD) continue;
			windows.add(new Window(offsetX, 50, 230, 30, category, this));
			offsetX += 250;
		}

		hudWindow = new Window(-1, -1, 230, 30, Category.HUD, this);
	}

	public List<Window> allWindows() {
		List<Window> all = new ArrayList<>(windows);
		all.add(hudWindow);
		return all;
	}

	public boolean isDraggingAlready() {
		for (Window window : windows)
			if (window.dragging)
				return true;

		return false;
	}

	@Override
	protected void setInitialFocus() {
		if (minecraft == null) return;
		super.setInitialFocus();
	}

	@Override
	protected void init() {
		super.init();
	}

	@Override
	public void extractRenderState(GuiGraphicsExtractor context, int mouseX, int mouseY, float delta) {
		if (mc.gui.screen() == this) {
			if (Argon.INSTANCE.previousScreen != null)
				Argon.INSTANCE.previousScreen.extractRenderState(context, 0, 0, delta);

			if (currentColor == null)
				currentColor = new Color(0, 0, 0, 0);
			else currentColor = new Color(0, 0, 0, currentColor.getAlpha());

			if (currentColor.getAlpha() != (ClickGUI.background.getValue() ? 200 : 0))
				currentColor = ColorUtils.smoothAlphaTransition(0.05F, ClickGUI.background.getValue() ? 200 : 0, currentColor);

			if (mc.gui.screen() instanceof ClickGui)
				context.fill(0, 0, mc.getWindow().getScreenWidth(), mc.getWindow().getScreenHeight(), currentColor.getRGB());

			RenderUtils.unscaledProjection(context);
			mouseX *= (int) Minecraft.getInstance().getWindow().getGuiScale();
			mouseY *= (int) Minecraft.getInstance().getWindow().getGuiScale();
			super.extractRenderState(context, mouseX, mouseY, delta);

			for (Window window : windows) {
				window.render(context, panel == null ? mouseX : -1, panel == null ? mouseY : -1, delta);
				window.updatePosition(mouseX, mouseY, delta);
			}

			if (panel != null)
				panel.render(context, mouseX, mouseY, delta);

			RenderUtils.scaledProjection(context);
		}
	}

	@Override
	public boolean keyPressed(KeyEvent event) {
		if (panel != null) {
			panel.keyPressed(event.key(), event.modifiers());
			return true;
		}

		if (event.key() == InputConstants.KEY_F && (event.modifiers() & InputConstants.MOD_CONTROL) != 0) {
			search.focus();
			return true;
		}

		if (search.focused()) {
			switch (event.key()) {
				case InputConstants.KEY_BACKSPACE -> search.backspace();
				case InputConstants.KEY_RETURN, InputConstants.KEY_NUMPADENTER -> search.blur();
				case InputConstants.KEY_ESCAPE -> search.escape();
			}
			return true;
		}

		if (event.key() == InputConstants.KEY_ESCAPE && search.escape())
			return true;

		for (Window window : windows)
			window.keyPressed(event.key(), event.keycode(), event.modifiers());

		return super.keyPressed(event);
	}

	@Override
	public boolean charTyped(CharacterEvent event) {
		if (panel != null)
			panel.charTyped(event.codepointAsString());
		else search.type(event.codepointAsString());
		return true;
	}

	public boolean isTyping() {
		return search.focused() || panel != null;
	}

	@Override
	public boolean keyReleased(KeyEvent event) {
		return super.keyReleased(event);
	}

	@Override
	public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
		double mouseX = event.x();
		double mouseY = event.y();
		int button = event.button();
		mouseX *= (int) Minecraft.getInstance().getWindow().getGuiScale();
		mouseY *= (int) Minecraft.getInstance().getWindow().getGuiScale();

		if (panel != null) {
			panel.mouseClicked(mouseX, mouseY, button);
			return true;
		}

		boolean onSearch = false;
		for (Window window : windows)
			onSearch |= window.isSearchHovered(mouseX, mouseY);
		if (onSearch) search.focus();
		else search.blur();

		for (Window window : windows)
			window.mouseClicked(mouseX, mouseY, button);

		return super.mouseClicked(event, doubleClick);
	}

	@Override
	public boolean mouseDragged(MouseButtonEvent event, double deltaX, double deltaY) {
		double mouseX = event.x();
		double mouseY = event.y();
		int button = event.button();
		mouseX *= (int) Minecraft.getInstance().getWindow().getGuiScale();
		mouseY *= (int) Minecraft.getInstance().getWindow().getGuiScale();
		if (panel != null)
			return true;

		for (Window window : windows)
			window.mouseDragged(mouseX, mouseY, button, deltaX, deltaY);

		return super.mouseDragged(event, deltaX, deltaY);
	}

	@Override
	public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
		Minecraft mc = Minecraft.getInstance();
		if (panel != null) {
			panel.mouseScrolled(mouseX * mc.getWindow().getGuiScale(), mouseY * mc.getWindow().getGuiScale(), verticalAmount);
			return true;
		}

		mouseY *= mc.getWindow().getGuiScale();

		for (Window window : windows)
			window.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount);

		return super.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount);
	}

	@Override
	public boolean isPauseScreen() {
		return false;
	}

	@Override
	public void onClose() {
		Argon.INSTANCE.getModuleManager().getModule(ClickGUI.class).setEnabledStatus(false);
		onGuiClose();
	}

	public void onGuiClose() {
		mc.gui.setScreen(Argon.INSTANCE.previousScreen);
		currentColor = null;
		panel = null;
		search.escape();

		for (Window window : windows)
			window.onGuiClose();
	}

	@Override
	public boolean mouseReleased(MouseButtonEvent event) {
		double mouseX = event.x();
		double mouseY = event.y();
		int button = event.button();
		mouseX *= (int) Minecraft.getInstance().getWindow().getGuiScale();
		mouseY *= (int) Minecraft.getInstance().getWindow().getGuiScale();

		if (panel != null)
			panel.mouseReleased(button);

		for (Window window : windows)
			window.mouseReleased(mouseX, mouseY, button);

		return super.mouseReleased(event);
	}
}
