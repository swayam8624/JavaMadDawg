package com.singal.maddog.renderer;

/**
 * A software pixel-pushing implementation of {@link RenderContext}.
 * Renders directly into a 1D flat integer pixel array (ARgb).
 */
public class SoftwareRenderContext implements RenderContext {
    private final int width;
    private final int height;
    private final int[] pixels;
    
    private int xOffset;
    private int yOffset;

    // Transparency color key: Magenta (0xFFFF00FF)
    private static final int ALPHA_COLOR_KEY = 0xFFFF00FF;

    public SoftwareRenderContext(int width, int height, int[] pixels) {
        this.width = width;
        this.height = height;
        this.pixels = pixels;
    }

    @Override
    public void clear(int color) {
        for (int i = 0; i < pixels.length; i++) {
            pixels[i] = color;
        }
    }

    @Override
    public void setCameraOffset(int xOffset, int yOffset) {
        this.xOffset = xOffset;
        this.yOffset = yOffset;
    }

    @Override
    public int getXOffset() {
        return xOffset;
    }

    @Override
    public int getYOffset() {
        return yOffset;
    }

    @Override
    public void drawPixel(int x, int y, int color, boolean useCamera) {
        if (useCamera) {
            x -= xOffset;
            y -= yOffset;
        }

        if (x < 0 || x >= width || y < 0 || y >= height) return;
        
        // Skip rendering transparent color key
        if ((color & 0xFFFFFF) == (ALPHA_COLOR_KEY & 0xFFFFFF)) return;

        pixels[x + y * width] = color;
    }

    @Override
    public void drawRect(int x, int y, int rectWidth, int rectHeight, int color, boolean useCamera) {
        if (useCamera) {
            x -= xOffset;
            y -= yOffset;
        }

        for (int xp = x; xp < x + rectWidth; xp++) {
            if (xp >= 0 && xp < width) {
                if (y >= 0 && y < height) pixels[xp + y * width] = color;
                if (y + rectHeight - 1 >= 0 && y + rectHeight - 1 < height) {
                    pixels[xp + (y + rectHeight - 1) * width] = color;
                }
            }
        }

        for (int yp = y; yp < y + rectHeight; yp++) {
            if (yp >= 0 && yp < height) {
                if (x >= 0 && x < width) pixels[x + yp * width] = color;
                if (x + rectWidth - 1 >= 0 && x + rectWidth - 1 < width) {
                    pixels[(x + rectWidth - 1) + yp * width] = color;
                }
            }
        }
    }

    @Override
    public void fillRect(int x, int y, int rectWidth, int rectHeight, int color, boolean useCamera) {
        if (useCamera) {
            x -= xOffset;
            y -= yOffset;
        }

        for (int yp = 0; yp < rectHeight; yp++) {
            int globalY = y + yp;
            if (globalY < 0 || globalY >= height) continue;
            
            for (int xp = 0; xp < rectWidth; xp++) {
                int globalX = x + xp;
                if (globalX < 0 || globalX >= width) continue;

                pixels[globalX + globalY * width] = color;
            }
        }
    }

    @Override
    public void drawSprite(int x, int y, Sprite sprite, boolean useCamera) {
        drawSprite(x, y, sprite, false, false, useCamera);
    }

    @Override
    public void drawSprite(int x, int y, Sprite sprite, boolean flipX, boolean flipY, boolean useCamera) {
        if (useCamera) {
            x -= xOffset;
            y -= yOffset;
        }

        int spriteWidth = sprite.getWidth();
        int spriteHeight = sprite.getHeight();
        int[] spritePixels = sprite.getPixels();

        for (int sy = 0; sy < spriteHeight; sy++) {
            int targetY = y + sy;
            if (targetY < 0 || targetY >= height) continue;
            
            int sourceY = flipY ? (spriteHeight - 1 - sy) : sy;

            for (int sx = 0; sx < spriteWidth; sx++) {
                int targetX = x + sx;
                if (targetX < 0 || targetX >= width) continue;

                int sourceX = flipX ? (spriteWidth - 1 - sx) : sx;

                int color = spritePixels[sourceX + sourceY * spriteWidth];
                
                // Transparency check
                if ((color & 0xFFFFFF) == (ALPHA_COLOR_KEY & 0xFFFFFF)) continue;

                pixels[targetX + targetY * width] = color;
            }
        }
    }

    @Override
    public int getWidth() {
        return width;
    }

    @Override
    public int getHeight() {
        return height;
    }
}
