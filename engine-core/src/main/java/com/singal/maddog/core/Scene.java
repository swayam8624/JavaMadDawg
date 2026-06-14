package com.singal.maddog.core;

import java.awt.Graphics2D;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * A Scene contains a collection of layers.
 * It manages transitions, updates layers, and delegates rendering.
 */
public abstract class Scene {
    private final List<Layer> layers = new ArrayList<>();
    private final List<Layer> layersReadOnly = Collections.unmodifiableList(layers);
    private boolean initialized = false;

    public final void attach() {
        if (!initialized) {
            onInit();
            initialized = true;
        }
        onAttach();
        for (Layer layer : layers) {
            layer.onAttach();
        }
    }

    public final void detach() {
        for (Layer layer : layers) {
            layer.onDetach();
        }
        onDetach();
    }

    protected abstract void onInit();
    
    protected void onAttach() {}
    
    protected void onDetach() {}

    public void addLayer(Layer layer) {
        layers.add(layer);
        if (initialized) {
            layer.onAttach();
        }
    }

    public void removeLayer(Layer layer) {
        if (layers.remove(layer)) {
            layer.onDetach();
        }
    }

    public void update(double deltaTime) {
        for (int i = 0; i < layers.size(); i++) {
            layers.get(i).update(deltaTime);
        }
    }

    public void render(Graphics2D g2d) {
        for (int i = 0; i < layers.size(); i++) {
            layers.get(i).render(g2d);
        }
    }

    public List<Layer> getLayers() {
        return layersReadOnly;
    }
}
