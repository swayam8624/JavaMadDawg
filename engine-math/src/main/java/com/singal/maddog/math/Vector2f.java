package com.singal.maddog.math;

import java.util.Objects;

/**
 * A mutable 2D float vector for physics and float coordinate calculations.
 */
public class Vector2f {
    public float x;
    public float y;

    public Vector2f() {
        this.x = 0.0f;
        this.y = 0.0f;
    }

    public Vector2f(float x, float y) {
        this.x = x;
        this.y = y;
    }

    public Vector2f(Vector2f vector) {
        this.x = vector.x;
        this.y = vector.y;
    }

    public Vector2f set(float x, float y) {
        this.x = x;
        this.y = y;
        return this;
    }

    public Vector2f set(Vector2f vector) {
        this.x = vector.x;
        this.y = vector.y;
        return this;
    }

    public Vector2f add(Vector2f vector) {
        this.x += vector.x;
        this.y += vector.y;
        return this;
    }

    public Vector2f add(float x, float y) {
        this.x += x;
        this.y += y;
        return this;
    }

    public Vector2f subtract(Vector2f vector) {
        this.x -= vector.x;
        this.y -= vector.y;
        return this;
    }

    public Vector2f subtract(float x, float y) {
        this.x -= x;
        this.y -= y;
        return this;
    }

    public Vector2f multiply(float scalar) {
        this.x *= scalar;
        this.y *= scalar;
        return this;
    }

    public float length() {
        return (float) Math.sqrt(x * x + y * y);
    }

    public Vector2f normalize() {
        float len = length();
        if (len != 0) {
            x /= len;
            y /= len;
        }
        return this;
    }

    public float distance(Vector2f other) {
        float dx = this.x - other.x;
        float dy = this.y - other.y;
        return (float) Math.sqrt(dx * dx + dy * dy);
    }

    public float getX() {
        return x;
    }

    public void setX(float x) {
        this.x = x;
    }

    public float getY() {
        return y;
    }

    public void setY(float y) {
        this.y = y;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Vector2f vector2f = (Vector2f) o;
        return Float.compare(vector2f.x, x) == 0 && Float.compare(vector2f.y, y) == 0;
    }

    @Override
    public int hashCode() {
        return Objects.hash(x, y);
    }

    @Override
    public String toString() {
        return "Vector2f(" + x + ", " + y + ")";
    }
}
