package com.rapsol.mixin;

import com.rapsol.Argon;
import com.rapsol.module.modules.render.Totem;
import net.minecraft.client.renderer.ScreenEffectRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArgs;
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
    private void newbridge$scaleTotemPopAnimation(Args args) {
        Totem totem = Argon.INSTANCE.getModuleManager().getModule(Totem.class);
        if (totem == null || !totem.isEnabled()) return;

        float scale = totem.popSize.getValueFloat();
        args.set(0, (float) args.get(0) * scale);
        args.set(1, (float) args.get(1) * scale);
        args.set(2, (float) args.get(2) * scale);
    }
}