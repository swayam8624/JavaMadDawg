package com.singal.maddog.core;

/**
 * A highly precise, configurable fixed-timestep game loop.
 * Runs on a dedicated thread, decoupled from AWT Event Thread.
 */
public class GameLoop implements Runnable {
    
    public interface GameLoopCallback {
        void onUpdate(double deltaTime);
        void onRender(double interpolation);
    }

    private final GameLoopCallback callback;
    private final double targetUps;
    private final double tickNanos;

    private Thread thread;
    private volatile boolean running = false;
    private volatile boolean paused = false;

    // Diagnostics
    private int fps = 0;
    private int ups = 0;
    private int currentFps = 0;
    private int currentUps = 0;
    private long lastStatsTime = 0;

    public GameLoop(double targetUps, GameLoopCallback callback) {
        this.targetUps = targetUps;
        this.tickNanos = 1_000_000_000.0 / targetUps;
        this.callback = callback;
    }

    public synchronized void start() {
        if (running) return;
        running = true;
        thread = new Thread(this, "GameLoop-Thread");
        thread.start();
    }

    public synchronized void stop() {
        if (!running) return;
        running = false;
        try {
            thread.join(2000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    public void setPaused(boolean paused) {
        this.paused = paused;
    }

    public boolean isPaused() {
        return paused;
    }

    public int getFps() {
        return fps;
    }

    public int getUps() {
        return ups;
    }

    @Override
    public void run() {
        long lastTime = System.nanoTime();
        double accumulator = 0.0;
        lastStatsTime = System.currentTimeMillis();

        while (running) {
            long now = System.nanoTime();
            double elapsed = now - lastTime;
            lastTime = now;

            // Cap elapsed time to prevent "spiral of death" under heavy stutter
            if (elapsed > 250_000_000.0) {
                elapsed = 250_000_000.0;
            }

            if (!paused) {
                accumulator += elapsed;
            }

            // Fixed update tick
            while (accumulator >= tickNanos) {
                callback.onUpdate(1.0 / targetUps); // Pass fixed delta time in seconds
                currentUps++;
                accumulator -= tickNanos;
            }

            // Variable render tick with interpolation factor
            double interpolation = accumulator / tickNanos;
            callback.onRender(interpolation);
            currentFps++;

            // Stats calculation every second
            long currentTime = System.currentTimeMillis();
            if (currentTime - lastStatsTime >= 1000) {
                fps = currentFps;
                ups = currentUps;
                currentFps = 0;
                currentUps = 0;
                lastStatsTime = currentTime;
            }

            // Sleep/Yield to prevent 100% CPU thread burn when ahead
            long timeUsed = System.nanoTime() - now;
            double timeRemaining = tickNanos - timeUsed;
            if (timeRemaining > 2_000_000.0) { // If we have more than 2ms remaining
                try {
                    // Sleep for 1ms to yield CPU to system
                    Thread.sleep(1);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            } else {
                Thread.yield();
            }
        }
    }
}
