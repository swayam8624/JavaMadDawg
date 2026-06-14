# Codebase Comparison: Legacy vs. Remastered

This document analyzes the legacy codebase from The Cherno's Game Programming tutorial series and compares it with the modernized implementation in **Rain Remastered**.

---

## Comparison Matrix

| Area | Original Tutorial Project | Remastered Project |
| :--- | :--- | :--- |
| **Build System** | Eclipse/IntelliJ manual build paths; IDE files committed to git. | Clean, multi-module Gradle project with strict module boundaries. |
| **Java Standards** | Java 7/8. Outdated loops, legacy syntax, manual thread spawning. | Java 22. Uses Records, modern patterns, thread-safe structures. |
| **Game Loop** | High CPU utilization (spins infinitely); no delta time passed to render. | Precise fixed-timestep loop with accumulator, yielding CPU time. |
| **Rendering** | Direct pixel copying inside `Screen` coupled with Swing buffers. | Decoupled `RenderContext` and `SoftwareRenderContext` abstractions. |
| **Entities** | Deep inheritance tree (`Chaser -> Mob -> Entity`). | Composition-based ECS-Lite (`Entity` composed of components). |
| **Input** | `boolean[] keys = new boolean[120]`, crashes when out-of-bounds keys hit. | Action-map based `InputManager` protecting array bounds safely. |
| **Asset Loading** | Static fields loaded immediately; no caching, duplicate loads allowed. | Caching `AssetManager` with error-handling and fallback buffers. |
| **Pathfinding** | Runs A* on main loop, blocking game execution during searches. | Decoupled A* on background threads, preventing main thread lag. |
| **Serialization** | Fragile custom binary serializations with strict offsets. | Flexible JSON serialization using Gson for settings and state. |
| **Testability** | Lacks unit tests; untestable due to high coupling and static state. | Full JUnit 5 test suite verifying math, collision, and AI. |

---

## Detailed Comparison Examples

### Input Handling

#### Original (Fragile)
```java
// Keyboard.java
private boolean[] keys = new boolean[120];

public void keyPressed(KeyEvent e) {
    keys[e.getKeyCode()] = true; // Throws IndexOutOfBounds for keycodes >= 120!
}
```

#### Remastered (Safe & Decoupled)
```java
// InputManager.java
private final boolean[] rawKeys = new boolean[65536]; // Safe bounds

public synchronized void keyPressed(KeyEvent e) {
    int code = e.getKeyCode();
    if (code >= 0 && code < rawKeys.length) {
        rawKeys[code] = true;
    }
}
```

### Entity Rendering Coupling

#### Original (Violation of Open-Closed Principle)
```java
// Screen.java
public void renderMob(int xp, int yp, Mob mob) {
    // ...
    int col = mob.getSprite().pixels[xs + ys * 32];
    if ((mob instanceof Chaser) && col == 0xff472BBF) col = 0xffBA0015;
    if ((mob instanceof Star) && col == 0xff472BBF) col = 0xffE8E83A;
    // ...
}
```

#### Remastered (Clean Rendering Decoupling)
```java
// SpriteComponent.java
@Override
public void render(RenderContext context) {
    TransformComponent transform = owner.getComponent(TransformComponent.class);
    Sprite activeSprite = getActiveSprite();
    if (activeSprite != null && transform != null) {
        context.drawSprite((int)transform.getX(), (int)transform.getY(), activeSprite, flipX, flipY, true);
    }
}
```
No type checks or instanceof statements inside the renderer. The sprite class itself holds the correct color properties, keeping rendering entirely generic.
