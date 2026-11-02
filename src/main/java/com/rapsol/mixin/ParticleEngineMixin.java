package com.rapsol.mixin;

import com.rapsol.Argon;
import com.rapsol.mixin.accessor.ParticleGroupAccessor;
import com.rapsol.module.modules.render.Particle;
import net.minecraft.client.Camera;
import net.minecraft.client.particle.ParticleGroup;
import net.minecraft.client.particle.ParticleEngine;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.state.level.ParticlesRenderState;
import net.minecraft.core.particles.ParticleOptions;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Map;

@Mixin(ParticleEngine.class)
public class ParticleEngineMixin {

	@Shadow
	@Final
	private Map<ParticleRenderType, ParticleGroup<?>> particles;

	@Inject(method = "extract",
			at = @At("HEAD"))
	private void argon$renderParticles(ParticlesRenderState state,
	                                   Frustum frustum,
	                                   Camera camera,
	                                   float tickDelta,
	                                   CallbackInfo ci) {

		Particle module = Argon.INSTANCE.getModuleManager().getModule(Particle.class);
		if (module == null || !module.isEnabled()) return;

		for (ParticleGroup<?> group : particles.values()) {
			((ParticleGroupAccessor) group).argon$getParticles().removeIf(particle -> shouldHideParticle(module, particle));
		}
	}

	@Inject(method = "makeParticle", at = @At("HEAD"), cancellable = true)
	private void argon$hideParticleType(ParticleOptions options, double x, double y, double z, double xa, double ya, double za,
	                                    CallbackInfoReturnable<net.minecraft.client.particle.Particle> cir) {
		Particle module = Argon.INSTANCE.getModuleManager().getModule(Particle.class);
		if (module != null && module.isEnabled() && module.hides(options.getType()))
			cir.setReturnValue(null);
	}

	private boolean shouldHideParticle(Particle module, net.minecraft.client.particle.Particle particle) {
		return particle instanceof net.minecraft.client.particle.TerrainParticle && module.particles.isHidden("block");
	}
}
