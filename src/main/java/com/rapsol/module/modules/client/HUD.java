package com.rapsol.module.modules.client;

import com.rapsol.Argon;
import com.rapsol.event.events.HudListener;
import com.rapsol.gui.ClickGui;
import com.rapsol.gui.HudElement;
import com.rapsol.gui.HudLayout;
import com.rapsol.gui.HudPoint;
import com.rapsol.module.Category;
import com.rapsol.module.Module;
import com.rapsol.module.setting.BooleanSetting;
import com.rapsol.module.setting.NumberSetting;
import com.rapsol.utils.printString;
import com.rapsol.utils.RenderUtils;
import com.rapsol.utils.TextRenderer;
import com.rapsol.utils.Utils;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.multiplayer.PlayerInfo;

import java.awt.*;
import java.util.List;

public final class HUD extends com.rapsol.module.Module implements HudListener {
    private static final CharSequence argon = printString.of("ArgonVisuals |");

    public final BooleanSetting info = new BooleanSetting(printString.of("Info"), true);
    public final BooleanSetting modules = new BooleanSetting("Modules", true)
            .setDescription(printString.of("Renders module array list"));

    public HUD() {
        super(printString.of("HUD"),
                printString.of("Renders the client version and enabled modules on the HUD"),
                -1,
                Category.RENDER);
        addSettings(info, modules);
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
        if (mc.screen instanceof ClickGui) return;

        final List<Module> enabledModules = Argon.INSTANCE.getModuleManager()
                .getEnabledModules()
                .stream()
                .sorted((a, b) -> Integer.compare(
                        TextRenderer.getWidth(b.getName()),
                        TextRenderer.getWidth(a.getName())))
                .toList();

        GuiGraphicsExtractor context = event.context;
        boolean customFont = ClickGUI.customFont.getValue();

        if (info.getValue() && mc.player != null) {
            HudPoint point = HudLayout.get(HudElement.INFO_BAR);
            float s = (float) point.scale;

            String ping = "Ping: ";
            if (mc.player != null && mc.getConnection() != null) {
                PlayerInfo entry = mc.getConnection().getPlayerInfo(mc.player.getUUID());
                ping += (entry != null ? entry.getLatency() : "N/A") + " |";
            } else {
                ping += "N/A |";
            }
            String fps = "FPS: " + mc.getFps() + " |";
            String server = mc.getCurrentServer() == null ? "None" : mc.getCurrentServer().ip;

            int[] bounds = getInfoBounds();
            int guiWidth = mc.getWindow().getGuiScaledWidth();
            int guiHeight = mc.getWindow().getGuiScaledHeight();
            int baseX = point.resolveX(guiWidth, bounds[0]);
            int baseY = point.resolveY(guiHeight, bounds[1]);

            int contentWidth = TextRenderer.getWidth(argon)
                    + TextRenderer.getWidth(fps)
                    + TextRenderer.getWidth(ping)
                    + TextRenderer.getWidth(server) + 35;

            context.pose().pushMatrix();
            context.pose().translate((float) baseX, (float) baseY);
            context.pose().scale(s, s);

            RenderUtils.renderRoundedQuad(context, new Color(35, 35, 35, 255),
                    0, 0, contentWidth, 24, 5, 15);

            int cx = 5;
            int textY = 6;
            TextRenderer.text(argon, context, cx, textY, Utils.getMainColor(255, 4).getRGB());
            cx += TextRenderer.getWidth(argon) + 10;
            TextRenderer.text(fps, context, cx, textY, Utils.getMainColor(255, 3).getRGB());
            cx += TextRenderer.getWidth(fps) + 10;
            TextRenderer.text(ping, context, cx, textY, Utils.getMainColor(255, 2).getRGB());
            cx += TextRenderer.getWidth(ping) + 10;
            TextRenderer.text(server, context, cx, textY, Utils.getMainColor(255, 1).getRGB());

            context.pose().popMatrix();
        }

        if (modules.getValue()) {
            HudPoint point = HudLayout.get(HudElement.MODULE_LIST);
            float s = (float) point.scale;

            int[] bounds = getModuleListBounds();
            int guiWidth = mc.getWindow().getGuiScaledWidth();
            int guiHeight = mc.getWindow().getGuiScaledHeight();
            int baseX = point.resolveX(guiWidth, bounds[0]);
            int baseY = point.resolveY(guiHeight, bounds[1]);

            context.pose().pushMatrix();
            context.pose().translate((float) baseX, (float) baseY);
            context.pose().scale(s, s);

            int lx = 0;
            int offset = 0;

            for (Module module : enabledModules) {
                int charOffset  = 6 + TextRenderer.getWidth(module.getName());
                int charOffset2 = customFont ? 5 : 8;
                int lineH = mc.font.lineHeight * 2;
                int idx   = enabledModules.indexOf(module);

                RenderUtils.renderRoundedQuad(context, new Color(0, 0, 0, 175),
                        lx, offset - 4,
                        lx + charOffset + 5, offset + lineH - 1,
                        0, 0, 0, 5, 10);

                context.fillGradient(lx, offset - 4, lx + 2, offset + lineH,
                        Utils.getMainColor(255, idx).getRGB(),
                        Utils.getMainColor(255, idx + 1).getRGB());

                TextRenderer.text(module.getName(), context,
                        lx + charOffset2, offset + (customFont ? 1 : 0),
                        Utils.getMainColor(255, idx).getRGB());

                offset += lineH + 3;
            }

            context.pose().popMatrix();
        }
    }

    public int[] getInfoBounds() {
        HudPoint infoPos = HudLayout.get(HudElement.INFO_BAR);
        float s = (float) infoPos.scale;

        String ping = "Ping: ";
        if (mc.player != null && mc.getConnection() != null) {
            PlayerInfo entry = mc.getConnection().getPlayerInfo(mc.player.getUUID());
            ping += (entry != null ? entry.getLatency() : "N/A") + " |";
        } else {
            ping += "N/A |";
        }
        String fps = "FPS: " + mc.getFps() + " |";
        String server = mc.getCurrentServer() == null ? "None" : mc.getCurrentServer().ip;

        int contentWidth = TextRenderer.getWidth(argon)
                + TextRenderer.getWidth(fps)
                + TextRenderer.getWidth(ping)
                + TextRenderer.getWidth(server) + 35;

        return new int[]{ (int) (contentWidth * s), (int) (24 * s) };
    }

    public int[] getModuleListBounds() {
        HudPoint listPos = HudLayout.get(HudElement.MODULE_LIST);
        float s = (float) listPos.scale;

        List<Module> enabledModules = Argon.INSTANCE.getModuleManager().getEnabledModules();
        int maxWidth = 0;
        for (Module m : enabledModules) {
            int w = 6 + TextRenderer.getWidth(m.getName()) + 5;
            if (w > maxWidth) maxWidth = w;
        }
        int count = Math.max(1, enabledModules.size());
        int lineH = mc.font.lineHeight * 2;
        int totalHeight = count * (lineH + 3) - 3;

        return new int[]{ (int) (maxWidth * s), (int) (totalHeight * s) };
    }

    public boolean infoEnabled()    { return info.getValue(); }
    public boolean modulesEnabled() { return modules.getValue(); }
}
