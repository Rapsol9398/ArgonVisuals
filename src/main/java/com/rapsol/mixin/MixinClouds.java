package com.rapsol.mixin;

import com.rapsol.Argon;
import com.rapsol.module.modules.render.Sky;
import net.minecraft.client.renderer.CloudRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(CloudRenderer.class)
public class MixinClouds {

    @Inject(method = "render", at = @At("HEAD"), cancellable = true)
    private void onRender(CallbackInfo ci) {
        Sky sky = Argon.INSTANCE.getModuleManager().getModule(Sky.class);
        if (sky != null && sky.isEnabled() && !sky.renderClouds.getValue()) {
            ci.cancel();
        }
    }
}