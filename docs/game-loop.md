# Game Loop Architecture

The **Game Loop** is the heartbeat of the engine. It manages synchronization between fixed updates and variable rendering.

---

## 1. Fixed Timestep Loop (Accumulator)

In real-time games, physics and updates must run at a consistent rate to ensure deterministic behavior. Variable frames rates should not speed up or slow down player speeds. We implement this using an accumulator:

$$\text{accumulator} \ge \text{targetNanos}$$

```java
// GameLoop.java run() loop
while (running) {
    long now = System.nanoTime();
    double elapsed = now - lastTime;
    lastTime = now;

    // Cap elapsed time to prevent locking up under heavy lag
    if (elapsed > 250_000_000.0) elapsed = 250_000_000.0;

    if (!paused) {
        accumulator += elapsed;
    }

    while (accumulator >= tickNanos) {
        callback.onUpdate(1.0 / targetUps);
        accumulator -= tickNanos;
    }

    double interpolation = accumulator / tickNanos;
    callback.onRender(interpolation);
}
```

---

## 2. Yielding CPU Time
To prevent the engine from eating 100% of a CPU core's capacity spinning, we check how much time is remaining in the tick, and if it exceeds 2 milliseconds, we sleep the thread for 1 millisecond. Otherwise, we yield execution.
