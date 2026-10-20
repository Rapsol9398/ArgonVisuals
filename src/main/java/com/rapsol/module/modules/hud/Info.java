package com.rapsol.module.modules.hud;

import com.rapsol.event.events.HudListener;
import com.rapsol.gui.ClickGui;
import com.rapsol.gui.HudElement;
import com.rapsol.gui.HudLayout;
import com.rapsol.gui.HudPoint;
import com.rapsol.module.Category;
import com.rapsol.module.Module;
import com.rapsol.module.setting.BooleanSetting;
import com.rapsol.module.setting.StringSetting;
import com.rapsol.utils.printString;
import com.rapsol.utils.RenderUtils;
import com.rapsol.utils.TextRenderer;
import com.rapsol.utils.Utils;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.multiplayer.PlayerInfo;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public final class Info extends Module implements HudListener {
    public final BooleanSetting showName = new BooleanSetting(printString.of("Show Name"), true);
    public final BooleanSetting fps = new BooleanSetting(printString.of("FPS"), true);
    public final BooleanSetting server = new BooleanSetting(printString.of("Server"), true);
    public final BooleanSetting ping = new BooleanSetting(printString.of("Ping"), true);
    public final StringSetting name = new StringSetting(printString.of("Name"), "ArgonVisuals")
            .setDescription(printString.of("Name shown in the info"));

    public Info() {
        super(printString.of("Info"),
                printString.of("Shows the client name, FPS, ping and server"),
                -1,
                Category.HUD);
        addSettings(showName, fps, server, ping, name);
    }

    @Override
    public void onEnable() {
        eventManager.add(HudListener.class, this);
        super.onEnable();
    }

    @Override
    public void onDisable() {
        eventManager.remove(HudListener.class, this);
        super.onDisable();
    }

    @Override
    public void onRenderHud(HudEvent event) {
        if (mc.screen instanceof ClickGui || mc.player == null) return;

        List<String> parts = parts();
        if (parts.isEmpty()) return;

        GuiGraphicsExtractor context = event.context;
        HudPoint point = HudLayout.get(HudElement.INFO_BAR);
        float s = (float) point.scale;

        int[] bounds = getBounds();
        int baseX = point.resolveX(mc.getWindow().getGuiScaledWidth(), bounds[0]);
        int baseY = point.resolveY(mc.getWindow().getGuiScaledHeight(), bounds[1]);

        context.pose().pushMatrix();
        context.pose().translate((float) baseX, (float) baseY);
        context.pose().scale(s, s);

        RenderUtils.renderRoundedQuad(context, new Color(35, 35, 35, 255),
                0, 0, width(parts), 24, 5, 15);

        int cx = 5;
        for (int i = 0; i < parts.size(); i++) {
            TextRenderer.text(parts.get(i), context, cx, 6, Utils.getMainColor(255, parts.size() - i).getRGB());
            cx += TextRenderer.getWidth(parts.get(i)) + 10;
        }

        context.pose().popMatrix();
    }

    private List<String> parts() {
        List<String> parts = new ArrayList<>();
        if (showName.getValue())
            parts.add(name.getValue());
        if (fps.getValue())
            parts.add("FPS: " + mc.getFps());
        if (ping.getValue()) {
            PlayerInfo entry = mc.player != null && mc.getConnection() != null ? mc.getConnection().getPlayerInfo(mc.player.getUUID()) : null;
            parts.add("Ping: " + (entry != null ? entry.getLatency() : "N/A"));
        }
        if (server.getValue())
            parts.add(mc.getCurrentServer() == null ? "None" : mc.getCurrentServer().ip);

        for (int i = 0; i < parts.size() - 1; i++)
            parts.set(i, parts.get(i) + " |");
        return parts;
    }

    private int width(List<String> parts) {
        int width = parts.size() * 10;
        for (String part : parts)
            width += TextRenderer.getWidth(part);
        return width;
    }

    public int[] getBounds() {
        float s = (float) HudLayout.get(HudElement.INFO_BAR).scale;
        return new int[]{ (int) (width(parts()) * s), (int) (24 * s) };
    }
}
