package com.singal.maddog.core;

import java.awt.Graphics2D;

/**
 * A Layer represents a distinct layer of rendering or logic,
 * such as a background tilemap, entity layer, or UI overlay.
 * Layers can receive events and updates, and be rendered sequentially.
 */
public interface Layer {
    
    default void onAttach() {}
    
    default void onDetach() {}
    
    default void update(double deltaTime) {}
    
    default void render(Graphics2D g2d) {}
    
    default boolean onEvent() {
        return false; // Returns true if the event was consumed
    }
}
