package com.singal.maddog.entity;

import com.singal.maddog.renderer.RenderContext;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * A generic game object container holding components in an ECS-lite model.
 */
public class Entity {
    private static int nextEntityId = 0;

    private final int id;
    private final String name;
    private final String tag;
    private final Map<Class<? extends Component>, Component> components = new HashMap<>();
    private final List<Component> componentList = new ArrayList<>(); // Fast iteration
    
    private boolean active = true;
    private boolean destroyed = false;

    public Entity(String name, String tag) {
        this.id = nextEntityId++;
        this.name = name;
        this.tag = tag;
    }

    public void init() {
        for (int i = 0; i < componentList.size(); i++) {
            componentList.get(i).init();
        }
    }

    public void update(double deltaTime) {
        if (!active || destroyed) return;
        for (int i = 0; i < componentList.size(); i++) {
            componentList.get(i).update(deltaTime);
        }
    }

    public void render(RenderContext context) {
        if (!active || destroyed) return;
        for (int i = 0; i < componentList.size(); i++) {
            componentList.get(i).render(context);
        }
    }

    public void destroy() {
        if (destroyed) return;
        destroyed = true;
        for (int i = 0; i < componentList.size(); i++) {
            componentList.get(i).onDestroy();
        }
    }

    public <T extends Component> T addComponent(T component) {
        if (component == null) return null;
        component.setOwner(this);
        components.put(component.getClass(), component);
        componentList.add(component);
        return component;
    }

    @SuppressWarnings("unchecked")
    public <T extends Component> T getComponent(Class<T> type) {
        Component component = components.get(type);
        if (component != null) {
            return (T) component;
        }
        // Fallback: check subclass types
        for (Component c : componentList) {
            if (type.isInstance(c)) {
                return (T) c;
            }
        }
        return null;
    }

    public <T extends Component> boolean hasComponent(Class<T> type) {
        return getComponent(type) != null;
    }

    public <T extends Component> void removeComponent(Class<T> type) {
        Component component = components.remove(type);
        if (component != null) {
            componentList.remove(component);
            component.onDestroy();
            component.setOwner(null);
        }
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getTag() {
        return tag;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public boolean isDestroyed() {
        return destroyed;
    }
}
