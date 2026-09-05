package com.rapsol.mixin;

import com.rapsol.Argon;
import com.rapsol.mixin.accessor.GuiGraphicsExtractorAccessor;
import com.rapsol.module.modules.render.ShulkerTooltip;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
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
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.awt.Color;

@Mixin(AbstractContainerScreen.class)
public class ShulkerTooltipMixin {

    @Shadow protected Slot hoveredSlot;

    @Inject(method = "extractRenderState", at = @At("TAIL"))
    private void onRender(GuiGraphicsExtractor context, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        if (Argon.INSTANCE.getModuleManager().getModule(ShulkerTooltip.class).isEnabled()) {
            if (hoveredSlot != null && hoveredSlot.hasItem()) {
                ItemStack stack = hoveredSlot.getItem();
                if (stack.getItem() instanceof BlockItem bi && bi.getBlock() instanceof ShulkerBoxBlock) {
                    ((GuiGraphicsExtractorAccessor) context).setDeferredTooltip(null);

                    if (ShulkerTooltip.isActive()) {
                        ItemContainerContents contents = stack.get(DataComponents.CONTAINER);
                        if (contents != null) {
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
        int cols     = 9;
        int rows     = 3;
        int cellSize = 18;
        int pad      = 7;
        int titleH   = 14;
        int width    = cols * cellSize + pad * 2;
        int height   = rows * cellSize + pad * 2 + titleH;

        int x = mouseX + 12;
        int y = mouseY - height / 2;

        int screenW = Minecraft.getInstance().getWindow().getScreenWidth();
        if (x + width > screenW) x = mouseX - width - 4;
        if (y < 0) y = 0;

        context.fill(x, y, x + width, y + height, new Color(30, 30, 30, 230).getRGB());
        context.fill(x,             y,              x + width,     y + 1,          new Color(80, 80, 80, 255).getRGB());
        context.fill(x,             y + height - 1, x + width,     y + height,     new Color(80, 80, 80, 255).getRGB());
        context.fill(x,             y,              x + 1,         y + height,     new Color(80, 80, 80, 255).getRGB());
        context.fill(x + width - 1, y,              x + width,     y + height,     new Color(80, 80, 80, 255).getRGB());

        String title = shulker.getHoverName().getString();
        context.text(Minecraft.getInstance().font, title, x + pad, y + pad - 1, 0xFFFFFFFF);

        context.fill(x + 1, y + titleH, x + width - 1, y + titleH + 1, new Color(60, 60, 60, 255).getRGB());

        int gridY = y + titleH + pad;
        for (int row = 0; row < rows; row++) {
            for (int col = 0; col < cols; col++) {
                int sx = x + pad + col * cellSize;
                int sy = gridY + row * cellSize;
                context.fill(sx,     sy,     sx + 16, sy + 16, new Color(50, 50, 50, 200).getRGB());
                context.fill(sx,     sy,     sx + 16, sy + 1,  new Color(40, 40, 40, 255).getRGB());
                context.fill(sx,     sy,     sx + 1,  sy + 16, new Color(40, 40, 40, 255).getRGB());
                context.fill(sx,     sy + 15,sx + 16, sy + 16, new Color(70, 70, 70, 255).getRGB());
                context.fill(sx + 15,sy,     sx + 16, sy + 16, new Color(70, 70, 70, 255).getRGB());

                ItemStack slotItem = items.get(row * cols + col);
                if (!slotItem.isEmpty()) {
                    context.item(slotItem, sx, sy);
                    context.itemDecorations(Minecraft.getInstance().font, slotItem, sx, sy);
                }
            }
        }
    }
}