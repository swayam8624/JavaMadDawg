package com.singal.maddog.entity;

import com.singal.maddog.renderer.RenderContext;

/**
 * Base class for all components in the ECS-lite system.
 * Components contain data and optionally update/render logic.
 */
public abstract class Component {
    protected Entity owner;

    public void init() {}
    
    public void update(double deltaTime) {}
    
    public void render(RenderContext context) {}
    
    public void onDestroy() {}

    public Entity getOwner() {
        return owner;
    }

    void setOwner(Entity owner) {
        this.owner = owner;
    }
}
