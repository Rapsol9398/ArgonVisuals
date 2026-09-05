package com.rapsol.mixin;

import com.rapsol.Argon;
import net.minecraft.client.Minecraft;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Minecraft.class)
public class MinecraftMixin {
	@Inject(method = "stop", at = @At("HEAD"))
	private void onClose(CallbackInfo ci) {
		Argon.INSTANCE.getProfileManager().saveProfile();
	}
}
