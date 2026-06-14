package com.singal.maddog.entity.components;

import com.singal.maddog.entity.Component;
import com.singal.maddog.math.Vector2f;
import com.singal.maddog.physics.AABB;
import com.singal.maddog.world.Tile;
import com.singal.maddog.world.TileMap;

/**
 * Handles entity movement and sliding collision resolution against world tiles.
 */
public class MovementComponent extends Component {
    private final Vector2f velocity = new Vector2f();
    private float speed = 1.0f;

    public void move(float xa, float ya, TileMap tileMap) {
        if (xa == 0 && ya == 0) return;

        // Decouple diagonal movements to allow sliding along walls
        if (xa != 0 && ya != 0) {
            move(xa, 0, tileMap);
            move(0, ya, tileMap);
            return;
        }

        TransformComponent transform = owner.getComponent(TransformComponent.class);
        ColliderComponent collider = owner.getComponent(ColliderComponent.class);

        if (transform == null) return;

        if (collider == null) {
            // No collider, move freely
            transform.setX(transform.getX() + xa);
            transform.setY(transform.getY() + ya);
            return;
        }

        // Check collision for future position
        float nextX = transform.getX() + xa;
        float nextY = transform.getY() + ya;

        // Construct a temporary AABB representing the future collider position
        AABB futureAABB = new AABB(
            nextX + collider.getOffsetX(),
            nextY + collider.getOffsetY(),
            collider.getWidth(),
            collider.getHeight()
        );

        if (!hasTileCollision(futureAABB, tileMap)) {
            transform.setX(nextX);
            transform.setY(nextY);
        }
    }

    private boolean hasTileCollision(AABB box, TileMap tileMap) {
        if (tileMap == null) return false;

        // Convert box boundaries to tile coordinates
        int minX = (int) (box.x / Tile.SIZE);
        int maxX = (int) ((box.x + box.width) / Tile.SIZE);
        int minY = (int) (box.y / Tile.SIZE);
        int maxY = (int) ((box.y + box.height) / Tile.SIZE);

        for (int y = minY; y <= maxY; y++) {
            for (int x = minX; x <= maxX; x++) {
                if (tileMap.getTile(x, y).isSolid()) {
                    return true;
                }
            }
        }
        return false;
    }

    public Vector2f getVelocity() {
        return velocity;
    }

    public float getSpeed() {
        return speed;
    }

    public void setSpeed(float speed) {
        this.speed = speed;
    }
}
