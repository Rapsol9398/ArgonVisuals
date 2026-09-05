package com.rapsol.gui.components.settings;

import com.rapsol.gui.components.ModuleButton;
import com.rapsol.module.setting.ColorSetting;
import com.rapsol.module.setting.Setting;
import com.rapsol.utils.ColorUtils;
import com.rapsol.utils.TextRenderer;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import org.lwjgl.glfw.GLFW;

import java.awt.*;

public final class ColorPicker extends RenderableSetting {

	private final ColorSetting setting;

	private static final int PAD          = 5;
	private static final int SV_HEIGHT    = 70;
	private static final int BAR_HEIGHT   = 8;
	private static final int PANEL_HEIGHT = PAD + SV_HEIGHT + PAD + BAR_HEIGHT + PAD + BAR_HEIGHT + PAD;

	private boolean open;
	private float   hue;
	private float   saturation;
	private float   brightness;
	private int     alpha;

	private boolean draggingSV;
	private boolean draggingHue;
	private boolean draggingAlpha;

	private Color hoverTint;

	public ColorPicker(ModuleButton parent, Setting<?> setting, int offset) {
		super(parent, setting, offset);
		this.setting = (ColorSetting) setting;
		syncHSBFromSetting();
	}

	@Override
	public int getExtraHeight() {
		return open ? PANEL_HEIGHT : 0;
	}

	private void syncHSBFromSetting() {
		Color c = setting.getValue();
		float[] hsb = Color.RGBtoHSB(c.getRed(), c.getGreen(), c.getBlue(), null);
		hue        = hsb[0];
		saturation = hsb[1];
		brightness = hsb[2];
		alpha      = c.getAlpha();
	}

	private Color builtColor() {
		int rgb = Color.HSBtoRGB(hue, saturation, brightness);
		Color c = new Color(rgb);
		return new Color(c.getRed(), c.getGreen(), c.getBlue(), alpha);
	}

	private int panelX() { return parentX(); }
	private int panelY() { return parentY() + parentOffset() + offset + parentHeight(); }
	private int panelW() { return parentWidth(); }

	private int svX()    { return panelX() + PAD; }
	private int svY()    { return panelY() + PAD; }
	private int svW()    { return panelW() - PAD * 2; }

	private int hueY()   { return svY() + SV_HEIGHT + PAD; }
	private int alphaY() { return hueY() + BAR_HEIGHT + PAD; }


	@Override
	public void render(GuiGraphicsExtractor context, int mouseX, int mouseY, float delta) {
		super.render(context, mouseX, mouseY, delta);
		TextRenderer.text(setting.getName(), context,
				parentX() + 5,
				parentY() + parentOffset() + offset + 9,
				new Color(245, 245, 245, 255).getRGB());

		Color cur     = builtColor();
		int swatchX   = parentX() + parentWidth() - 19;
		int swatchY   = parentY() + parentOffset() + offset + 5;
		int swatchH   = parentHeight() - 10;
		context.fill(swatchX - 1, swatchY - 1, swatchX + 15, swatchY + swatchH + 1, Color.DARK_GRAY.getRGB());
		context.fill(swatchX,     swatchY,      swatchX + 14, swatchY + swatchH,     cur.getRGB());

		if (!parent.parent.dragging) {
			int target = isHovered(mouseX, mouseY) ? 15 : 0;
			if (hoverTint == null)
				hoverTint = new Color(255, 255, 255, target);
			else
				hoverTint = new Color(255, 255, 255, hoverTint.getAlpha());
			if (hoverTint.getAlpha() != target)
				hoverTint = ColorUtils.smoothAlphaTransition(0.05f, target, hoverTint);
			context.fill(parentX(), parentY() + parentOffset() + offset,
					parentX() + parentWidth(), parentY() + parentOffset() + offset + parentHeight(),
					hoverTint.getRGB());
		}
	}

	@Override
	public void renderPost(GuiGraphicsExtractor context, int mouseX, int mouseY, float delta) {
		if (open) renderPanel(context);
	}

	private void renderPanel(GuiGraphicsExtractor context) {
		int px = panelX(), py = panelY(), pw = panelW();
		context.fill(px, py, px + pw, py + PANEL_HEIGHT, new Color(25, 25, 25, 220).getRGB());
		renderSVSquare(context);
		renderHueBar(context);
		renderAlphaBar(context);
	}

	private void renderSVSquare(GuiGraphicsExtractor context) {
		int x = svX(), y = svY(), w = svW();
		int hueRgb = Color.HSBtoRGB(hue, 1f, 1f);
		for (int col = 0; col < w; col++) {
			float t      = (float) col / w;
			Color rowTop = blend(Color.WHITE, new Color(hueRgb), t);
			context.fillGradient(x + col, y, x + col + 1, y + SV_HEIGHT, rowTop.getRGB(), Color.BLACK.getRGB());
		}
		int cx = x + Math.round(saturation * w);
		int cy = y + Math.round((1f - brightness) * SV_HEIGHT);
		context.fill(cx - 2, cy - 2, cx + 2, cy + 2, new Color(255, 255, 255, 200).getRGB());
		context.fill(cx - 1, cy - 1, cx + 1, cy + 1, new Color(0,   0,   0,   200).getRGB());
	}

	private void renderHueBar(GuiGraphicsExtractor context) {
		int x = svX(), y = hueY(), w = svW();
		for (int col = 0; col < w; col++) {
			context.fill(x + col, y, x + col + 1, y + BAR_HEIGHT,
					Color.HSBtoRGB((float) col / w, 1f, 1f) | 0xFF000000);
		}
		int cx = x + Math.round(hue * w);
		context.fill(cx - 1, y - 1, cx + 1, y + BAR_HEIGHT + 1, new Color(255, 255, 255, 220).getRGB());
	}

	private void renderAlphaBar(GuiGraphicsExtractor context) {
		int x = svX(), y = alphaY(), w = svW();
		Color solid = new Color(Color.HSBtoRGB(hue, saturation, brightness));
		context.fillGradient(x, y, x + w, y + BAR_HEIGHT,
				new Color(solid.getRed(), solid.getGreen(), solid.getBlue(), 0).getRGB(),
				solid.getRGB());
		int cx = x + Math.round((alpha / 255f) * w);
		context.fill(cx - 1, y - 1, cx + 1, y + BAR_HEIGHT + 1, new Color(255, 255, 255, 220).getRGB());
	}

	private boolean inSV(double mx, double my) {
		return mx >= svX() && mx <= svX() + svW() && my >= svY() && my <= svY() + SV_HEIGHT;
	}
	private boolean inHue(double mx, double my) {
		return mx >= svX() && mx <= svX() + svW() && my >= hueY() && my <= hueY() + BAR_HEIGHT;
	}
	private boolean inAlpha(double mx, double my) {
		return mx >= svX() && mx <= svX() + svW() && my >= alphaY() && my <= alphaY() + BAR_HEIGHT;
	}
	private boolean inPanel(double mx, double my) {
		return mx >= panelX() && mx <= panelX() + panelW()
				&& my >= panelY() && my <= panelY() + PANEL_HEIGHT;
	}

	private void applySV(double mx, double my) {
		saturation = (float) Math.max(0, Math.min(1, (mx - svX()) / svW()));
		brightness = (float) Math.max(0, Math.min(1, 1.0 - (my - svY()) / SV_HEIGHT));
		commit();
	}
	private void applyHue(double mx) {
		hue = (float) Math.max(0, Math.min(1, (mx - svX()) / svW()));
		commit();
	}
	private void applyAlpha(double mx) {
		alpha = (int) Math.max(0, Math.min(255, ((mx - svX()) / svW()) * 255));
		commit();
	}
	private void commit() {
		setting.setValue(builtColor());
	}

	@Override
	public void mouseClicked(double mouseX, double mouseY, int button) {
		if (button == GLFW.GLFW_MOUSE_BUTTON_LEFT) {
			if (open) {
				if (inSV(mouseX, mouseY))    { draggingSV    = true; applySV(mouseX, mouseY); return; }
				if (inHue(mouseX, mouseY))   { draggingHue   = true; applyHue(mouseX);         return; }
				if (inAlpha(mouseX, mouseY)) { draggingAlpha = true; applyAlpha(mouseX);        return; }
				if (!inPanel(mouseX, mouseY) && !isHovered(mouseX, mouseY)) { open = false; return; }
			}
			if (isHovered(mouseX, mouseY)) open = !open;
		}
		super.mouseClicked(mouseX, mouseY, button);
	}

	@Override
	public void mouseReleased(double mouseX, double mouseY, int button) {
		if (button == GLFW.GLFW_MOUSE_BUTTON_LEFT) {
			draggingSV = draggingHue = draggingAlpha = false;
		}
		super.mouseReleased(mouseX, mouseY, button);
	}

	@Override
	public void mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
		if (draggingSV)    applySV(mouseX, mouseY);
		if (draggingHue)   applyHue(mouseX);
		if (draggingAlpha) applyAlpha(mouseX);
		super.mouseDragged(mouseX, mouseY, button, deltaX, deltaY);
	}

	@Override
	public void keyPressed(int keyCode, int scanCode, int modifiers) {
		if (mouseOver && parent.extended) {
			if (keyCode == GLFW.GLFW_KEY_BACKSPACE) {
				setting.setValue(setting.getOriginalValue());
				syncHSBFromSetting();
			} else if (keyCode == GLFW.GLFW_KEY_ESCAPE) {
				open = false;
			}
		}
		super.keyPressed(keyCode, scanCode, modifiers);
	}

	@Override
	public void onGuiClose() {
		hoverTint = null;
		open = false;
		super.onGuiClose();
	}

	private static Color blend(Color a, Color b, float t) {
		return new Color(
				Math.max(0, Math.min(255, Math.round(a.getRed()   + t * (b.getRed()   - a.getRed())))),
				Math.max(0, Math.min(255, Math.round(a.getGreen() + t * (b.getGreen() - a.getGreen())))),
				Math.max(0, Math.min(255, Math.round(a.getBlue()  + t * (b.getBlue()  - a.getBlue()))))
		);
	}
}