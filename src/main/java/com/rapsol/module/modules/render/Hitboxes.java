package com.rapsol.module.modules.render;

import com.rapsol.module.Category;
import com.rapsol.module.Module;
import com.rapsol.module.setting.BooleanSetting;
import com.rapsol.module.setting.ColorSetting;
import com.rapsol.module.setting.NumberSetting;
import com.rapsol.utils.ColorUtils;
import com.rapsol.utils.printString;
import net.fabricmc.fabric.api.client.rendering.v1.level.LevelExtractionContext;
import net.fabricmc.fabric.api.client.rendering.v1.level.LevelRenderEvents;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.gizmos.GizmoStyle;
import net.minecraft.gizmos.Gizmos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;
import net.minecraft.world.entity.boss.enderdragon.EnderDragonPart;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.awt.*;

public final class Hitboxes extends Module {
	public final ColorSetting color = new ColorSetting(printString.of("Color"), new Color(255, 255, 255, 255))
			.setDescription(printString.of("Color of the hitbox lines"));
	public final BooleanSetting rgb = new BooleanSetting(printString.of("RGB"), false)
			.setDescription(printString.of("Cycles the color through the rainbow"));
	public final NumberSetting lineWidth = new NumberSetting(printString.of("Line Width"), 0.5, 6.0, 2.5, 0.5)
			.setDescription(printString.of("Thickness of the hitbox lines"));
	public final BooleanSetting eyeHeight = new BooleanSetting(printString.of("Eye Height"), false)
			.setDescription(printString.of("Line at eye height like F3+B"));
	public final ColorSetting eyeColor = new ColorSetting(printString.of("Eye Color"), new Color(255, 0, 0, 255))
			.setDescription(printString.of("Color of the eye height line"));
	public final BooleanSetting lookDirection = new BooleanSetting(printString.of("Look Direction"), false)
			.setDescription(printString.of("Arrow where the entity looks like F3+B"));
	public final ColorSetting lookColor = new ColorSetting(printString.of("Look Color"), new Color(0, 0, 255, 255))
			.setDescription(printString.of("Color of the look direction arrow"));

	public Hitboxes() {
		super(
				printString.of("Hitboxes"),
				printString.of("Shows entity hitboxes like F3+B but styled"),
				-1,
				Category.RENDER
		);

		addSettings(color, rgb, lineWidth, eyeHeight, eyeColor, lookDirection, lookColor);
		LevelRenderEvents.END_EXTRACTION.register(context -> {
			if (isEnabled())
				render(context);
		});
	}

	private void render(LevelExtractionContext context) {
		Frustum frustum = context.camera().getCullFrustum();
		float width = lineWidth.getValueFloat();
		int line = rgb.getValue() ? ColorUtils.getBreathingRGBColor(0, color.getValue().getAlpha()).getRGB() : color.getValue().getRGB();

		for (Entity entity : context.level().entitiesForRendering()) {
			if (entity.isInvisible() || !frustum.isVisible(entity.getBoundingBox())) continue;
			if (entity == mc.getCameraEntity() && mc.options.getCameraType().isFirstPerson()) continue;

			float partialTick = context.deltaTracker().getGameTimeDeltaPartialTick(!context.level().tickRateManager().isEntityFrozen(entity));
			Vec3 pos = entity.getPosition(partialTick);
			AABB box = entity.getBoundingBox().move(pos.subtract(entity.position()));
			Gizmos.cuboid(box, GizmoStyle.stroke(line, width));

			if (entity instanceof EnderDragon dragon)
				for (EnderDragonPart part : dragon.getSubEntities())
					Gizmos.cuboid(part.getBoundingBox().move(part.getPosition(partialTick).subtract(part.position())), GizmoStyle.stroke(line, width));

			if (eyeHeight.getValue() && entity instanceof LivingEntity) {
				double eyeY = box.minY + entity.getEyeHeight();
				Gizmos.cuboid(new AABB(box.minX, eyeY - 0.01, box.minZ, box.maxX, eyeY + 0.01, box.maxZ), GizmoStyle.stroke(eyeColor.getValue().getRGB(), width));
			}

			if (lookDirection.getValue()) {
				Vec3 eye = pos.add(0, entity.getEyeHeight(), 0);
				Gizmos.arrow(eye, eye.add(entity.getViewVector(partialTick).scale(2)), lookColor.getValue().getRGB(), width);
			}
		}
	}
}
