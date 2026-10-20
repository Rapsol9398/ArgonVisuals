package com.rapsol.mixin;

import com.mojang.blaze3d.resource.CrossFrameResourcePool;
import com.rapsol.Argon;
import com.rapsol.module.modules.render.MotionBlur;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.renderer.GameRenderer;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GameRenderer.class)
public class GameRendererMixin {

    @Shadow @Final private CrossFrameResourcePool resourcePool;

    @Inject(
            method = "render",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/renderer/GameRenderer;renderLevel(Lnet/minecraft/client/DeltaTracker;)V",
                    shift = At.Shift.AFTER
            )
    )
    private void onRenderLevel(DeltaTracker deltaTracker, boolean advanceGameTime, CallbackInfo ci) {
        MotionBlur motionBlur = Argon.INSTANCE.getModuleManager().getModule(MotionBlur.class);
        if (motionBlur != null && motionBlur.isEnabled())
            motionBlur.process(resourcePool);
    }
}
