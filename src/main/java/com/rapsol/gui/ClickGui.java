package com.rapsol.gui;

import com.rapsol.Argon;
import com.rapsol.module.Category;
import com.rapsol.module.modules.client.ClickGUI;
import com.rapsol.utils.ColorUtils;
import com.rapsol.utils.RenderUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

import static com.rapsol.Argon.mc;

public final class ClickGui extends Screen {
	public List<Window> windows = new ArrayList<>();
	public Color currentColor;
	private static final StackWalker sw = StackWalker.getInstance(StackWalker.Option.RETAIN_CLASS_REFERENCE);

	public ClickGui() {
		super(Component.empty());

		int offsetX = 50;
		for (Category category : Category.values()) {
			windows.add(new Window(offsetX, 50, 230, 30, category, this));
			offsetX += 250;
		}
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
		if (mc.screen == this) {
			if (Argon.INSTANCE.previousScreen != null)
				Argon.INSTANCE.previousScreen.extractRenderState(context, 0, 0, delta);

			if (currentColor == null)
				currentColor = new Color(0, 0, 0, 0);
			else currentColor = new Color(0, 0, 0, currentColor.getAlpha());

			if (currentColor.getAlpha() != (ClickGUI.background.getValue() ? 200 : 0))
				currentColor = ColorUtils.smoothAlphaTransition(0.05F, ClickGUI.background.getValue() ? 200 : 0, currentColor);

			if (mc.screen instanceof ClickGui)
				context.fill(0, 0, mc.getWindow().getScreenWidth(), mc.getWindow().getScreenHeight(), currentColor.getRGB());

			RenderUtils.unscaledProjection(context);
			mouseX *= (int) Minecraft.getInstance().getWindow().getGuiScale();
			mouseY *= (int) Minecraft.getInstance().getWindow().getGuiScale();
			super.extractRenderState(context, mouseX, mouseY, delta);

			for (Window window : windows) {
				window.render(context, mouseX, mouseY, delta);
				window.updatePosition(mouseX, mouseY, delta);
			}

			RenderUtils.scaledProjection(context);
		}
	}

	@Override
	public boolean keyPressed(KeyEvent event) {

		for (Window window : windows)
			window.keyPressed(event.key(), event.scancode(), event.modifiers());

		return super.keyPressed(event);
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
		for (Window window : windows)
			window.mouseDragged(mouseX, mouseY, button, deltaX, deltaY);

		return super.mouseDragged(event, deltaX, deltaY);
	}

	@Override
	public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
		Minecraft mc = Minecraft.getInstance();
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
		mc.setScreen(Argon.INSTANCE.previousScreen);
		currentColor = null;

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

		for (Window window : windows)
			window.mouseReleased(mouseX, mouseY, button);

		return super.mouseReleased(event);
	}
}
