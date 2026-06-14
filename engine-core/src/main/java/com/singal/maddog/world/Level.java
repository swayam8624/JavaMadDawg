package com.singal.maddog.world;

import com.singal.maddog.core.Layer;
import com.singal.maddog.entity.Entity;
import com.singal.maddog.renderer.RenderContext;
import java.awt.Graphics2D;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Manages the world map tiles and dynamic game entities.
 * Implements {@link Layer} to integrate seamlessly into a Scene.
 */
public class Level implements Layer {
    private TileMap tileMap;
    
    private final List<Entity> entities = new ArrayList<>();
    private final List<Entity> entitiesReadOnly = Collections.unmodifiableList(entities);
    
    private final List<Entity> entitiesToAdd = new ArrayList<>();
    private final List<Entity> entitiesToRemove = new ArrayList<>();

    public Level(TileMap tileMap) {
        this.tileMap = tileMap;
    }

    public void addEntity(Entity entity) {
        entitiesToAdd.add(entity);
    }

    public void removeEntity(Entity entity) {
        entitiesToRemove.add(entity);
    }

    public List<Entity> getEntities() {
        return entitiesReadOnly;
    }

    public TileMap getTileMap() {
        return tileMap;
    }

    public void setTileMap(TileMap tileMap) {
        this.tileMap = tileMap;
    }

    @Override
    public void update(double deltaTime) {
        // Handle additions
        if (!entitiesToAdd.isEmpty()) {
            for (Entity e : entitiesToAdd) {
                entities.add(e);
                e.init();
            }
            entitiesToAdd.clear();
        }

        // Update active entities
        for (int i = 0; i < entities.size(); i++) {
            Entity entity = entities.get(i);
            if (!entity.isDestroyed()) {
                entity.update(deltaTime);
            } else {
                entitiesToRemove.add(entity);
            }
        }

        // Handle removals
        if (!entitiesToRemove.isEmpty()) {
            for (Entity e : entitiesToRemove) {
                entities.remove(e);
            }
            entitiesToRemove.clear();
        }
    }

    @Override
    public void render(Graphics2D g2d) {
        // We render using SoftwareRenderContext via the Application, 
        // but if custom rendering hooks are needed, AWT graphics are passed here.
    }

    public void render(RenderContext context) {
        // Render background tiles
        if (tileMap != null) {
            tileMap.render(context);
        }

        // Render entities
        for (int i = 0; i < entities.size(); i++) {
            entities.get(i).render(context);
        }
    }
}
