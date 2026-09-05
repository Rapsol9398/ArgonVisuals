package com.rapsol.gui;

import com.rapsol.Argon;
import com.rapsol.module.modules.render.ArmorHud;
import com.rapsol.module.modules.client.HUD;
import com.rapsol.module.modules.client.HudEditor;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.function.IntSupplier;

public class HudEditorScreen extends Screen {

    private static final int HANDLE = 8;

    private final List<HudPanel> panels = new ArrayList<>();
    private HudPanel dragging;
    private boolean resizing;
    private double dragOffX, dragOffY;
    private double resizeOriginX, resizeOriginY;
    private double resizeStartScale;

    public HudEditorScreen() {
        super(Component.literal("HUD Editor"));
    }

    @Override
    protected void init() {
        buildPanels();
    }

    private void buildPanels() {
        panels.clear();

        ArmorHud armorHud = Argon.INSTANCE.getModuleManager().getModule(ArmorHud.class);
        if (armorHud != null && armorHud.isEnabled()) {
            panels.add(new HudPanel(HudElement.ARMOR_HUD,
                    () -> armorHud.getBounds()[0],
                    () -> armorHud.getBounds()[1]));
        }

        HUD hud = Argon.INSTANCE.getModuleManager().getModule(HUD.class);
        if (hud.infoEnabled()) {
            panels.add(new HudPanel(HudElement.INFO_BAR,
                    () -> hud.getInfoBounds()[0],
                    () -> hud.getInfoBounds()[1]));
        }

        if (hud.modulesEnabled()) {
            panels.add(new HudPanel(HudElement.MODULE_LIST,
                    () -> hud.getModuleListBounds()[0],
                    () -> hud.getModuleListBounds()[1]));
        }
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor ctx, int mx, int my, float delta) {
        ctx.fill(0, 0, width, height, 0x55000000);

        ctx.centeredText(minecraft.font,
                "§7Drag §8| §7Corner handle to resize §8| §7ESC to close",
                width / 2, 4, 0xFFCCCCCC);

        if (panels.isEmpty()) {
            ctx.centeredText(minecraft.font,
                    "§cNo HUD modules are enabled.", width / 2, height / 2, 0xFFFFFFFF);
        }

        for (HudPanel p : panels) {
            p.render(ctx, mx, my);
        }

        super.extractRenderState(ctx, mx, my, delta);
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
        double mx = event.x();
        double my = event.y();
        int btn = event.button();

        if (btn != 0) {
            return super.mouseClicked(event, doubleClick);
        }

        for (int i = panels.size() - 1; i >= 0; i--) {
            HudPanel p = panels.get(i);

            int px = p.screenX();
            int py = p.screenY();
            int pw = p.w();
            int ph = p.h();

            if (p.element.resizable) {
                int hx = px + pw - HANDLE;
                int hy = py + ph - HANDLE;

                if (mx >= hx && mx <= hx + HANDLE && my >= hy && my <= hy + HANDLE) {
                    dragging = p;
                    resizing = true;
                    resizeOriginX = mx;
                    resizeOriginY = my;
                    resizeStartScale = p.point().scale;
                    return true;
                }
            }

            if (mx >= px && mx <= px + pw && my >= py && my <= py + ph) {
                dragging = p;
                resizing = false;
                dragOffX = mx - px;
                dragOffY = my - py;
                return true;
            }
        }

        return super.mouseClicked(event, doubleClick);
    }

    @Override
    public boolean mouseDragged(MouseButtonEvent event, double dx, double dy) {
        double mx = event.x();
        double my = event.y();
        if (dragging == null) return false;

        if (resizing && dragging.element.resizable) {
            double delta = ((mx - resizeOriginX) + (my - resizeOriginY)) * 0.015;
            double min = dragging.element.minScale;
            double max = dragging.element.maxScale;
            HudLayout.setScale(dragging.element,
                    Math.max(min, Math.min(max, resizeStartScale + delta)));
        } else {
            double nx = Math.max(0, Math.min(width - dragging.w(), mx - dragOffX));
            double ny = Math.max(0, Math.min(height - dragging.h(), my - dragOffY));
            dragging.setPos(nx, ny);
        }
        return true;
    }

    @Override
    public boolean mouseReleased(MouseButtonEvent event) {
        dragging = null;
        resizing = false;
        return super.mouseReleased(event);
    }

    @Override
    public boolean isPauseScreen() { return false; }

    @Override
    public void onClose() {
        HudEditor mod = Argon.INSTANCE.getModuleManager().getModule(HudEditor.class);
        if (mod != null && mod.isEnabled()) mod.setEnabledStatus(false);
        super.onClose();
    }

    private final class HudPanel {

        final HudElement element;
        final IntSupplier widthSupplier, heightSupplier;

        HudPanel(HudElement element, IntSupplier widthSupplier, IntSupplier heightSupplier) {
            this.element = element;
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

        void render(GuiGraphicsExtractor ctx, int mx, int my) {
            int px = screenX(), py = screenY();
            int pw = w(), ph = h();

            int labelW = minecraft.font.width(element.displayName) + 4;
            ctx.fill(px, py - 12, px + labelW, py - 1, 0xBB000000);
            ctx.text(minecraft.font, element.displayName, px + 2, py - 11, 0xFFDDDDDD);

            if (element.resizable) {
                int hx = px + pw - HANDLE;
                int hy = py + ph - HANDLE;
                boolean handleHovered = mx >= hx && mx <= hx + HANDLE && my >= hy && my <= hy + HANDLE;

                ctx.fill(hx, hy, hx + HANDLE, hy + HANDLE,
                        handleHovered ? 0xFF88BBFF : 0xFF4477CC);

                ctx.fill(hx + 1, hy + 5, hx + 3, hy + 7, 0xFFFFFFFF);
                ctx.fill(hx + 3, hy + 3, hx + 5, hy + 5, 0xFFFFFFFF);
                ctx.fill(hx + 5, hy + 1, hx + 7, hy + 3, 0xFFFFFFFF);
            }
        }
    }

    @Override
    public void extractBackground(GuiGraphicsExtractor context, int mouseX, int mouseY, float delta) {
    }
}