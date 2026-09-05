package com.rapsol.utils;

import com.rapsol.font.Fonts;
import com.rapsol.module.modules.client.ClickGUI;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import org.joml.Matrix3x2fStack;

import static com.rapsol.Argon.mc;


public final class TextRenderer {
	public static void text(CharSequence string, GuiGraphicsExtractor context, int x, int y, int color) {
		drawString(string, context, x, y, color);
	}

	public static void centeredText(CharSequence string, GuiGraphicsExtractor context, int x, int y, int color) {
		drawCenteredString(string, context, x, y, color);
	}

	public static void drawString(CharSequence string, GuiGraphicsExtractor context, int x, int y, int color) {
		boolean custom = ClickGUI.customFont.getValue();
		if (custom)
			Fonts.QUICKSAND.drawString(context, string, x, y - 8, color);
		else drawMinecraftText(string, context, x, y, color);
	}

	public static int getWidth(CharSequence string) {
		boolean custom = ClickGUI.customFont.getValue();
		if (custom)
			return Fonts.QUICKSAND.getStringWidth(string);
		else return mc.font.width(string.toString()) * 2;
	}

	public static void drawCenteredString(CharSequence string, GuiGraphicsExtractor context, int x, int y, int color) {
		boolean custom = ClickGUI.customFont.getValue();
		if (custom)
			Fonts.QUICKSAND.drawString(context, string, (x - (Fonts.QUICKSAND.getStringWidth(string) / 2)), y - 8, color);
		else drawCenteredMinecraftText(string, context, x, y, color);
	}

	public static void drawLargeString(CharSequence string, GuiGraphicsExtractor context, int x, int y, int color) {
		boolean custom = ClickGUI.customFont.getValue();
		if (custom) {
			Matrix3x2fStack matrices = context.pose();
			matrices.pushMatrix();

			matrices.scale(1.4f, 1.4f);
			Fonts.QUICKSAND.drawString(context, string, x, y - 8, color);

			matrices.popMatrix();
		} else
			drawLargerMinecraftText(string, context, x, y, color);
	}

	public static void drawMinecraftText(CharSequence string, GuiGraphicsExtractor context, int x, int y, int color) {
		Matrix3x2fStack matrices = context.pose();
		matrices.pushMatrix();

		matrices.scale(2f, 2f);
		context.text(mc.font, string.toString(), (x) / 2, (y) / 2, color, false);

		matrices.popMatrix();
	}

	public static void drawLargerMinecraftText(CharSequence string, GuiGraphicsExtractor context, int x, int y, int color) {
		Matrix3x2fStack matrices = context.pose();
		matrices.pushMatrix();

		matrices.scale(3, 3);
		context.text(mc.font, (String) string, (x) / 3, (y) / 3, color, false);

		matrices.popMatrix();
	}

	public static void drawCenteredMinecraftText(CharSequence string, GuiGraphicsExtractor context, int x, int y, int color) {
		Matrix3x2fStack matrices = context.pose();
		matrices.pushMatrix();

		matrices.scale(2f, 2f);
		context.text(mc.font, (String) string, (x / 2) - (mc.font.width((String) string) / 2), (y) / 2, color, false);

		matrices.popMatrix();
	}
}
