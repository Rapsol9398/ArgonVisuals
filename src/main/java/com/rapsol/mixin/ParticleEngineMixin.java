package com.rapsol.mixin;

import com.rapsol.Argon;
import com.rapsol.module.modules.render.Particle;
import net.minecraft.client.Camera;
import net.minecraft.client.particle.FireworkParticles;
import net.minecraft.client.particle.ParticleGroup;
import net.minecraft.client.particle.ParticleEngine;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.state.level.ParticlesRenderState;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

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
			group.getAll().removeIf(particle -> shouldHideParticle(module, particle));
		}
	}

	private boolean shouldHideParticle(Particle module, net.minecraft.client.particle.Particle particle) {

		if (module.disableAll.getValue()) return true;
		if (particle instanceof net.minecraft.client.particle.TotemParticle)
			return !module.totem.getValue();
		if (particle instanceof FireworkParticles.Starter)
			return !module.firework.getValue();
		if (particle instanceof net.minecraft.client.particle.SpellParticle)
			return !module.potion.getValue();
		if (particle instanceof net.minecraft.client.particle.TerrainParticle)
			return !module.blockBreak.getValue();
		if (particle instanceof net.minecraft.client.particle.HugeExplosionParticle
				|| particle instanceof net.minecraft.client.particle.HugeExplosionSeedParticle
				|| particle instanceof net.minecraft.client.particle.ExplodeParticle) {
			return !module.explosion.getValue();
		}
		if (particle instanceof net.minecraft.client.particle.CampfireSmokeParticle
				|| particle instanceof net.minecraft.client.particle.SmokeParticle
				|| particle instanceof net.minecraft.client.particle.WhiteSmokeParticle
				|| particle instanceof net.minecraft.client.particle.LargeSmokeParticle) {
			return !module.smoke.getValue();
		}
		if (particle instanceof net.minecraft.client.particle.SnowflakeParticle)
			return !module.snow.getValue();
		return false;
	}
}
