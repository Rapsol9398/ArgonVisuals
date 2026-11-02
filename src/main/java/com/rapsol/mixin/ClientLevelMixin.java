package com.rapsol.mixin;

import com.rapsol.Argon;
import com.rapsol.module.modules.render.Sky;
import net.minecraft.client.multiplayer.ClientLevel;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientLevel.class)
public class ClientLevelMixin {
    @Inject(method = "tickWeatherEffects", at = @At("HEAD"), cancellable = true)
    private void rainParticleRender(CallbackInfo ci){
        Sky sky = Argon.INSTANCE.getModuleManager().getModule(Sky.class);
        if (sky != null && sky.isEnabled() && !sky.renderRainSplashes.getValue()) {
            ci.cancel();
        }
    }
}
