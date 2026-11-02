package com.rapsol.mixin;

import com.rapsol.Argon;
import com.rapsol.module.modules.render.Particle;
import it.unimi.dsi.fastutil.ints.IntList;
import net.minecraft.client.particle.FireworkParticles;
import net.minecraft.core.particles.ParticleTypes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(FireworkParticles.Starter.class)
public class FireworkStarterMixin {

	@Inject(method = "createParticle", at = @At("HEAD"), cancellable = true)
	private void onCreateParticle(double x, double y, double z, double xa, double ya, double za, IntList rgbColors,
	                              IntList fadeColors, boolean trail, boolean twinkle, CallbackInfo ci) {
		Particle module = Argon.INSTANCE.getModuleManager().getModule(Particle.class);
		if (module != null && module.isEnabled() && module.hides(ParticleTypes.FIREWORK))
			ci.cancel();
	}
}
