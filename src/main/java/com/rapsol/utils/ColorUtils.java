package com.rapsol.utils;

import com.mojang.serialization.MapCodec;
import net.minecraft.client.renderer.texture.atlas.SpriteSource;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

public final class ColorUtils {
	public static Color getBreathingRGBColor(int increment, int alpha) {
		Color color = Color.getHSBColor((((((System.currentTimeMillis()) * 3) + increment * 175) % (360 * 20)) / (360f * 20)), 0.6f, 1f);
		return new Color(color.getRed(), color.getGreen(), color.getBlue(), alpha);
	}

	public static Color getMainColor(Color color, int n, int n2) {
		float[] fArray = new float[3];
		Color.RGBtoHSB(color.getRed(), color.getGreen(), color.getBlue(), fArray);

		float f = Math.abs(((float) (System.currentTimeMillis() % 2000) / 1000 + (float) n / (float) n2 * 2) % 2 - 1);
		fArray[2] = 0.25f + 0.75f * f % 2;

		int rgb = Color.HSBtoRGB(fArray[0], fArray[1], fArray[2]);
		return new Color((rgb >> 16) & 0xFF, (rgb >> 8) & 0xFF, rgb & 0xFF, color.getAlpha());
	}

	public static Color smoothColorTransition(float speed, Color toColor, Color fromColor) {
		return new Color((int) MathUtils.goodLerp(speed, fromColor.getRed(), toColor.getRed()),
                (int) MathUtils.goodLerp(speed, fromColor.getGreen(), toColor.getGreen()),
                (int) MathUtils.goodLerp(speed, fromColor.getBlue(), toColor.getBlue()));
	}

	public static Color smoothAlphaTransition(float speed, int toAlpha, Color fromColor) {
		return new Color(fromColor.getRed(),
				fromColor.getGreen(),
				fromColor.getBlue(),
                (int) MathUtils.goodLerp(speed, fromColor.getAlpha(), toAlpha));
	}

	public static SpriteSource graySprites(List<String> textures) {
		return new SpriteSource() {
			@Override
			public void run(ResourceManager manager, Output output) {
				for (String texture : textures) {
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
	}

	public static Identifier grayId(Identifier id) {
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
}
