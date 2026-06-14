package com.singal.maddog.entity.components;

import com.singal.maddog.entity.Component;
import com.singal.maddog.physics.AABB;

/**
 * Attaches an AABB physical boundary to an Entity for collision query checks.
 */
public class ColliderComponent extends Component {
    public enum CollisionLayer {
        NONE, PLAYER, ENEMY, PROJECTILE, SOLID_TILE
    }

    private final AABB aabb;
    private final float offsetX;
    private final float offsetY;
    private final float width;
    private final float height;
    
    private CollisionLayer layer = CollisionLayer.NONE;
    private boolean isTrigger = false;

    public ColliderComponent(float offsetX, float offsetY, float width, float height) {
        this.offsetX = offsetX;
        this.offsetY = offsetY;
        this.width = width;
        this.height = height;
        this.aabb = new AABB(0, 0, width, height);
    }

    @Override
    public void init() {
        updateAABB();
    }

    @Override
    public void update(double deltaTime) {
        updateAABB();
    }

    private void updateAABB() {
        TransformComponent transform = owner.getComponent(TransformComponent.class);
        if (transform != null) {
            aabb.set(transform.getX() + offsetX, transform.getY() + offsetY, width, height);
        }
    }

    public AABB getAABB() {
        return aabb;
    }

    public CollisionLayer getLayer() {
        return layer;
    }

    public void setLayer(CollisionLayer layer) {
        this.layer = layer;
    }

    public boolean isTrigger() {
        return isTrigger;
    }

    public void setTrigger(boolean trigger) {
        isTrigger = trigger;
    }

    public float getOffsetX() {
        return offsetX;
    }

    public float getOffsetY() {
        return offsetY;
    }

    public float getWidth() {
        return width;
    }

    public float getHeight() {
        return height;
    }
}
