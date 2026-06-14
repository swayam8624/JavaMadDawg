package com.singal.maddog.world;

import com.singal.maddog.ai.NavigationGrid;
import com.singal.maddog.renderer.RenderContext;
import java.util.HashMap;
import java.util.Map;

/**
 * Manages a grid of tiles, performing frustum culling during rendering.
 */
public class TileMap implements NavigationGrid {
    private final int width;
    private final int height;
    private final int[] tiles;
    private final Map<Integer, Tile> tileTemplates = new HashMap<>();
    private Tile voidTile;

    public TileMap(int width, int height) {
        this.width = width;
        this.height = height;
        this.tiles = new int[width * height];
    }

    public void registerTileTemplate(Tile tile) {
        tileTemplates.put(tile.getId(), tile);
    }

    public void setVoidTile(Tile voidTile) {
        this.voidTile = voidTile;
    }

    public void setTile(int x, int y, int tileId) {
        if (x >= 0 && x < width && y >= 0 && y < height) {
            tiles[x + y * width] = tileId;
        }
    }

    public int getTileId(int x, int y) {
        if (x < 0 || x >= width || y < 0 || y >= height) {
            return -1;
        }
        return tiles[x + y * width];
    }

    public Tile getTile(int x, int y) {
        if (x < 0 || x >= width || y < 0 || y >= height) {
            return voidTile;
        }
        int id = tiles[x + y * width];
        return tileTemplates.getOrDefault(id, voidTile);
    }

    @Override
    public boolean isSolid(int x, int y) {
        return getTile(x, y).isSolid();
    }

    public void render(RenderContext context) {
        int xOffset = context.getXOffset();
        int yOffset = context.getYOffset();

        // Calculate tile index boundaries for frustum culling
        int startX = Math.max(0, xOffset / Tile.SIZE);
        int endX = Math.min(width, (xOffset + context.getWidth() + Tile.SIZE) / Tile.SIZE);
        int startY = Math.max(0, yOffset / Tile.SIZE);
        int endY = Math.min(height, (yOffset + context.getHeight() + Tile.SIZE) / Tile.SIZE);

        for (int y = startY; y < endY; y++) {
            for (int x = startX; x < endX; x++) {
                Tile tile = getTile(x, y);
                if (tile != null) {
                    tile.render(x, y, context);
                }
            }
        }
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public int getWidthInPixels() {
        return width * Tile.SIZE;
    }

    public int getHeightInPixels() {
        return height * Tile.SIZE;
    }
}
