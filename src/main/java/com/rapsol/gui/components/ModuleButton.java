package com.rapsol.gui.components;

import com.rapsol.Argon;
import com.rapsol.gui.Window;
import com.rapsol.gui.components.settings.*;
import com.rapsol.module.Module;
import com.rapsol.module.modules.client.ClickGUI;
import com.rapsol.module.setting.*;
import com.rapsol.utils.*;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.Minecraft;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

import static com.rapsol.Argon.mc;

public final class ModuleButton {
	public List<RenderableSetting> settings = new ArrayList<>();
	public Window parent;
	public Module module;
	public int offset;
	public boolean extended;
	public int settingOffset;
	public Color currentColor;
	public Color defaultColor = Color.WHITE;
	public Color currentAlpha;
	public AnimationUtils animation = new AnimationUtils(0);

	private boolean wasHovered = false;

	public ModuleButton(Window parent, com.rapsol.module.Module module, int offset) {
		this.parent = parent;
		this.module = module;
		this.offset = offset;
		this.extended = false;

		settingOffset = parent.getHeight();
		for (Setting<?> setting : module.getSettings()) {
			if (setting instanceof BooleanSetting booleanSetting)
				settings.add(new CheckBox(this, booleanSetting, settingOffset));
			else if (setting instanceof NumberSetting numberSetting)
				settings.add(new Slider(this, numberSetting, settingOffset));
			else if (setting instanceof ModeSetting<?> modeSetting)
				settings.add(new ModeBox(this, modeSetting, settingOffset));
			else if (setting instanceof KeybindSetting keybindSetting)
				settings.add(new KeybindBox(this, keybindSetting, settingOffset));
			else if (setting instanceof StringSetting stringSetting)
				settings.add(new StringBox(this, stringSetting, settingOffset));
			else if (setting instanceof MinMaxSetting minMaxSetting)
				settings.add(new MinMaxSlider(this, minMaxSetting, settingOffset));
			else if (setting instanceof ColorSetting)
				settings.add(new ColorPicker(this, setting, settingOffset));

			settingOffset += parent.getHeight();
		}
	}

	public void updateLayout(float delta) {
		for (RenderableSetting setting : settings)
			setting.tick(delta);
		recomputeOffsets();
	}

	public int getExpandedHeight() {
		int totalHeight = parent.getHeight() * (settings.size() + 1);
		for (RenderableSetting setting : settings)
			totalHeight += setting.getExtraHeight();
		return totalHeight;
	}

	private void recomputeOffsets() {
		int accumulated = 0;
		for (int i = 0; i < settings.size(); i++) {
			RenderableSetting s = settings.get(i);
			s.offset = parent.getHeight() + (i * parent.getHeight()) + accumulated;
			accumulated += s.getExtraHeight();
		}
	}

	public void render(GuiGraphicsExtractor context, int mouseX, int mouseY, float delta) {
		if (parent.getY() + offset > Minecraft.getInstance().getWindow().getScreenHeight())
			return;

		for (RenderableSetting renderableSetting : settings)
			renderableSetting.onUpdate();

		boolean hovered = isHovered(mouseX, mouseY);
		if (hovered && !wasHovered && !parent.dragging)
		wasHovered = hovered;

		if (currentColor == null)
			currentColor = new Color(0, 0, 0, 0);
		else currentColor = new Color(0, 0, 0, currentColor.getAlpha());

		int toAlpha = 170;
		currentColor = ColorUtils.smoothAlphaTransition(0.05F, toAlpha, currentColor);

		Color toColor = module.isEnabled()
				? Utils.getMainColor(255, Argon.INSTANCE.getModuleManager().getModulesInCategory(module.getCategory()).indexOf(module))
				: Color.WHITE;

		if (defaultColor != toColor)
			defaultColor = ColorUtils.smoothColorTransition(0.1F, toColor, defaultColor);

		if (parent.moduleButtons.get(parent.moduleButtons.size() - 1) != this) {
			context.fill(parent.getX(), parent.getY() + offset, parent.getX() + parent.getWidth(), parent.getY() + parent.getHeight() + offset, currentColor.getRGB());
			context.fillGradient(
					parent.getX(), parent.getY() + offset,
					parent.getX() + 2, parent.getY() + parent.getHeight() + offset,
					Utils.getMainColor(255, Argon.INSTANCE.getModuleManager().getModulesInCategory(module.getCategory()).indexOf(module)).getRGB(),
					Utils.getMainColor(255, Argon.INSTANCE.getModuleManager().getModulesInCategory(module.getCategory()).indexOf(module) + 1).getRGB()
			);
		} else {
			RenderUtils.renderRoundedQuad(context, currentColor,
					parent.getX(), parent.getY() + offset,
					parent.getX() + parent.getWidth(), parent.getY() + parent.getHeight() + offset,
					0, 0, 3, animation.getValue() > 30 ? 0 : ClickGUI.roundQuads.getValueInt(), 50);
			RenderUtils.renderRoundedQuad(context,
					Utils.getMainColor(255, Argon.INSTANCE.getModuleManager().getModulesInCategory(module.getCategory()).indexOf(module)),
					parent.getX(), parent.getY() + offset,
					parent.getX() + 2, parent.getY() + (parent.getHeight() - 1) + offset,
					0, 0, extended ? 0 : 2, 0, 50);
		}

		CharSequence nameChars = module.getName();
		int totalWidth = TextRenderer.getWidth(nameChars);
		int parentCenterX = parent.getX() + parent.getWidth() / 2;
		int textCenterX = parentCenterX - totalWidth / 2;
		TextRenderer.text(nameChars, context, textCenterX, parent.getY() + offset + 8, defaultColor.getRGB());

		renderHover(context, mouseX, mouseY, delta);
		renderSettings(context, mouseX, mouseY, delta);

		for (RenderableSetting renderableSetting : settings)
			if (extended) renderableSetting.renderDescription(context, mouseX, mouseY, delta);

		if (hovered && !parent.dragging) {
			CharSequence chars = module.getDescription();
			int tw = TextRenderer.getWidth(chars);
			int parentCenter = mc.getWindow().getWidth() / 2;
			int textCenter = parentCenter - tw / 2;

			RenderUtils.renderRoundedQuad(
					context,
					new Color(100, 100, 100, 100),
					textCenter - 5,
					((double) mc.getWindow().getHeight() / 2) + 294,
					textCenter + tw + 5,
					((double) mc.getWindow().getHeight() / 2) + 318,
					3, 10
			);
			TextRenderer.text(chars, context, textCenter, (mc.getWindow().getHeight() / 2) + 300, Color.WHITE.getRGB());
		}
	}

	private void renderHover(GuiGraphicsExtractor context, int mouseX, int mouseY, float delta) {
		if (!parent.dragging) {
			int toHoverAlpha = isHovered(mouseX, mouseY) ? 15 : 0;

			if (currentAlpha == null)
				currentAlpha = new Color(255, 255, 255, toHoverAlpha);
			else currentAlpha = new Color(255, 255, 255, currentAlpha.getAlpha());

			if (currentAlpha.getAlpha() != toHoverAlpha)
				currentAlpha = ColorUtils.smoothAlphaTransition(0.05F, toHoverAlpha, currentAlpha);

			context.fill(parent.getX(), parent.getY() + offset, parent.getX() + parent.getWidth(), parent.getY() + parent.getHeight() + offset, currentAlpha.getRGB());
		}
	}

	private void renderSettings(GuiGraphicsExtractor context, int mouseX, int mouseY, float delta) {
		context.enableScissor(parent.getX(), parent.getY() + offset, parent.getX() + parent.getWidth(), (int) (parent.getY() + offset + animation.getValue()));

		for (RenderableSetting renderableSetting : settings)
			if (animation.getValue() > parent.getHeight())
				renderableSetting.render(context, mouseX, mouseY, delta);

		for (RenderableSetting renderableSetting : settings) {
			if (animation.getValue() > parent.getHeight()) {
				if (renderableSetting instanceof Slider slider) {
					RenderUtils.renderCircle(context, new Color(0, 0, 0, 170), (slider.parentX() + (Math.max(slider.lerpedOffsetX, 2.5))), slider.parentY() + slider.offset + slider.parentOffset() + 27.5, 6, 15);
					RenderUtils.renderCircle(context, slider.currentColor1.brighter(), (slider.parentX() + (Math.max(slider.lerpedOffsetX, 2.5))), slider.parentY() + slider.offset + slider.parentOffset() + 27.5, 5, 15);
				} else if (renderableSetting instanceof MinMaxSlider slider) {
					RenderUtils.renderCircle(context, new Color(0, 0, 0, 170), (slider.parentX() + (Math.max(slider.lerpedOffsetMinX, 2.5))), slider.parentY() + slider.offset + slider.parentOffset() + 27.5, 6, 15);
					RenderUtils.renderCircle(context, slider.currentColor1.brighter(), (slider.parentX() + (Math.max(slider.lerpedOffsetMinX, 2.5))), slider.parentY() + slider.offset + slider.parentOffset() + 27.5, 5, 15);
					RenderUtils.renderCircle(context, new Color(0, 0, 0, 170), (slider.parentX() + (Math.max(slider.lerpedOffsetMaxX, 2.5))), slider.parentY() + slider.offset + slider.parentOffset() + 27.5, 6, 15);
					RenderUtils.renderCircle(context, slider.currentColor1.brighter(), (slider.parentX() + (Math.max(slider.lerpedOffsetMaxX, 2.5))), slider.parentY() + slider.offset + slider.parentOffset() + 27.5, 5, 15);
				}
			}
		}

		for (RenderableSetting renderableSetting : settings)
			if (animation.getValue() > parent.getHeight())
				renderableSetting.renderPost(context, mouseX, mouseY, delta);

		context.disableScissor();
	}

	public void onExtend() {
		for (ModuleButton moduleButton : parent.moduleButtons)
			moduleButton.extended = false;
	}

	public void keyPressed(int keyCode, int scanCode, int modifiers) {
		for (RenderableSetting setting : settings)
			setting.keyPressed(keyCode, scanCode, modifiers);
	}

	public void mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
		if (extended)
			for (RenderableSetting renderableSetting : settings)
				renderableSetting.mouseDragged(mouseX, mouseY, button, deltaX, deltaY);
	}

	public void mouseClicked(double mouseX, double mouseY, int button) {
		if (isHovered(mouseX, mouseY)) {
			if (button == 0) {
				module.toggle();
			}

			if (button == 1) {
				if (module.getSettings().isEmpty()) return;
				if (!extended)
					onExtend();

				extended = !extended;
			}
		}
		if (extended) {
			for (RenderableSetting renderableSetting : settings)
				renderableSetting.mouseClicked(mouseX, mouseY, button);
		}
	}

	public void onGuiClose() {
		this.currentAlpha = null;
		this.currentColor = null;
		this.wasHovered   = false;

		for (RenderableSetting renderableSetting : settings)
			renderableSetting.onGuiClose();
	}

	public void mouseReleased(double mouseX, double mouseY, int button) {
		for (RenderableSetting renderableSetting : settings)
			renderableSetting.mouseReleased(mouseX, mouseY, button);
	}

	public boolean isHovered(double mouseX, double mouseY) {
		return mouseX > parent.getX()
				&& mouseX < parent.getX() + parent.getWidth()
				&& mouseY > parent.getY() + offset
				&& mouseY < parent.getY() + offset + parent.getHeight();
	}
}
