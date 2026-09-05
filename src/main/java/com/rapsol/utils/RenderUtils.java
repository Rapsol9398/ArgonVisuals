package com.rapsol.utils;

import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.client.gui.render.TextureSetup;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.state.gui.GuiElementRenderState;
import net.minecraft.util.Mth;
import org.joml.Matrix3x2f;
import org.joml.Matrix3x2fc;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

import static com.rapsol.Argon.mc;

public final class RenderUtils {
	public static boolean rendering3D = true;

	public static void unscaledProjection(GuiGraphicsExtractor context) {
		float s = (float)(1.0 / mc.getWindow().getGuiScale());
		context.pose().pushMatrix();
		context.pose().scale(s, s);
		rendering3D = false;
	}

	public static void scaledProjection(GuiGraphicsExtractor context) {
		context.pose().popMatrix();
		rendering3D = true;
	}
	public static void renderRoundedQuad(GuiGraphicsExtractor context, Color c,
	                                     double x, double y, double x2, double y2,
	                                     double c1, double c2, double c3, double c4, double samples) {
		addGuiMesh(context, RenderPipelines.GUI, TextureSetup.noTexture(),
				fanToQuads(c, x, y, x2, y2, c1, c2, c3, c4, samples), x, y, x2, y2);
	}

	public static void renderRoundedQuad(GuiGraphicsExtractor context, Color c,
	                                     double x, double y, double x1, double y1, double rad, double samples) {
		renderRoundedQuad(context, c, x, y, x1, y1, rad, rad, rad, rad, samples);
	}

	public static void renderCircle(GuiGraphicsExtractor context, Color c,
	                                double originX, double originY, double rad, int segments) {
		int steps = Mth.clamp(segments, 4, 360);
		Vertex center = new Vertex((float)originX, (float)originY, c.getRGB());
		List<Vertex> rim = new ArrayList<>();
		for (int i = 0; i <= 360; i += Math.max(1, 360/steps)) {
			double r = Math.toRadians(i);
			rim.add(new Vertex((float)(originX+Math.sin(r)*rad), (float)(originY+Math.cos(r)*rad), c.getRGB()));
		}
		List<Vertex> out = new ArrayList<>();
		for (int i = 0; i < rim.size()-1; i++) {
			out.add(center); out.add(rim.get(i));
			out.add(rim.get(i+1)); out.add(rim.get(i+1));
		}
		addGuiMesh(context, RenderPipelines.GUI, TextureSetup.noTexture(), out,
				originX-rad, originY-rad, originX+rad, originY+rad);
	}

	public static void renderRoundedOutline(GuiGraphicsExtractor context, Color c,
	                                        double fromX, double fromY, double toX, double toY,
	                                        double r1, double r2, double r3, double r4, double width, double samples) {
		addGuiMesh(context, RenderPipelines.GUI, TextureSetup.noTexture(),
				stripToQuads(buildRoundedOutline(c, fromX, fromY, toX, toY, r1, r2, r3, r4, width, samples)),
				fromX-width, fromY-width, toX+width, toY+width);
	}

	private static List<Vertex> fanToQuads(Color c,
	                                       double x, double y, double x2, double y2,
	                                       double c1, double c2, double c3, double c4, double samples) {
		Vertex center = new Vertex((float)((x+x2)/2), (float)((y+y2)/2), c.getRGB());
		List<Vertex> rim = new ArrayList<>();
		double[][] map = {
				{x2-c4, y2-c4, c4}, {x2-c2, y+c2, c2},
				{x+c1,  y+c1,  c1}, {x+c3,  y2-c3, c3}
		};
		for (int i = 0; i < 4; i++) {
			double[] cur = map[i]; double rad = cur[2];
			for (double r = i*90d; r < 90d+i*90d; r += 90d/samples) {
				float rad1 = (float)Math.toRadians(r);
				rim.add(new Vertex((float)cur[0]+(float)(Math.sin(rad1)*rad),
						(float)cur[1]+(float)(Math.cos(rad1)*rad), c.getRGB()));
			}
			float rad1 = (float)Math.toRadians(90d+i*90d);
			rim.add(new Vertex((float)cur[0]+(float)(Math.sin(rad1)*rad),
					(float)cur[1]+(float)(Math.cos(rad1)*rad), c.getRGB()));
		}
		rim.add(rim.get(0));

		List<Vertex> out = new ArrayList<>();
		for (int i = 0; i < rim.size()-1; i++) {
			out.add(center);
			out.add(rim.get(i));
			out.add(rim.get(i+1));
			out.add(rim.get(i+1));
		}
		return out;
	}

	private static List<Vertex> stripToQuads(List<Vertex> strip) {
		List<Vertex> out = new ArrayList<>();
		for (int i = 0; i < strip.size()-2; i += 2) {
			if (i+3 < strip.size()) {
				out.add(strip.get(i));
				out.add(strip.get(i+1));
				out.add(strip.get(i+3));
				out.add(strip.get(i+2));
			}
		}
		return out;
	}

	private static List<Vertex> buildRoundedOutline(Color c,
	                                                double fromX, double fromY, double toX, double toY,
	                                                double rC1, double rC2, double rC3, double rC4, double width, double samples) {
		List<Vertex> strip = new ArrayList<>();
		double[][] map = {
				{toX - rC4, toY - rC4, rC4},
				{toX - rC2, fromY + rC2, rC2},
				{fromX + rC1, fromY + rC1, rC1},
				{fromX + rC3, toY - rC3, rC3}
		};
		for (int i = 0; i < 4; i++) {
			double[] cur = map[i];
			double rad = cur[2];
			for (double r = i * 90d; r < 90d + i * 90d; r += 90d / samples)
				addOutlinePair(strip, cur, rad, width, r, c);
			addOutlinePair(strip, cur, rad, width, 90d + i * 90d, c);
		}
		double[] cur = map[0];
		strip.add(new Vertex((float) cur[0], (float)(cur[1] + cur[2]), c.getRGB()));
		strip.add(new Vertex((float) cur[0], (float)(cur[1] + cur[2] + width), c.getRGB()));
		return strip;
	}

	private static void addOutlinePair(List<Vertex> v, double[] cur, double rad, double width, double deg, Color c) {
		float radians = (float) Math.toRadians(deg);
		double sin = Math.sin(radians), cos = Math.cos(radians);
		v.add(new Vertex((float)(cur[0] + sin * rad),         (float)(cur[1] + cos * rad),         c.getRGB()));
		v.add(new Vertex((float)(cur[0] + sin * (rad+width)), (float)(cur[1] + cos * (rad+width)), c.getRGB()));
	}

	private static List<Vertex> stripToTriangles(List<Vertex> strip) {
		List<Vertex> out = new ArrayList<>();
		for (int i = 0; i < strip.size() - 2; i++) {
			if (i % 2 == 0) {
				out.add(strip.get(i));
				out.add(strip.get(i + 1));
				out.add(strip.get(i + 2));
			} else {
				out.add(strip.get(i + 1));
				out.add(strip.get(i));
				out.add(strip.get(i + 2));
			}
		}
		return out;
	}

	public static void addGuiMesh(GuiGraphicsExtractor context, RenderPipeline pipeline,
	                              TextureSetup textureSetup, List<Vertex> vertices,
	                              double minX, double minY, double maxX, double maxY) {
		context.guiRenderState.addGuiElement(new MeshRenderState(
				pipeline, textureSetup,
				new Matrix3x2f(context.pose()),
				vertices,
				new ScreenRectangle(0, 0, 100000, 100000),
				context.scissorStack.peek()
		));
	}

	public record Vertex(float x, float y, int color, float u, float v) {
		public Vertex(float x, float y, int color) { this(x, y, color, 0f, 0f); }
	}

	private record MeshRenderState(
			RenderPipeline pipeline, TextureSetup textureSetup,
			Matrix3x2fc pose, List<Vertex> vertices,
			ScreenRectangle bounds, ScreenRectangle scissorArea
	) implements GuiElementRenderState {
		@Override
		public void buildVertices(VertexConsumer consumer) {
			for (Vertex v : vertices)
				consumer.addVertexWith2DPose(pose, v.x(), v.y())
						.setUv(v.u(), v.v())
						.setColor(v.color());
		}
	}
}