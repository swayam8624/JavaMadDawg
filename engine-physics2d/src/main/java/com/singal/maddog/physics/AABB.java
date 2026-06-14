package com.singal.maddog.physics;

import com.singal.maddog.math.Vector2f;

/**
 * Axis-Aligned Bounding Box (AABB) for 2D collision detection.
 */
public class AABB {
    public float x;
    public float y;
    public float width;
    public float height;

    public AABB(float x, float y, float width, float height) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
    }

    public boolean intersects(AABB other) {
        return this.x < other.x + other.width &&
               this.x + this.width > other.x &&
               this.y < other.y + other.height &&
               this.y + this.height > other.y;
    }

    public boolean contains(float px, float py) {
        return px >= this.x && px <= this.x + this.width &&
               py >= this.y && py <= this.y + this.height;
    }

    public void set(float x, float y, float width, float height) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
    }

    @Override
    public String toString() {
        return "AABB(" + x + ", " + y + ", " + width + ", " + height + ")";
    }
}
