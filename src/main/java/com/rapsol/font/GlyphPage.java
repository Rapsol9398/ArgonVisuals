package com.rapsol.font;

import com.mojang.blaze3d.platform.NativeImage;
import com.rapsol.utils.RenderUtils;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.render.TextureSetup;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.texture.AbstractTexture;
import net.minecraft.client.renderer.texture.DynamicTexture;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.font.FontRenderContext;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.List;

public final class GlyphPage {

	private int imgSize;
	private int maxFontHeight = -1;
	private final Font font;
	private final boolean antiAliasing;
	private final boolean fractionalMetrics;
	private final HashMap<Character, Glyph> glyphCharacterMap = new HashMap<>();

	private BufferedImage bufferedImage;
	private AbstractTexture loadedTexture;

	public GlyphPage(Font font, boolean antiAliasing, boolean fractionalMetrics) {
		this.font = font;
		this.antiAliasing = antiAliasing;
		this.fractionalMetrics = fractionalMetrics;
	}

	public void generateGlyphPage(char[] chars) {
		double maxWidth = -1;
		double maxHeight = -1;

		AffineTransform affineTransform = new AffineTransform();
		FontRenderContext fontRenderContext = new FontRenderContext(affineTransform, antiAliasing, fractionalMetrics);

		for (char ch : chars) {
			Rectangle2D bounds = font.getStringBounds(Character.toString(ch), fontRenderContext);

			if (maxWidth < bounds.getWidth())
				maxWidth = bounds.getWidth();
			if (maxHeight < bounds.getHeight())
				maxHeight = bounds.getHeight();
		}

		maxWidth += 2;
		maxHeight += 2;

		imgSize = (int) Math.ceil(Math.max(Math.ceil(Math.sqrt(maxWidth * maxWidth * chars.length) / maxWidth),
				Math.ceil(Math.sqrt(maxHeight * maxHeight * chars.length) / maxHeight)) * Math.max(maxWidth, maxHeight))
				+ 1;

		bufferedImage = new BufferedImage(imgSize, imgSize, BufferedImage.TYPE_INT_ARGB);

		Graphics2D g = bufferedImage.createGraphics();

		g.setFont(font);
		g.setColor(new Color(255, 255, 255, 0));
		g.fillRect(0, 0, imgSize, imgSize);

		g.setColor(Color.white);

		g.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS, fractionalMetrics ? RenderingHints.VALUE_FRACTIONALMETRICS_ON : RenderingHints.VALUE_FRACTIONALMETRICS_OFF);
		g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, antiAliasing ? RenderingHints.VALUE_ANTIALIAS_ON : RenderingHints.VALUE_ANTIALIAS_OFF);
		g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, antiAliasing ? RenderingHints.VALUE_TEXT_ANTIALIAS_ON : RenderingHints.VALUE_TEXT_ANTIALIAS_OFF);

		FontMetrics fontMetrics = g.getFontMetrics();

		int currentCharHeight = 0;
		int posX = 0;
		int posY = 1;

		for (char ch : chars) {
			Glyph glyph = new Glyph();

			Rectangle2D bounds = fontMetrics.getStringBounds(Character.toString(ch), g);

			glyph.width = bounds.getBounds().width + 8;
			glyph.height = bounds.getBounds().height;

			if (posX + glyph.width >= imgSize) {
				posX = 0;
				posY += currentCharHeight;
				currentCharHeight = 0;
			}

			glyph.x = posX;
			glyph.y = posY;

			if (glyph.height > maxFontHeight)
				maxFontHeight = glyph.height;

			if (glyph.height > currentCharHeight)
				currentCharHeight = glyph.height;

			g.drawString(Character.toString(ch), posX + 2, posY + fontMetrics.getAscent());

			posX += glyph.width;

			glyphCharacterMap.put(ch, glyph);

		}
	}

	public void setupTexture() {
		try {
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			ImageIO.write(bufferedImage, "png", baos);
			byte[] bytes = baos.toByteArray();

			ByteBuffer direct = ByteBuffer.allocateDirect(bytes.length);
			direct.put(bytes);
			direct.flip();

			NativeImage nativeImage = NativeImage.read(direct);
			loadedTexture = new DynamicTexture(() -> "argon-glyph-page", nativeImage);
		} catch (Exception e) {
			System.err.println("[Argon] GlyphPage.setupTexture() failed: " + e.getMessage());
			e.printStackTrace();
		}
	}

	public float drawChar(GuiGraphicsExtractor context, char ch, float x, float y, float red, float blue, float green, float alpha) {
		if (loadedTexture == null)
			setupTexture();
		if (loadedTexture == null)
			return 0;
		Glyph glyph = glyphCharacterMap.get(ch);
		if (glyph == null) return 0;

		float pageX = glyph.x / (float) imgSize;
		float pageY = glyph.y / (float) imgSize;

		float pageWidth = glyph.width / (float) imgSize;
		float pageHeight = glyph.height / (float) imgSize;

		float width = glyph.width;
		float height = glyph.height;

		int color = ((int) (alpha * 255.0F) & 255) << 24
				| ((int) (red * 255.0F) & 255) << 16
				| ((int) (green * 255.0F) & 255) << 8
				| ((int) (blue * 255.0F) & 255);

		RenderUtils.addGuiMesh(
				context,
				RenderPipelines.GUI_TEXTURED,
				TextureSetup.singleTexture(loadedTexture.getTextureView(), loadedTexture.getSampler()),
				List.of(
						new RenderUtils.Vertex(x, y + height, color, pageX, pageY + pageHeight),
						new RenderUtils.Vertex(x + width, y + height, color, pageX + pageWidth, pageY + pageHeight),
						new RenderUtils.Vertex(x + width, y, color, pageX + pageWidth, pageY),
						new RenderUtils.Vertex(x, y, color, pageX, pageY)
				),
				x, y, x + width, y + height
		);

		return width - 8;
	}

	public float getWidth(char ch) {
		return glyphCharacterMap.get(ch).width;
	}

	public boolean isAntiAliasingEnabled() {
		return antiAliasing;
	}

	public boolean isFractionalMetricsEnabled() {
		return fractionalMetrics;
	}

	static class Glyph {
		private int x;
		private int y;
		private int width;
		private int height;

		Glyph(int x, int y, int width, int height) {
			this.x = x;
			this.y = y;
			this.width = width;
			this.height = height;
		}

		Glyph() {
		}

	}

	public int getMaxFontHeight() {
		return maxFontHeight;
	}
}
