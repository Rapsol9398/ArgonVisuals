package com.rapsol.module.modules.render;

import com.mojang.serialization.MapCodec;
import com.rapsol.event.events.HudListener;
import com.rapsol.module.Category;
import com.rapsol.module.Module;
import com.rapsol.module.setting.BooleanSetting;
import com.rapsol.module.setting.ColorSetting;
import com.rapsol.module.setting.NumberSetting;
import com.rapsol.utils.printString;
import net.fabricmc.fabric.api.client.model.loading.v1.ModelLoadingPlugin;
import net.fabricmc.fabric.api.client.renderer.v1.mesh.MutableQuadView;
import net.fabricmc.fabric.api.client.renderer.v1.mesh.QuadEmitter;
import net.minecraft.client.renderer.block.BlockAndTintGetter;
import net.minecraft.client.renderer.block.dispatch.BlockStateModel;
import net.minecraft.client.renderer.block.dispatch.BlockStateModelPart;
import net.minecraft.client.renderer.chunk.ChunkSectionLayer;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.atlas.SpriteSource;
import net.minecraft.client.resources.model.geometry.BakedQuad;
import net.minecraft.client.resources.model.sprite.Material;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.data.AtlasIds;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.BaseFireBlock;
import net.minecraft.world.level.block.state.BlockState;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.function.Predicate;

public final class Fire extends Module implements HudListener {

	public static Fire instance;

	private static final List<String> TEXTURES = List.of("block/fire_0", "block/fire_1", "block/soul_fire_0", "block/soul_fire_1");

	public static final SpriteSource GRAY = new SpriteSource() {
		@Override
		public void run(ResourceManager manager, Output output) {
			for (String texture : TEXTURES) {
				Identifier id = Identifier.withDefaultNamespace(texture);
				manager.getResource(TEXTURE_ID_CONVERTER.idToFile(id))
						.ifPresent(resource -> output.add(grayId(id), new Resource(resource.source(), () -> gray(resource), resource::metadata)));
			}
		}

		@Override
		public MapCodec<? extends SpriteSource> codec() {
			return MapCodec.unit(this);
		}
	};

	public final NumberSetting height = new NumberSetting(printString.of("Height"), 0, 1.4, 1.4, 0.05)
			.setDescription(printString.of("Cuts fire off above this height"));
	public final BooleanSetting recolor = new BooleanSetting(printString.of("Recolor"), false)
			.setDescription(printString.of("Uses the color below instead of the normal fire"));
	public final ColorSetting color = new ColorSetting(printString.of("Color"), new Color(60, 150, 255, 255))
			.setDescription(printString.of("Fire color, alpha makes it see-through"));

	private double lastHeight = 1.4;
	private int lastTint = -1;
	private long stableSince;

	public Fire() {
		super(
				printString.of("Fire"),
				printString.of("Changes the height and color of fire"),
				-1,
				Category.RENDER
		);

		addSettings(height, recolor, color);
		instance = this;

		ModelLoadingPlugin.register(plugin -> plugin.modifyBlockModelAfterBake().register((model, context) ->
				context.state().getBlock() instanceof BaseFireBlock ? wrap(model) : model));
	}

	@Override
	public void onEnable() {
		eventManager.add(HudListener.class, this);
		super.onEnable();
		rebuild();
	}

	@Override
	public void onDisable() {
		eventManager.remove(HudListener.class, this);
		super.onDisable();
		rebuild();
	}

	@Override
	public void onRenderHud(HudEvent event) {
		if (height.getValue() == lastHeight && tint() == lastTint) {
			stableSince = System.currentTimeMillis();
			return;
		}
		if (System.currentTimeMillis() - stableSince < 300) return;

		rebuild();
	}

	private void rebuild() {
		lastHeight = height.getValue();
		lastTint = tint();
		if (mc.levelRenderer != null)
			mc.levelRenderer.allChanged();
	}

	public static boolean isActive() {
		return instance != null && instance.isEnabled();
	}

	private int tint() {
		return recolor.getValue() ? color.getValue().getRGB() : -1;
	}

	private boolean seeThrough() {
		return isEnabled() && tint() >>> 24 < 255;
	}

	private boolean transform(MutableQuadView quad) {
		float top = height.getValueFloat();
		if (quad.y(0) > top && quad.y(1) > top && quad.y(2) > top && quad.y(3) > top) return false;

		if (recolor.getValue())
			graySprite(quad);

		for (int i = 0; i < 4; i++) {
			if (quad.y(i) <= top) continue;
			int low = quad.y((i + 1) % 4) < quad.y((i + 3) % 4) ? (i + 1) % 4 : (i + 3) % 4;
			float t = (top - quad.y(low)) / (quad.y(i) - quad.y(low));
			quad.pos(i, Mth.lerp(t, quad.x(low), quad.x(i)), top, Mth.lerp(t, quad.z(low), quad.z(i)));
			quad.uv(i, Mth.lerp(t, quad.u(low), quad.u(i)), Mth.lerp(t, quad.v(low), quad.v(i)));
		}

		quad.multiplyColor(tint());
		if (seeThrough())
			quad.chunkLayer(ChunkSectionLayer.TRANSLUCENT);
		return true;
	}

	private void graySprite(MutableQuadView quad) {
		TextureAtlas atlas = mc.getAtlasManager().getAtlasOrThrow(AtlasIds.BLOCKS);
		float u = (quad.u(0) + quad.u(2)) / 2;
		float v = (quad.v(0) + quad.v(2)) / 2;

		for (String texture : TEXTURES) {
			Identifier id = Identifier.withDefaultNamespace(texture);
			TextureAtlasSprite sprite = atlas.getSprite(id);
			TextureAtlasSprite gray = atlas.getSprite(grayId(id));
			if (gray == atlas.missingSprite() || u < sprite.getU0() || u > sprite.getU1() || v < sprite.getV0() || v > sprite.getV1()) continue;

			for (int i = 0; i < 4; i++)
				quad.uv(i, quad.u(i) - sprite.getU0() + gray.getU0(), quad.v(i) - sprite.getV0() + gray.getV0());
			return;
		}
	}

	private static Identifier grayId(Identifier id) {
		return Identifier.fromNamespaceAndPath("argonvisuals", id.getPath() + "_gray");
	}

	private static InputStream gray(Resource resource) throws IOException {
		BufferedImage image;
		try (InputStream in = resource.open()) {
			image = ImageIO.read(in);
		}

		int max = 1;
		for (int x = 0; x < image.getWidth(); x++)
			for (int y = 0; y < image.getHeight(); y++)
				if (image.getRGB(x, y) >>> 24 != 0)
					max = Math.max(max, luma(image.getRGB(x, y)));

		BufferedImage gray = new BufferedImage(image.getWidth(), image.getHeight(), BufferedImage.TYPE_INT_ARGB);
		for (int x = 0; x < image.getWidth(); x++) {
			for (int y = 0; y < image.getHeight(); y++) {
				int pixel = image.getRGB(x, y);
				int l = Math.min(255, luma(pixel) * 255 / max);
				gray.setRGB(x, y, pixel & 0xFF000000 | l << 16 | l << 8 | l);
			}
		}

		ByteArrayOutputStream out = new ByteArrayOutputStream();
		ImageIO.write(gray, "png", out);
		return new ByteArrayInputStream(out.toByteArray());
	}

	private static int luma(int pixel) {
		return ((pixel >> 16 & 255) * 3 + (pixel >> 8 & 255) * 6 + (pixel & 255)) / 10;
	}

	public BlockStateModel wrap(BlockStateModel model) {
		return new BlockStateModel() {
			@Override
			public void collectParts(RandomSource random, List<BlockStateModelPart> parts) {
				model.collectParts(random, parts);
			}

			@Override
			public void emitQuads(QuadEmitter emitter, BlockAndTintGetter level, BlockPos pos, BlockState state, RandomSource random, Predicate<Direction> cullTest) {
				if (!isEnabled()) {
					model.emitQuads(emitter, level, pos, state, random, cullTest);
					return;
				}

				emitter.pushTransform(Fire.this::transform);
				model.emitQuads(emitter, level, pos, state, random, cullTest);
				emitter.popTransform();
			}

			@Override
			public Material.Baked particleMaterial() {
				return model.particleMaterial();
			}

			@Override
			public int materialFlags() {
				return seeThrough() ? model.materialFlags() | BakedQuad.FLAG_TRANSLUCENT : model.materialFlags();
			}
		};
	}
}
