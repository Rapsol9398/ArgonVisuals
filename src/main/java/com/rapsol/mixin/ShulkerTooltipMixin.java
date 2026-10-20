package com.rapsol.mixin;

import com.rapsol.Argon;
import com.rapsol.module.modules.client.ClickGUI;
import com.rapsol.module.modules.render.ShulkerTooltip;
import com.rapsol.utils.RenderUtils;
import com.rapsol.utils.TextRenderer;
import com.rapsol.utils.Utils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.core.NonNullList;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemContainerContents;
import net.minecraft.world.level.block.ShulkerBoxBlock;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.awt.Color;

@Mixin(AbstractContainerScreen.class)
public class ShulkerTooltipMixin {

    @Shadow protected Slot hoveredSlot;

    @Inject(method = "extractTooltip", at = @At("HEAD"), cancellable = true)
    private void onTooltip(GuiGraphicsExtractor context, int mouseX, int mouseY, CallbackInfo ci) {
        if (Argon.INSTANCE.getModuleManager().getModule(ShulkerTooltip.class).isEnabled()) {
            if (hoveredSlot != null && hoveredSlot.hasItem()) {
                ItemStack stack = hoveredSlot.getItem();
                if (stack.getItem() instanceof BlockItem bi && bi.getBlock() instanceof ShulkerBoxBlock) {
                    ItemContainerContents contents = stack.get(DataComponents.CONTAINER);
                    if (contents != null) {
                        ci.cancel();

                        if (ShulkerTooltip.isActive()) {
                            NonNullList<ItemStack> items = NonNullList.withSize(27, ItemStack.EMPTY);
                            contents.copyInto(items);
                            renderShulkerGrid(context, mouseX, mouseY, stack, items);
                        }
                    }
                }
            }
        }
    }

    @Unique
    private void renderShulkerGrid(GuiGraphicsExtractor context, int mouseX, int mouseY, ItemStack shulker, NonNullList<ItemStack> items) {
        Minecraft mc = Minecraft.getInstance();
        int cols     = 9;
        int rows     = 3;
        int cellSize = 30;
        int iconSize = 24;
        int pad      = 10;
        int titleH   = 30;
        int width    = cols * cellSize + pad * 2;
        int height   = titleH + rows * cellSize + pad * 2;
        float scale  = 2.0f;

        double guiScale = mc.getWindow().getGuiScale();
        int mx = (int) (mouseX * guiScale / scale);
        int my = (int) (mouseY * guiScale / scale);
        int screenW = (int) (mc.getWindow().getScreenWidth() / scale);
        int screenH = (int) (mc.getWindow().getScreenHeight() / scale);

        int x = mx + 20;
        int y = my - height / 2;
        if (x + width > screenW) x = mx - 20 - width;
        x = Math.max(0, x);
        y = Math.max(0, Math.min(screenH - height, y));

        RenderUtils.unscaledProjection(context);
        context.pose().scale(scale, scale);

        int round = ClickGUI.roundQuads.getValueInt();
        RenderUtils.renderRoundedQuad(context, new Color(0, 0, 0, ClickGUI.alphaWindow.getValueInt()), x, y, x + width, y + height, round, 50);
        context.fill(x, y + titleH - 2, x + width, y + titleH, Utils.getMainColor(255, 0).getRGB());
        TextRenderer.centeredText(shulker.getHoverName().getString(), context, x + width / 2, y + 6, Color.WHITE.getRGB());

        int gridX = x + pad;
        int gridY = y + titleH + pad;
        context.fill(gridX, gridY, gridX + cols * cellSize, gridY + rows * cellSize, new Color(0, 0, 0, 120).getRGB());

        float itemScale = iconSize / 16.0f;
        for (int row = 0; row < rows; row++) {
            for (int col = 0; col < cols; col++) {
                ItemStack slotItem = items.get(row * cols + col);
                if (!slotItem.isEmpty()) {
                    context.pose().pushMatrix();
                    context.pose().translate(gridX + col * cellSize + (cellSize - iconSize) / 2, gridY + row * cellSize + (cellSize - iconSize) / 2);
                    context.pose().scale(itemScale, itemScale);
                    context.item(slotItem, 0, 0);
                    context.itemDecorations(mc.font, slotItem, 0, 0);
                    context.pose().popMatrix();
                }
            }
        }

        RenderUtils.scaledProjection(context);
    }
}
