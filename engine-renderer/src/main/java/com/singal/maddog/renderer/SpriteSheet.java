package com.singal.maddog.renderer;

/**
 * A SpriteSheet partitions a single Texture into multiple individual Sprites.
 */
public class SpriteSheet {
    private final Texture texture;
    private final int spriteWidth;
    private final int spriteHeight;
    private Sprite[] sprites;

    public SpriteSheet(Texture texture, int spriteSize) {
        this(texture, spriteSize, spriteSize);
    }

    public SpriteSheet(Texture texture, int spriteWidth, int spriteHeight) {
        this.texture = texture;
        this.spriteWidth = spriteWidth;
        this.spriteHeight = spriteHeight;
        sliceSheet();
    }

    private void sliceSheet() {
        int cols = texture.getWidth() / spriteWidth;
        int rows = texture.getHeight() / spriteHeight;
        int numSprites = cols * rows;
        sprites = new Sprite[numSprites];

        int[] sheetPixels = texture.getPixels();
        int sheetWidth = texture.getWidth();

        for (int y = 0; y < rows; y++) {
            for (int x = 0; x < cols; x++) {
                int[] spritePixels = new int[spriteWidth * spriteHeight];
                for (int sy = 0; sy < spriteHeight; sy++) {
                    int globalY = y * spriteHeight + sy;
                    for (int sx = 0; sx < spriteWidth; sx++) {
                        int globalX = x * spriteWidth + sx;
                        spritePixels[sx + sy * spriteWidth] = sheetPixels[globalX + globalY * sheetWidth];
                    }
                }
                sprites[x + y * cols] = new Sprite(spritePixels, spriteWidth, spriteHeight);
            }
        }
    }

    /**
     * Grabs a single sprite from the spritesheet using grid coordinates.
     */
    public Sprite getSprite(int xGrid, int yGrid) {
        int cols = texture.getWidth() / spriteWidth;
        int index = xGrid + yGrid * cols;
        if (index < 0 || index >= sprites.length) {
            // Return a blank default sprite if index is out of bounds
            return new Sprite(spriteWidth, spriteHeight, 0xFFFF00FF);
        }
        return sprites[index];
    }

    /**
     * Slices out a sub-spritesheet using a subset of columns/rows.
     * Useful for extracting directional animation frames from a larger sheet.
     */
    public SpriteSheet getSubSheet(int xGridStart, int yGridStart, int colsCount, int rowsCount) {
        int subWidth = colsCount * spriteWidth;
        int subHeight = rowsCount * spriteHeight;
        int[] subPixels = new int[subWidth * subHeight];
        
        int[] sheetPixels = texture.getPixels();
        int sheetWidth = texture.getWidth();
        int startX = xGridStart * spriteWidth;
        int startY = yGridStart * spriteHeight;

        for (int y = 0; y < subHeight; y++) {
            int globalY = startY + y;
            for (int x = 0; x < subWidth; x++) {
                int globalX = startX + x;
                subPixels[x + y * subWidth] = sheetPixels[globalX + globalY * sheetWidth];
            }
        }

        Texture subTexture = new Texture(subWidth, subHeight, subPixels);
        return new SpriteSheet(subTexture, spriteWidth, spriteHeight);
    }

    public Sprite[] getSprites() {
        return sprites;
    }

    public int getSpriteWidth() {
        return spriteWidth;
    }

    public int getSpriteHeight() {
        return spriteHeight;
    }

    public int getWidth() {
        return texture.getWidth();
    }

    public int getHeight() {
        return texture.getHeight();
    }
}
