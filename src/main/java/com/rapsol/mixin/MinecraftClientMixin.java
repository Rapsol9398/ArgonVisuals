package com.rapsol.mixin;

import com.rapsol.Argon;
import net.minecraft.client.player.LocalPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LocalPlayer.class)
public class MinecraftClientMixin {
    private int counter = 0;
    @Inject(method = "tick", at = @At("HEAD"))
    private void onTick(CallbackInfo ci) {
            if (++counter >= 20) {
                counter = 0;
                Argon.INSTANCE.getProfileManager().tick();
        }
    }
}
