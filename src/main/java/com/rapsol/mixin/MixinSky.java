package com.rapsol.mixin;

import com.rapsol.Argon;
import com.rapsol.module.modules.render.Sky;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.SkyRenderer;
import net.minecraft.client.renderer.state.level.SkyRenderState;
import net.minecraft.world.level.MoonPhase;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(SkyRenderer.class)
public class MixinSky {

    @Inject(method = "extractRenderState", at = @At("RETURN"))
    private void updateSky(ClientLevel level, float partialTicks, Camera camera,
                           SkyRenderState state, CallbackInfo ci) {
        Sky sky = Argon.INSTANCE.getModuleManager().getModule(Sky.class);
        if (sky == null || !sky.isEnabled()) return;
        state.skyColor = sky.skyColor.getValue().getRGB();
        state.starBrightness = (sky.starBrightness.getValueFloat());
        state.rainBrightness = sky.starBrightness.getValueFloat();
    }

    @Inject(method = "renderSun", at = @At("HEAD"), cancellable = true)
    private void onRenderSun(float rainBrightness, PoseStack poseStack, CallbackInfo ci) {
        Sky sky = Argon.INSTANCE.getModuleManager().getModule(Sky.class);
        if (sky != null && sky.isEnabled() && !sky.renderSun.getValue()) {
            ci.cancel();
        }
    }

    @Inject(method = "renderMoon", at = @At("HEAD"), cancellable = true)
    private void onRenderMoon(MoonPhase moonPhase, float rainBrightness,
                              PoseStack poseStack, CallbackInfo ci) {
        Sky sky = Argon.INSTANCE.getModuleManager().getModule(Sky.class);
        if (sky != null && sky.isEnabled() && !sky.renderMoon.getValue()) {
            ci.cancel();
        }
    }
}