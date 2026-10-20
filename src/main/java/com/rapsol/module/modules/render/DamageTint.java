package com.rapsol.module.modules.render;

import com.mojang.blaze3d.platform.NativeImage;
import com.rapsol.event.events.HudListener;
import com.rapsol.mixin.accessor.OverlayTextureAccessor;
import com.rapsol.module.Category;
import com.rapsol.module.Module;
import com.rapsol.module.setting.ColorSetting;
import com.rapsol.utils.printString;
import net.minecraft.client.renderer.texture.DynamicTexture;

import java.awt.*;

public final class DamageTint extends Module implements HudListener {
	private static final int VANILLA = new Color(255, 0, 0, 77).getRGB();

	public final ColorSetting color = new ColorSetting(printString.of("Color"), new Color(255, 0, 0, 77))
			.setDescription(printString.of("Hurt tint color, alpha is how strong it is"));

	private int lastColor;

	public DamageTint() {
		super(
				printString.of("Damage Tint"),
				printString.of("Changes the red flash on hurt players and mobs"),
				-1,
				Category.RENDER
		);

		addSettings(color);
	}

	@Override
	public void onEnable() {
		eventManager.add(HudListener.class, this);
		super.onEnable();
		upload(color.getValue().getRGB());
	}

	@Override
	public void onDisable() {
		eventManager.remove(HudListener.class, this);
		super.onDisable();
		upload(VANILLA);
	}

	@Override
	public void onRenderHud(HudEvent event) {
		if (color.getValue().getRGB() != lastColor)
			upload(color.getValue().getRGB());
	}

	private void upload(int tint) {
		if (mc.gameRenderer == null) return;

		DynamicTexture texture = ((OverlayTextureAccessor) mc.gameRenderer.overlayTexture()).argon$getTexture();
		NativeImage pixels = texture.getPixels();
		if (pixels == null) return;

		for (int y = 0; y < 8; y++)
			for (int x = 0; x < 16; x++)
				pixels.setPixel(x, y, (255 - (tint >>> 24)) << 24 | tint & 0xFFFFFF);
		texture.upload();
		lastColor = tint;
	}
}
