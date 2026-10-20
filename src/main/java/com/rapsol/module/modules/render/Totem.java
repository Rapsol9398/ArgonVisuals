package com.rapsol.module.modules.render;


import com.rapsol.mixin.accessor.ItemStackRenderStateAccessor;
import com.rapsol.module.Category;
import com.rapsol.module.Module;
import com.rapsol.module.setting.BooleanSetting;
import com.rapsol.module.setting.ColorSetting;
import com.rapsol.module.setting.NumberSetting;
import com.rapsol.utils.ColorUtils;
import com.rapsol.utils.printString;
import net.minecraft.client.model.geom.builders.UVPair;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.geometry.BakedQuad;
import net.minecraft.data.AtlasIds;

import java.awt.*;
import java.util.List;

public final class Totem extends Module {
	public static final List<String> TEXTURES = List.of("item/totem_of_undying");

	public final NumberSetting popSize = new NumberSetting(printString.of("Pop Size"), 0.01, 2.0, 0.3, 0.01)
			.setDescription(printString.of("Scales the totem pop animation"));
	public final BooleanSetting recolor = new BooleanSetting(printString.of("Recolor"), false)
			.setDescription(printString.of("Uses the color below for the totem pop"));
	public final ColorSetting color = new ColorSetting(printString.of("Color"), new Color(255, 80, 80, 255))
			.setDescription(printString.of("Totem pop color, alpha makes it see-through"));

	public Totem() {
		super(
				printString.of("Totem"),
				printString.of("Customizes the totem pop"),
				-1,
				Category.RENDER
		);

		addSettings(popSize, recolor, color);
	}

	public void recolor(ItemStackRenderState state) {
		if (!recolor.getValue()) return;

		int tint = color.getValue().getRGB();
		TextureAtlas atlas = mc.getAtlasManager().getAtlasOrThrow(AtlasIds.ITEMS);
		ItemStackRenderStateAccessor accessor = (ItemStackRenderStateAccessor) state;

		for (int i = 0; i < accessor.argon$getActiveLayerCount(); i++) {
			ItemStackRenderState.LayerRenderState layer = accessor.argon$getLayers()[i];
			List<BakedQuad> quads = layer.prepareQuadList();
			int slot = layer.tintLayers().size();
			layer.tintLayers().add(tint);

			for (int j = 0; j < quads.size(); j++) {
				BakedQuad quad = quads.get(j);
				BakedQuad.MaterialInfo info = quad.materialInfo();
				TextureAtlasSprite sprite = info.sprite();
				TextureAtlasSprite gray = atlas.getSprite(ColorUtils.grayId(sprite.contents().name()));
				if (gray == atlas.missingSprite()) gray = sprite;

				quads.set(j, new BakedQuad(
						quad.position0(), quad.position1(), quad.position2(), quad.position3(),
						move(quad.packedUV0(), sprite, gray),
						move(quad.packedUV1(), sprite, gray),
						move(quad.packedUV2(), sprite, gray),
						move(quad.packedUV3(), sprite, gray),
						quad.direction(),
						new BakedQuad.MaterialInfo(gray, info.layer(), tint >>> 24 < 255 ? Sheets.translucentItemSheet() : info.itemRenderType(), slot, info.shade(), info.lightEmission())
				));
			}
		}
	}

	private static long move(long uv, TextureAtlasSprite from, TextureAtlasSprite to) {
		return UVPair.pack(UVPair.unpackU(uv) - from.getU0() + to.getU0(), UVPair.unpackV(uv) - from.getV0() + to.getV0());
	}
}
