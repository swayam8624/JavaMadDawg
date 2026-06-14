package com.singal.maddog.game;

import com.singal.maddog.core.Application;
import com.singal.maddog.core.Scene;
import com.singal.maddog.input.InputManager;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;

/**
 * Renders the initial title screen and handles transitions to gameplay.
 */
public class MainMenuScene extends Scene {
    private final Application app;
    private final InputManager input;

    public MainMenuScene(Application app, InputManager input) {
        this.app = app;
        this.input = input;
    }

    @Override
    protected void onInit() {
        // Setup initial action bindings
        input.bindAction("start_game", java.awt.event.KeyEvent.VK_ENTER);
        input.bindAction("quit_game", java.awt.event.KeyEvent.VK_ESCAPE);
        input.bindAction("toggle_debug", java.awt.event.KeyEvent.VK_F3);
    }

    @Override
    public void update(double deltaTime) {
        input.update();
        if (input.isActionPressed("start_game")) {
            app.setScene(new GameplayScene(app, input));
        } else if (input.isActionPressed("quit_game")) {
            app.stop();
        }
    }

    @Override
    public void render(Graphics2D g2d) {
        // Clear background
        g2d.setColor(new Color(0x1a, 0x1a, 0x2a));
        g2d.fillRect(0, 0, app.getBufferWidth() * app.getScale(), app.getBufferHeight() * app.getScale());

        // Title text
        g2d.setColor(Color.WHITE);
        g2d.setFont(new Font("Monospaced", Font.BOLD, 36));
        g2d.drawString("RAIN REMASTERED", 150, 150);

        // Subtext menu choices
        g2d.setFont(new Font("Monospaced", Font.PLAIN, 18));
        g2d.drawString("Press [ENTER] to Start Game", 180, 240);
        g2d.drawString("Press [F3] to Toggle debug metrics in game", 180, 280);
        g2d.drawString("Press [ESC] to Exit", 180, 320);

        // Footer copyright info
        g2d.setFont(new Font("Monospaced", Font.ITALIC, 12));
        g2d.drawString("Java Remaster Portfolio Engine", 280, 450);
    }
}
