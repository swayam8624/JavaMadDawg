package com.singal.maddog.world;

import com.singal.maddog.renderer.RenderContext;
import com.singal.maddog.renderer.Sprite;

/**
 * Defines a static square block in the world grid.
 */
public class Tile {
    public static final int SIZE = 16; // 16x16 pixels

    private final int id;
    private final Sprite sprite;
    private final boolean solid;

    public Tile(int id, Sprite sprite, boolean solid) {
        this.id = id;
        this.sprite = sprite;
        this.solid = solid;
    }

    public void render(int xGrid, int yGrid, RenderContext context) {
        if (sprite != null) {
            context.drawSprite(xGrid * SIZE, yGrid * SIZE, sprite, true);
        }
    }

    public int getId() {
        return id;
    }

    public Sprite getSprite() {
        return sprite;
    }

    public boolean isSolid() {
        return solid;
    }
}
