package com.singal.maddog.renderer;

import com.singal.maddog.math.MathUtils;
import com.singal.maddog.math.Vector2f;

/**
 * Tracks the viewport coordinate offsets for 2D scrolling.
 * Supports smooth linear interpolation (lerp) chasing.
 */
public class Camera2D {
    private final Vector2f position = new Vector2f();
    private float targetX;
    private float targetY;
    private float lerpSpeed = 1.0f; // 1.0 means instant lock

    public Camera2D() {}

    public Camera2D(float x, float y) {
        this.position.set(x, y);
        this.targetX = x;
        this.targetY = y;
    }

    public void update(double deltaTime) {
        if (lerpSpeed >= 1.0f) {
            position.x = targetX;
            position.y = targetY;
        } else {
            // Smoothly move camera towards target
            float alpha = (float) (1.0 - Math.exp(-lerpSpeed * deltaTime * 60.0));
            position.x = MathUtils.lerp(position.x, targetX, alpha);
            position.y = MathUtils.lerp(position.y, targetY, alpha);
        }
    }

    public void setPosition(float x, float y) {
        this.position.set(x, y);
        this.targetX = x;
        this.targetY = y;
    }

    public void setTarget(float tx, float ty) {
        this.targetX = tx;
        this.targetY = ty;
    }

    public float getX() {
        return position.x;
    }

    public float getY() {
        return position.y;
    }

    public float getLerpSpeed() {
        return lerpSpeed;
    }

    public void setLerpSpeed(float lerpSpeed) {
        this.lerpSpeed = MathUtils.clamp(lerpSpeed, 0.01f, 1.0f);
    }
}
