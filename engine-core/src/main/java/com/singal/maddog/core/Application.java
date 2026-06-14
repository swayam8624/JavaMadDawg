package com.singal.maddog.core;

import javax.swing.JFrame;
import java.awt.Canvas;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferStrategy;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;

/**
 * Base game engine container class that manages the window, canvas, 
 * render buffer, active scene, and main loop.
 */
public class Application extends Canvas implements GameLoop.GameLoopCallback {
    private static final long serialVersionUID = 1L;

    private final String title;
    private final int width;
    private final int height;
    private final int scale;

    private JFrame frame;
    private GameLoop gameLoop;
    private Scene currentScene;

    // Software buffer setup
    private final BufferedImage backBuffer;
    private final int[] pixels;

    public Application(String title, int width, int height, int scale) {
        this.title = title;
        this.width = width;
        this.height = height;
        this.scale = scale;

        Dimension size = new Dimension(width * scale, height * scale);
        setPreferredSize(size);
        setMinimumSize(size);
        setMaximumSize(size);

        // Set up flat software pixel buffer
        backBuffer = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        pixels = ((DataBufferInt) backBuffer.getRaster().getDataBuffer()).getData();

        initWindow();
        
        gameLoop = new GameLoop(60.0, this);
    }

    private void initWindow() {
        frame = new JFrame();
        frame.setTitle(title);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setResizable(true);
        frame.add(this);
        frame.pack();
        frame.setLocationRelativeTo(null);
    }

    public void start() {
        frame.setVisible(true);
        requestFocus();
        gameLoop.start();
    }

    public void stop() {
        gameLoop.stop();
        frame.dispose();
    }

    public synchronized void setScene(Scene scene) {
        if (currentScene != null) {
            currentScene.detach();
        }
        currentScene = scene;
        if (currentScene != null) {
            currentScene.attach();
        }
    }

    public Scene getCurrentScene() {
        return currentScene;
    }

    public int getBufferWidth() {
        return width;
    }

    public int getBufferHeight() {
        return height;
    }

    public int getScale() {
        return scale;
    }

    public float getRenderScaleX() {
        return (float) getWidth() / width;
    }

    public float getRenderScaleY() {
        return (float) getHeight() / height;
    }

    public int[] getPixelBuffer() {
        return pixels;
    }

    public int getFps() {
        return gameLoop.getFps();
    }

    public int getUps() {
        return gameLoop.getUps();
    }

    @Override
    public void onUpdate(double deltaTime) {
        if (currentScene != null) {
            currentScene.update(deltaTime);
        }
    }

    @Override
    public void onRender(double interpolation) {
        BufferStrategy bs = getBufferStrategy();
        if (bs == null) {
            createBufferStrategy(3); // Triple buffering
            return;
        }

        // Get target rendering graphics
        Graphics2D g = (Graphics2D) bs.getDrawGraphics();
        
        // Render current scene directly onto the screen backBuffer
        if (currentScene != null) {
            Graphics2D bufferGraphics = backBuffer.createGraphics();
            bufferGraphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
            currentScene.render(bufferGraphics);
            bufferGraphics.dispose();
        }

        // Scale and blit the software buffer to canvas
        g.drawImage(backBuffer, 0, 0, getWidth(), getHeight(), null);

        g.dispose();
        bs.show();
    }
}
