package com.singal.maddog.ai;

/**
 * An abstraction representing a grid layout that can be queried for pathfinding.
 * Decouples the AI module from concrete tile systems.
 */
public interface NavigationGrid {
    boolean isSolid(int x, int y);
}
