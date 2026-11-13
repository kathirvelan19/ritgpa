package com.rit.gpa.pdf;

public class TextFragment {
    private final String text;
    private final float x;
    private final float y;
    private final float width;
    private final float height;

    public TextFragment(String text, float x, float y, float width, float height) {
        this.text = text != null ? text.replace('\u00a0', ' ').trim() : "";
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
    }

    public String getText() {
        return text;
    }

    public float getX() {
        return x;
    }

    public float getY() {
        return y;
    }

    public float getWidth() {
        return width;
    }

    public float getHeight() {
        return height;
    }

    public float getXEnd() {
        return x + width;
    }

    public float getYEnd() {
        return y + height;
    }

    @Override
    public String toString() {
        return String.format("[%s @ (%.1f, %.1f) w=%.1f h=%.1f]", text, x, y, width, height);
    }
}
