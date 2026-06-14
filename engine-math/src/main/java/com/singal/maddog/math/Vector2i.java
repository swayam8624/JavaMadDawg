package com.singal.maddog.math;

import java.util.Objects;

/**
 * A mutable 2D integer vector for coordinate calculations.
 */
public class Vector2i {
    public int x;
    public int y;

    public Vector2i() {
        this.x = 0;
        this.y = 0;
    }

    public Vector2i(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public Vector2i(Vector2i vector) {
        this.x = vector.x;
        this.y = vector.y;
    }

    public Vector2i set(int x, int y) {
        this.x = x;
        this.y = y;
        return this;
    }

    public Vector2i set(Vector2i vector) {
        this.x = vector.x;
        this.y = vector.y;
        return this;
    }

    public Vector2i add(Vector2i vector) {
        this.x += vector.x;
        this.y += vector.y;
        return this;
    }

    public Vector2i add(int x, int y) {
        this.x += x;
        this.y += y;
        return this;
    }

    public Vector2i subtract(Vector2i vector) {
        this.x -= vector.x;
        this.y -= vector.y;
        return this;
    }

    public Vector2i subtract(int x, int y) {
        this.x -= x;
        this.y -= y;
        return this;
    }

    public Vector2i multiply(int scalar) {
        this.x *= scalar;
        this.y *= scalar;
        return this;
    }

    public double distance(Vector2i other) {
        double dx = this.x - other.x;
        double dy = this.y - other.y;
        return Math.sqrt(dx * dx + dy * dy);
    }

    public int getX() {
        return x;
    }

    public void setX(int x) {
        this.x = x;
    }

    public int getY() {
        return y;
    }

    public void setY(int y) {
        this.y = y;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Vector2i vector2i = (Vector2i) o;
        return x == vector2i.x && y == vector2i.y;
    }

    @Override
    public int hashCode() {
        return Objects.hash(x, y);
    }

    @Override
    public String toString() {
        return "Vector2i(" + x + ", " + y + ")";
    }
}
