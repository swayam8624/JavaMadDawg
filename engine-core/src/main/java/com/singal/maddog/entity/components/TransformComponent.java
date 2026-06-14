package com.singal.maddog.entity.components;

import com.singal.maddog.entity.Component;
import com.singal.maddog.math.Vector2f;

/**
 * Defines the position and orientation of an entity in 2D space.
 */
public class TransformComponent extends Component {
    private final Vector2f position = new Vector2f();
    private float rotation = 0.0f; // In radians
    private float scaleX = 1.0f;
    private float scaleY = 1.0f;

    public TransformComponent() {}

    public TransformComponent(float x, float y) {
        this.position.set(x, y);
    }

    public Vector2f getPosition() {
        return position;
    }

    public void setPosition(float x, float y) {
        this.position.set(x, y);
    }

    public float getX() {
        return position.x;
    }

    public void setX(float x) {
        this.position.x = x;
    }

    public float getY() {
        return position.y;
    }

    public void setY(float y) {
        this.position.y = y;
    }

    public float getRotation() {
        return rotation;
    }

    public void setRotation(float rotation) {
        this.rotation = rotation;
    }

    public float getScaleX() {
        return scaleX;
    }

    public void setScaleX(float scaleX) {
        this.scaleX = scaleX;
    }

    public float getScaleY() {
        return scaleY;
    }

    public void setScaleY(float scaleY) {
        this.scaleY = scaleY;
    }
}
