package com.singal.maddog.renderer;

/**
 * A Sprite holds a pixel buffer representing a 2D image,
 * ready to be rendered onto a target pixel buffer.
 */
public class Sprite {
    private final int width;
    private final int height;
    private final int[] pixels;

    public Sprite(int[] pixels, int width, int height) {
        this.width = width;
        this.height = height;
        this.pixels = pixels;
    }

    public Sprite(int size, int color) {
        this.width = size;
        this.height = size;
        this.pixels = new int[size * size];
        fillColor(color);
    }

    public Sprite(int width, int height, int color) {
        this.width = width;
        this.height = height;
        this.pixels = new int[width * height];
        fillColor(color);
    }

    private void fillColor(int color) {
        for (int i = 0; i < pixels.length; i++) {
            pixels[i] = color;
        }
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public int[] getPixels() {
        return pixels;
    }
}
