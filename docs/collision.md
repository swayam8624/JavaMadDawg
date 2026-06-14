# Collision & Physics 2D

The engine features a simple axis-aligned bounding box (AABB) sliding physics check.

---

## 1. Sliding Collision Resolution
When an entity moves along a vector $(xa, ya)$, we resolve diagonal movements independently:
```java
if (xa != 0 && ya != 0) {
    move(xa, 0, tileMap);
    move(0, ya, tileMap);
    return;
}
```
If moving along $x$ causes a collision, we discard $xa$ but continue with $ya$. This allows players and mobs to smoothly slide along borders and walls instead of stopping dead.

---

## 2. AABB Intersections
Overlap queries are calculated by checking boundaries overlap on the 2D plane:
```java
public boolean intersects(AABB other) {
    return this.x < other.x + other.width &&
           this.x + this.width > other.x &&
           this.y < other.y + other.height &&
           this.y + this.height > other.y;
}
```
This is computationally lightweight and sufficient for grid-based tile layouts.
