package com.rapsol.module.modules.hud;

import com.rapsol.Argon;
import com.rapsol.event.events.HudListener;
import com.rapsol.gui.ClickGui;
import com.rapsol.gui.HudElement;
import com.rapsol.gui.HudLayout;
import com.rapsol.gui.HudPoint;
import com.rapsol.module.Category;
import com.rapsol.module.Module;
import com.rapsol.module.modules.client.ClickGUI;
import com.rapsol.utils.printString;
import com.rapsol.utils.RenderUtils;
import com.rapsol.utils.TextRenderer;
import com.rapsol.utils.Utils;
import net.minecraft.client.gui.GuiGraphicsExtractor;

import java.awt.*;
import java.util.List;

public final class ModuleList extends Module implements HudListener {

    public ModuleList() {
        super(printString.of("Module List"),
                printString.of("Lists the enabled modules"),
                -1,
                Category.HUD);
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

        HudPoint point = HudLayout.get(HudElement.MODULE_LIST);
        List<Module> modules = modules();
        if (point.anchorY == HudPoint.Anchor.END)
            modules = modules.reversed();
        boolean right = point.anchorX == HudPoint.Anchor.END;

        GuiGraphicsExtractor context = event.context;
        boolean customFont = ClickGUI.customFont.getValue();
        float s = (float) point.scale;

        int[] bounds = getBounds();
        int baseX = point.resolveX(mc.getWindow().getGuiScaledWidth(), bounds[0]);
        int baseY = point.resolveY(mc.getWindow().getGuiScaledHeight(), bounds[1]);
        int listWidth = listWidth();

        context.pose().pushMatrix();
        context.pose().translate((float) baseX, (float) baseY);
        context.pose().scale(s, s);

        int offset = 4;
        int lineH = mc.font.lineHeight * 2;
        int charOffset = customFont ? 5 : 8;

        for (int idx = 0; idx < modules.size(); idx++) {
            Module module = modules.get(idx);
            int rowWidth = TextRenderer.getWidth(module.getName()) + 11;
            int x0 = right ? listWidth - rowWidth : 0;
            int x1 = x0 + rowWidth;

            RenderUtils.renderRoundedQuad(context, new Color(0, 0, 0, 175),
                    x0, offset - 4,
                    x1, offset + lineH - 1,
                    0, 0, right ? 5 : 0, right ? 0 : 5, 10);

            int stripe = right ? x1 - 2 : x0;
            context.fillGradient(stripe, offset - 4, stripe + 2, offset + lineH,
                    Utils.getMainColor(255, idx).getRGB(),
                    Utils.getMainColor(255, idx + 1).getRGB());

            TextRenderer.text(module.getName(), context,
                    right ? x0 + 11 - charOffset : x0 + charOffset, offset + (customFont ? 1 : 0),
                    Utils.getMainColor(255, idx).getRGB());

            offset += lineH + 3;
        }

        context.pose().popMatrix();
    }

    private List<Module> modules() {
        return Argon.INSTANCE.getModuleManager()
                .getEnabledModules()
                .stream()
                .sorted((a, b) -> Integer.compare(
                        TextRenderer.getWidth(b.getName()),
                        TextRenderer.getWidth(a.getName())))
                .toList();
    }

    private int listWidth() {
        int width = 0;
        for (Module module : modules())
            width = Math.max(width, TextRenderer.getWidth(module.getName()) + 11);
        return width;
    }

    public int[] getBounds() {
        float s = (float) HudLayout.get(HudElement.MODULE_LIST).scale;
        int count = Math.max(1, modules().size());
        int height = count * (mc.font.lineHeight * 2 + 3) + 4;
        return new int[]{ (int) (listWidth() * s), (int) (height * s) };
    }
}
