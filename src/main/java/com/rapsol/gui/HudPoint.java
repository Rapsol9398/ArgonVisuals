package com.rapsol.gui;

public final class HudPoint {

    public enum Anchor { START, END }

    public Anchor anchorX = Anchor.START;
    public Anchor anchorY = Anchor.START;
    public double offsetX;
    public double offsetY;
    public double scale;

    public HudPoint(double offsetX, double offsetY, double scale) {
        this.offsetX = offsetX;
        this.offsetY = offsetY;
        this.scale = scale;
    }

    public int resolveX(int guiWidth, int elementWidth) {
        return anchorX == Anchor.START
                ? (int) offsetX
                : guiWidth - (int) offsetX - elementWidth;
    }

    public int resolveY(int guiHeight, int elementHeight) {
        return anchorY == Anchor.START
                ? (int) offsetY
                : guiHeight - (int) offsetY - elementHeight;
    }

    public void setFromAbsolute(double x, double y, int guiWidth, int guiHeight, int elementWidth, int elementHeight) {
        double centerX = x + elementWidth / 2.0;
        double centerY = y + elementHeight / 2.0;

        if (centerX < guiWidth / 2.0) {
            anchorX = Anchor.START;
            offsetX = x;
        } else {
            anchorX = Anchor.END;
            offsetX = guiWidth - (x + elementWidth);
        }

        if (centerY < guiHeight / 2.0) {
            anchorY = Anchor.START;
            offsetY = y;
        } else {
            anchorY = Anchor.END;
            offsetY = guiHeight - (y + elementHeight);
        }
    }
}