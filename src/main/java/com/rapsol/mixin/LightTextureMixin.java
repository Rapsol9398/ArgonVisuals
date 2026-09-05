package com.rapsol.mixin;

import com.rapsol.Argon;
import com.rapsol.module.modules.render.Fullbright;
import com.rapsol.module.modules.render.Totem;
import net.minecraft.client.renderer.Lightmap;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin(Lightmap.class)
public class LightTextureMixin {

    @ModifyArg(
            method = "render",
            at = @At(
                    value = "INVOKE",
                    target = "Lcom/mojang/blaze3d/buffers/Std140Builder;putFloat(F)Lcom/mojang/blaze3d/buffers/Std140Builder;",
                    ordinal = 5
            ),
            index = 0
    )
    private float newbridge$overrideBrightness(float brightness) {
        Fullbright fullbright = Argon.INSTANCE.getModuleManager().getModule(Fullbright.class);
        if (fullbright != null && fullbright.isEnabled()) {
            return 16;
        }
        return brightness;
    }
}