package com.rapsol.module.modules.render;

import com.rapsol.gui.HudEditorScreen;
import com.rapsol.gui.HudElement;
import com.rapsol.gui.HudLayout;
import com.rapsol.gui.HudPoint;
import com.rapsol.module.Category;
import com.rapsol.module.Module;
import com.rapsol.module.setting.BooleanSetting;
import com.rapsol.utils.printString;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;

public final class ArmorHud extends Module {

    public final BooleanSetting showDurability = new BooleanSetting(printString.of("Durability"), false)
            .setDescription(printString.of("Show remaining durability"));
    public final BooleanSetting colorDurability = new BooleanSetting(printString.of("Color Durability"), true)
            .setDescription(printString.of("Color durability by percentage"));
    public final BooleanSetting vertical = new BooleanSetting(printString.of("Vertical"), false)
            .setDescription(printString.of("Stack slots vertically"));

    private static final EquipmentSlot[] SLOTS = {
            EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET
    };

    public ArmorHud() {
        super(printString.of("ArmorHUD"), printString.of("Displays equipped armor with durability"), -1, Category.RENDER);
        addSettings(showDurability, colorDurability, vertical);
    }

    public HudPoint layout() {
        return HudLayout.get(HudElement.ARMOR_HUD);
    }

    public void onHudRender(GuiGraphicsExtractor context) {
        if (!isEnabled()) return;
        if (mc.player == null || mc.level == null) return;
        if (mc.screen != null && !(mc.screen instanceof HudEditorScreen)) return;

        HudPoint point = layout();
        float s = (float) point.scale;
        int stride = (int) ((16 + 2) * s);

        int[] bounds = getBounds();
        int guiWidth = mc.getWindow().getGuiScaledWidth();
        int guiHeight = mc.getWindow().getGuiScaledHeight();
        int baseX = point.resolveX(guiWidth, bounds[0]);
        int baseY = point.resolveY(guiHeight, bounds[1]);

        for (int i = 0; i < SLOTS.length; i++) {
            ItemStack stack = mc.player.getItemBySlot(SLOTS[i]);
            if (stack.isEmpty()) continue;

            int drawX = vertical.getValue() ? baseX : baseX + stride * i;
            int drawY = vertical.getValue() ? baseY + stride * i : baseY;

            context.pose().pushMatrix();
            context.pose().translate(drawX, drawY);
            context.pose().scale(s, s);

            context.item(stack, 0, 0);
            context.itemDecorations(mc.font, stack, 0, 0);

            if (showDurability.getValue() && stack.isDamageableItem()) {
                int cur = stack.getMaxDamage() - stack.getDamageValue();
                String label = String.valueOf(cur);
                float pct = (float) cur / stack.getMaxDamage();

                int tx, ty;
                if (vertical.getValue()) {
                    tx = 16 + 2;
                    ty = (16 - mc.font.lineHeight) / 2;
                } else {
                    tx = (16 - mc.font.width(label)) / 2;
                    ty = 16 + 1;
                }
                context.text(mc.font, label, tx, ty, durabilityColor(pct));
            }

            context.pose().popMatrix();
        }
    }

    public int[] getBounds() {
        float s = (float) layout().scale;
        int stride = (int) ((16 + 2) * s);
        int durExtra = showDurability.getValue() ? (int) ((mc.font.lineHeight + 1) * s) : 0;
        if (vertical.getValue()) {
            int w = (int) (16 * s) + (showDurability.getValue() ? (int) (28 * s) : 0);
            int h = stride * 4 - (int) (2 * s);
            return new int[]{w, h};
        } else {
            int w = stride * 4 - (int) (2 * s);
            int h = (int) (16 * s) + durExtra;
            return new int[]{w, h};
        }
    }

    private int durabilityColor(float pct) {
        if (!colorDurability.getValue()) return 0xFFFFFFFF;
        if (pct > 0.5f) {
            int r = (int) ((1f - (pct - 0.5f) * 2f) * 255);
            return 0xFF000000 | (r << 16) | (0xFF << 8);
        } else {
            int g = (int) (pct * 2f * 255);
            return 0xFF000000 | (0xFF << 16) | (g << 8);
        }
    }
}