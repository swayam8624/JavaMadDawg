package com.singal.maddog.renderer;

import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;

/**
 * Represents a raw graphical texture. Holds pixel data for software blitting.
 */
public class Texture {
    private final int width;
    private final int height;
    private final int[] pixels;

    public Texture(BufferedImage image) {
        this.width = image.getWidth();
        this.height = image.getHeight();
        
        // Ensure image is TYPE_INT_RGB or TYPE_INT_ARGB
        if (image.getType() == BufferedImage.TYPE_INT_RGB || image.getType() == BufferedImage.TYPE_INT_ARGB) {
            this.pixels = ((DataBufferInt) image.getRaster().getDataBuffer()).getData();
        } else {
            // Convert to TYPE_INT_ARGB if not already in correct format
            BufferedImage converted = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
            converted.getGraphics().drawImage(image, 0, 0, null);
            this.pixels = ((DataBufferInt) converted.getRaster().getDataBuffer()).getData();
        }
    }

    public Texture(int width, int height, int[] pixels) {
        this.width = width;
        this.height = height;
        this.pixels = pixels;
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
