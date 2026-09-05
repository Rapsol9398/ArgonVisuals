package com.rapsol.mixin;

import com.rapsol.Argon;
import com.rapsol.module.modules.render.Fog;
import net.minecraft.client.Camera;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.fog.FogData;
import net.minecraft.client.renderer.fog.FogRenderer;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.material.FogType;
import org.joml.Vector4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.awt.*;

@Mixin(FogRenderer.class)
public class FogRendererMixin {
	@Inject(method = "computeFogColor", at = @At("TAIL"))
	private void argon$render(Camera camera, float tickDelta, ClientLevel world, int viewDistance, float skyDarkness, Vector4f colorVector, CallbackInfo ci) {
		Fog fog = Argon.INSTANCE.getModuleManager().getModule(Fog.class);
		if (fog == null || !fog.isEnabled() || fog.disableAll.getValue()) {
			return;
		}

		Color color = argon$getFogColor(fog, camera);
		if (color != null) {
			colorVector.set(color.getRed() / 255.0f, color.getGreen() / 255.0f, color.getBlue() / 255.0f, colorVector.w);
		}
	}

	@Inject(method = "setupFog", at = @At("RETURN"), cancellable = true)
	private void argon$applyFog(Camera camera, int viewDistance, DeltaTracker deltaTracker, float skyDarkness, ClientLevel world, CallbackInfoReturnable<FogData> cir) {
		Fog fog = Argon.INSTANCE.getModuleManager().getModule(Fog.class);
		if (fog == null || !fog.isEnabled()) {
			return;
		}

		FogData data = cir.getReturnValue();
		if (fog.disableAll.getValue()) {
			data.environmentalStart = 1_000_000.0f;
			data.renderDistanceStart = 1_000_000.0f;
			data.environmentalEnd = 1_000_001.0f;
			data.renderDistanceEnd = 1_000_001.0f;
			data.skyEnd = 1_000_001.0f;
			data.cloudEnd = 1_000_001.0f;
			return;
		}

		float fogEnd = argon$getFogDistance(fog, camera);
		if (fogEnd > 0.0f) {
			float fogStart = Math.max(0.0f, fogEnd * 0.25f);
			data.environmentalStart = fogStart;
			data.renderDistanceStart = fogStart;
			data.environmentalEnd = fogEnd;
			data.renderDistanceEnd = fogEnd;
			data.skyEnd = fogEnd;
			data.cloudEnd = fogEnd;
		}
	}

	private static Color argon$getFogColor(Fog fog, Camera camera) {
		FogType submersionType = camera.getFluidInCamera();
		if (submersionType == FogType.WATER) return fog.waterColor.getValue();
		if (submersionType == FogType.LAVA) return fog.lavaColor.getValue();

		ResourceKey<Level> dimension = argon$getDimension(camera);
		if (dimension == Level.NETHER) return fog.netherColor.getValue();
		if (dimension == Level.END) return fog.endColor.getValue();
		if (dimension == Level.OVERWORLD) return fog.overworldColor.getValue();
		return null;
	}

	private static float argon$getFogDistance(Fog fog, Camera camera) {
		FogType submersionType = camera.getFluidInCamera();
		if (submersionType == FogType.WATER) return fog.waterDistance.getValueFloat();
		if (submersionType == FogType.LAVA) return fog.lavaDistance.getValueFloat();

		ResourceKey<Level> dimension = argon$getDimension(camera);
		if (dimension == Level.NETHER) return fog.netherDistance.getValueFloat();
		if (dimension == Level.END) return fog.endDistance.getValueFloat();
		if (dimension == Level.OVERWORLD) return fog.overworldDistance.getValueFloat();
		return -1.0f;
	}

	private static ResourceKey<Level> argon$getDimension(Camera camera) {
		Entity entity = camera.entity();
		return entity == null ? Level.OVERWORLD : entity.level().dimension();
	}
}
