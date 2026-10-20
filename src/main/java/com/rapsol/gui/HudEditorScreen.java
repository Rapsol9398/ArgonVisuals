package com.rapsol.gui;

import com.rapsol.Argon;
import com.rapsol.module.Module;
import com.rapsol.module.modules.client.ClickGUI;
import com.rapsol.module.modules.client.HudEditor;
import com.rapsol.module.modules.hud.ArmorHud;
import com.rapsol.module.modules.hud.Info;
import com.rapsol.module.modules.hud.ModuleList;
import com.rapsol.utils.ColorUtils;
import com.rapsol.utils.KeyUtils;
import com.rapsol.utils.RenderUtils;
import com.rapsol.utils.TextRenderer;
import com.rapsol.utils.Utils;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import org.lwjgl.glfw.GLFW;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.function.IntSupplier;

public class HudEditorScreen extends Screen {

    private static final int PAD = 10;
    private static final int ROW = 30;
    private static final int HANDLE_RADIUS = 6;

    private final List<HudPanel> panels = new ArrayList<>();
    private HudPanel dragging;
    private boolean resizing;
    private double dragOffX, dragOffY;
    private double resizeOriginX, resizeOriginY;
    private double resizeStartScale;
    private Color backgroundColor;

    public HudEditorScreen() {
        super(Component.literal("HUD Editor"));
    }

    @Override
    protected void init() {
        buildPanels();

        Window window = Argon.INSTANCE.clickGui.hudWindow;
        if (window.x < 0) {
            window.setX((minecraft.getWindow().getScreenWidth() - window.getWidth()) / 2);
            window.setY(PAD * 3 + ROW * 2);
        }
    }

    private void buildPanels() {
        panels.clear();

        Info info = Argon.INSTANCE.getModuleManager().getModule(Info.class);
        panels.add(new HudPanel(HudElement.INFO_BAR, info,
                () -> info.getBounds()[0],
                () -> info.getBounds()[1]));

        ModuleList moduleList = Argon.INSTANCE.getModuleManager().getModule(ModuleList.class);
        panels.add(new HudPanel(HudElement.MODULE_LIST, moduleList,
                () -> moduleList.getBounds()[0],
                () -> moduleList.getBounds()[1]));

        ArmorHud armorHud = Argon.INSTANCE.getModuleManager().getModule(ArmorHud.class);
        panels.add(new HudPanel(HudElement.ARMOR_HUD, armorHud,
                () -> armorHud.getBounds()[0],
                () -> armorHud.getBounds()[1]));
    }

    private int raw(double guiCoordinate) {
        return (int) (guiCoordinate * (int) minecraft.getWindow().getGuiScale());
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor ctx, int mx, int my, float delta) {
        if (backgroundColor == null)
            backgroundColor = new Color(0, 0, 0, 0);
        if (backgroundColor.getAlpha() != 90)
            backgroundColor = ColorUtils.smoothAlphaTransition(0.05F, 90, backgroundColor);
        ctx.fill(0, 0, width, height, backgroundColor.getRGB());

        RenderUtils.unscaledProjection(ctx);

        boolean empty = true;
        for (int i = 0; i < panels.size(); i++) {
            if (panels.get(i).module.isEnabled()) {
                panels.get(i).render(ctx, mx, my, i);
                empty = false;
            }
        }

        renderTitle(ctx);

        if (empty)
            TextRenderer.centeredText("Enable an element in the HUD window", ctx,
                    minecraft.getWindow().getScreenWidth() / 2, minecraft.getWindow().getScreenHeight() / 2, new Color(150, 150, 150, 255).getRGB());

        Window window = Argon.INSTANCE.clickGui.hudWindow;
        window.render(ctx, raw(mx), raw(my), delta);
        window.updatePosition(raw(mx), raw(my), delta);

        RenderUtils.scaledProjection(ctx);
        super.extractRenderState(ctx, mx, my, delta);
    }

    private void renderTitle(GuiGraphicsExtractor ctx) {
        String hint = "Drag to move, pull the corner to resize";
        int w = Math.max(230, TextRenderer.getWidth(hint) + PAD * 2);
        int x = (minecraft.getWindow().getScreenWidth() - w) / 2;
        int round = ClickGUI.roundQuads.getValueInt();

        RenderUtils.renderRoundedQuad(ctx, new Color(0, 0, 0, ClickGUI.alphaWindow.getValueInt()), x, PAD, x + w, PAD + ROW * 2, round, round, round, round, 50);
        ctx.fill(x, PAD + ROW - 2, x + w, PAD + ROW, Utils.getMainColor(255, 0).getRGB());
        TextRenderer.centeredText("HUD Editor", ctx, x + w / 2, PAD + 6, Color.WHITE.getRGB());
        TextRenderer.centeredText(hint, ctx, x + w / 2, PAD + ROW + 9, new Color(150, 150, 150, 255).getRGB());
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
        double mx = event.x();
        double my = event.y();
        int btn = event.button();

        Window window = Argon.INSTANCE.clickGui.hudWindow;
        if (window.contains(raw(mx), raw(my))) {
            window.mouseClicked(raw(mx), raw(my), btn);
            return true;
        }

        if (btn != 0) {
            return super.mouseClicked(event, doubleClick);
        }

        for (int i = panels.size() - 1; i >= 0; i--) {
            HudPanel p = panels.get(i);
            if (!p.module.isEnabled()) continue;

            if (p.isHandleHovered(mx, my)) {
                dragging = p;
                resizing = true;
                resizeOriginX = mx;
                resizeOriginY = my;
                resizeStartScale = p.point().scale;
                return true;
            }

            if (p.isHovered(mx, my)) {
                dragging = p;
                resizing = false;
                dragOffX = mx - p.screenX();
                dragOffY = my - p.screenY();
                return true;
            }
        }

        return super.mouseClicked(event, doubleClick);
    }

    @Override
    public boolean mouseDragged(MouseButtonEvent event, double dx, double dy) {
        double mx = event.x();
        double my = event.y();
        if (dragging == null) {
            Argon.INSTANCE.clickGui.hudWindow.mouseDragged(raw(mx), raw(my), event.button(), dx, dy);
            return true;
        }

        if (resizing) {
            double delta = ((mx - resizeOriginX) + (my - resizeOriginY)) * 0.015;
            HudLayout.setScale(dragging.element,
                    Math.max(dragging.element.minScale, Math.min(dragging.element.maxScale, resizeStartScale + delta)));
        } else {
            double nx = Math.max(0, Math.min(width - dragging.w(), mx - dragOffX));
            double ny = Math.max(0, Math.min(height - dragging.h(), my - dragOffY));
            dragging.setPos(nx, ny);

            if (!KeyUtils.isKeyPressed(GLFW.GLFW_KEY_LEFT_SHIFT)) {
                if (dragging.point().offsetX < 4) dragging.point().offsetX = 0;
                if (dragging.point().offsetY < 4) dragging.point().offsetY = 0;
            }
        }
        return true;
    }

    @Override
    public boolean mouseReleased(MouseButtonEvent event) {
        dragging = null;
        resizing = false;
        Argon.INSTANCE.clickGui.hudWindow.mouseReleased(raw(event.x()), raw(event.y()), event.button());
        return super.mouseReleased(event);
    }

    @Override
    public boolean keyPressed(KeyEvent event) {
        Argon.INSTANCE.clickGui.hudWindow.keyPressed(event.key(), event.scancode(), event.modifiers());
        return super.keyPressed(event);
    }

    @Override
    public boolean isPauseScreen() { return false; }

    @Override
    public void onClose() {
        HudEditor mod = Argon.INSTANCE.getModuleManager().getModule(HudEditor.class);
        if (mod != null && mod.isEnabled()) mod.setEnabledStatus(false);
        Argon.INSTANCE.clickGui.hudWindow.onGuiClose();
        super.onClose();
    }

    private final class HudPanel {

        final HudElement element;
        final Module module;
        final IntSupplier widthSupplier, heightSupplier;
        private Color hoverColor;

        HudPanel(HudElement element, Module module, IntSupplier widthSupplier, IntSupplier heightSupplier) {
            this.element = element;
            this.module = module;
            this.widthSupplier = widthSupplier;
            this.heightSupplier = heightSupplier;
        }

        HudPoint point() { return HudLayout.get(element); }

        int screenX() { return point().resolveX(width, w()); }
        int screenY() { return point().resolveY(height, h()); }
        void setPos(double x, double y) {
            point().setFromAbsolute(x, y, width, height, w(), h());
        }

        int w() { return widthSupplier.getAsInt(); }
        int h() { return heightSupplier.getAsInt(); }

        boolean isHovered(double mx, double my) {
            return mx >= screenX() && mx <= screenX() + w() && my >= screenY() && my <= screenY() + h();
        }

        boolean isHandleHovered(double mx, double my) {
            if (!element.resizable) return false;
            double radius = (HANDLE_RADIUS + 2) / minecraft.getWindow().getGuiScale();
            return Math.abs(mx - (screenX() + w())) <= radius && Math.abs(my - (screenY() + h())) <= radius;
        }

        void render(GuiGraphicsExtractor ctx, int mx, int my, int index) {
            int left = raw(screenX()), top = raw(screenY());
            int right = raw(screenX() + w()), bottom = raw(screenY() + h());
            boolean hovered = dragging == this || (dragging == null && isHovered(mx, my));

            int toHoverAlpha = hovered ? 15 : 0;
            if (hoverColor == null)
                hoverColor = new Color(255, 255, 255, 0);
            if (hoverColor.getAlpha() != toHoverAlpha)
                hoverColor = ColorUtils.smoothAlphaTransition(0.05F, toHoverAlpha, hoverColor);
            ctx.fill(left, top, right, bottom, hoverColor.getRGB());

            int round = ClickGUI.roundQuads.getValueInt();
            Color frame = hovered ? Utils.getMainColor(255, index) : new Color(50, 50, 50, 255);
            RenderUtils.renderRoundedOutline(ctx, frame, left, top, right, bottom, round, round, round, round, 2, 20);

            String name = element.displayName;
            String detail = (point().anchorY == HudPoint.Anchor.START ? "Top " : "Bottom ")
                    + (point().anchorX == HudPoint.Anchor.START ? "Left" : "Right")
                    + String.format("  %.2fx", point().scale);
            int nameWidth = TextRenderer.getWidth(name);
            int labelWidth = nameWidth + TextRenderer.getWidth(detail) + PAD * 3;
            int labelY = top - ROW - PAD / 2;
            if (labelY < 0) labelY = bottom + PAD / 2;
            int labelX = Math.max(0, Math.min(minecraft.getWindow().getScreenWidth() - labelWidth, left));

            RenderUtils.renderRoundedQuad(ctx, new Color(0, 0, 0, ClickGUI.alphaWindow.getValueInt()), labelX, labelY, labelX + labelWidth, labelY + ROW, round, round, round, round, 50);
            ctx.fillGradient(labelX, labelY + round, labelX + 2, labelY + ROW - round,
                    Utils.getMainColor(255, index).getRGB(), Utils.getMainColor(255, index + 1).getRGB());
            TextRenderer.text(name, ctx, labelX + PAD, labelY + 9, new Color(245, 245, 245, 255).getRGB());
            TextRenderer.text(detail, ctx, labelX + PAD * 2 + nameWidth, labelY + 9, new Color(150, 150, 150, 255).getRGB());

            if (element.resizable) {
                boolean handleHovered = dragging == this && resizing || (dragging == null && isHandleHovered(mx, my));
                Color knob = Utils.getMainColor(255, index);
                RenderUtils.renderCircle(ctx, new Color(0, 0, 0, 170), right, bottom, HANDLE_RADIUS + 1, 15);
                RenderUtils.renderCircle(ctx, handleHovered ? knob.brighter() : knob, right, bottom, HANDLE_RADIUS, 15);
            }
        }
    }

    @Override
    public void extractBackground(GuiGraphicsExtractor context, int mouseX, int mouseY, float delta) {
    }
}
