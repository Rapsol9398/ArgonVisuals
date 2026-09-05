package com.rapsol.mixin;

import com.rapsol.Argon;
import com.rapsol.module.modules.render.Sky;
import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.WeatherEffectRenderer;
import net.minecraft.client.renderer.state.level.WeatherRenderState;
import net.minecraft.server.level.ParticleStatus;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(WeatherEffectRenderer.class)
public class WeatherMixin {
    @Inject(method = "render", at = @At("HEAD"), cancellable = true)
    private void rainRender(Vec3 cameraPos, WeatherRenderState renderState, CallbackInfo ci) {
        Sky sky = Argon.INSTANCE.getModuleManager().getModule(Sky.class);
        if (sky != null && sky.isEnabled() && !sky.renderRain.getValue()) {
            ci.cancel();
        }
    }
    @Inject(method = "tickRainParticles", at = @At("HEAD"), cancellable = true)
    private void rainParticleRender(ClientLevel level, Camera camera, int ticks, ParticleStatus particleStatus, int weatherRadius, CallbackInfo ci){
        Sky sky = Argon.INSTANCE.getModuleManager().getModule(Sky.class);
        if (sky != null && sky.isEnabled() && !sky.renderRainSplashes.getValue()) {
            ci.cancel();
        }
    }
}
