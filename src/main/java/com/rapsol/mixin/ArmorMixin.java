package com.rapsol.mixin;

import com.rapsol.module.modules.render.NoRender;
import net.minecraft.client.renderer.entity.layers.HumanoidArmorLayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(HumanoidArmorLayer.class)
public class ArmorMixin {

    @Inject(method = "renderArmorPiece", at = @At("HEAD"), cancellable = true)
    private void onRender(CallbackInfo ci) {
        if (NoRender.isActive() && NoRender.instance.armor.getValue())
            ci.cancel();
    }
}