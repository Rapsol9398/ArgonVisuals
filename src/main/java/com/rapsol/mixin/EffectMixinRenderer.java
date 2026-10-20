package com.rapsol.mixin;

import com.rapsol.Argon;
import com.rapsol.module.modules.render.Totem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.ScreenEffectRenderer;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArgs;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.invoke.arg.Args;

@Mixin(ScreenEffectRenderer.class)
public class EffectMixinRenderer {

    @ModifyArgs(
            method = "renderItemActivationAnimation",
            at = @At(
                    value = "INVOKE",
                    target = "Lcom/mojang/blaze3d/vertex/PoseStack;scale(FFF)V"
            )
    )
    private void onTotemScale(Args args) {
        Totem totem = Argon.INSTANCE.getModuleManager().getModule(Totem.class);
        if (totem == null || !totem.isEnabled()) return;

        float scale = totem.popSize.getValueFloat();
        args.set(0, (float) args.get(0) * scale);
        args.set(1, (float) args.get(1) * scale);
        args.set(2, (float) args.get(2) * scale);
    }

    @Redirect(
            method = "renderItemActivationAnimation",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/renderer/item/ItemStackRenderState;submit(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/SubmitNodeCollector;III)V"
            )
    )
    private void onSubmitTotem(ItemStackRenderState state, PoseStack poseStack, SubmitNodeCollector collector, int light, int overlay, int outline) {
        Totem totem = Argon.INSTANCE.getModuleManager().getModule(Totem.class);
        if (totem != null && totem.isEnabled())
            totem.recolor(state);
        state.submit(poseStack, collector, light, overlay, outline);
    }
}
