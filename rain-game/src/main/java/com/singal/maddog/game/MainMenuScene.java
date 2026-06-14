package com.singal.maddog.game;
 
import com.singal.maddog.core.Application;
import com.singal.maddog.core.Scene;
import com.singal.maddog.input.InputManager;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.event.KeyEvent;

/**
 * Renders the initial title screen and handles transitions to gameplay.
 */
public class MainMenuScene extends Scene {
    private final Application app;
    private final InputManager input;
    
    private int selectedOption = 0;
    private boolean showControls = false;
    private double animTime = 0.0;

    public MainMenuScene(Application app, InputManager input) {
        this.app = app;
        this.input = input;
    }

    @Override
    protected void onInit() {
        // Setup menu controls
        input.bindAction("menu_up", KeyEvent.VK_UP, KeyEvent.VK_W);
        input.bindAction("menu_down", KeyEvent.VK_DOWN, KeyEvent.VK_S);
        input.bindAction("menu_select", KeyEvent.VK_ENTER);
        input.bindAction("menu_quit", KeyEvent.VK_ESCAPE);
    }

    @Override
    public void update(double deltaTime) {
        input.update();
        animTime += deltaTime;

        if (showControls) {
            if (input.isActionPressed("menu_select") || input.isActionPressed("menu_quit")) {
                showControls = false;
            }
        } else {
            if (input.isActionPressed("menu_up")) {
                selectedOption = (selectedOption - 1 + 3) % 3;
            } else if (input.isActionPressed("menu_down")) {
                selectedOption = (selectedOption + 1) % 3;
            } else if (input.isActionPressed("menu_select")) {
                if (selectedOption == 0) {
                    app.setScene(new GameplayScene(app, input));
                } else if (selectedOption == 1) {
                    showControls = true;
                } else if (selectedOption == 2) {
                    app.stop();
                }
            } else if (input.isActionPressed("menu_quit")) {
                app.stop();
            }
        }
    }

    @Override
    public void render(Graphics2D g2d) {
        int w = app.getBufferWidth();
        int h = app.getBufferHeight();

        // 1. Clear background to dark space color
        g2d.setColor(new Color(0x05, 0x05, 0x0f));
        g2d.fillRect(0, 0, w, h);

        // 2. Draw retro horizontal scanlines
        g2d.setColor(new Color(255, 255, 255, 8));
        for (int y = 0; y < h; y += 4) {
            g2d.fillRect(0, y, w, 1);
        }

        if (showControls) {
            // RENDER CONTROLS SCREEN
            g2d.setColor(new Color(0x00, 0xff, 0xff)); // Cyan Title
            g2d.setFont(new Font("Monospaced", Font.BOLD, 16));
            g2d.drawString("HOW TO PLAY", 100, 40);

            g2d.setColor(Color.WHITE);
            g2d.setFont(new Font("Monospaced", Font.PLAIN, 10));
            g2d.drawString("Move    : WASD / Arrow Keys", 60, 70);
            g2d.drawString("Shoot   : Mouse Left Click", 60, 90);
            g2d.drawString("Debug   : F3 overlay", 60, 110);

            // Flashing prompt to return
            int alpha = (int) (127 + 127 * Math.sin(animTime * 6.0));
            g2d.setColor(new Color(255, 255, 255, alpha));
            g2d.drawString("[ Press ENTER to return ]", 75, 140);
        } else {
            // RENDER MAIN MENU SCREEN
            
            // Pulsing title glow (red/orange neon)
            float glowPulse = (float) (Math.sin(animTime * 3.0) * 0.5f + 0.5f);
            Color shadowColor = new Color((int)(120 + 80 * glowPulse), 0, 120);
            Color titleColor = new Color(255, (int)(100 + 100 * glowPulse), 0);

            g2d.setFont(new Font("Monospaced", Font.BOLD, 28));
            
            // Draw title shadow
            g2d.setColor(shadowColor);
            g2d.drawString("MADDAWG", 88, 52);
            
            // Draw title main text
            g2d.setColor(titleColor);
            g2d.drawString("MADDAWG", 86, 50);

            // Menu choices
            String[] options = { "PLAY GAME", "CONTROLS", "QUIT" };
            g2d.setFont(new Font("Monospaced", Font.BOLD, 11));

            for (int i = 0; i < options.length; i++) {
                int yPos = 85 + i * 20;
                
                if (selectedOption == i) {
                    // Flash cursor and highlight selection
                    int selectAlpha = (int) (180 + 75 * Math.sin(animTime * 10.0));
                    g2d.setColor(new Color(0, 255, 255, selectAlpha)); // Cyan selection
                    
                    // Draw selector arrow
                    g2d.drawString("►", 85, yPos);
                    g2d.drawString(options[i], 105, yPos);
                } else {
                    g2d.setColor(new Color(150, 150, 180)); // Dim gray-blue
                    g2d.drawString(options[i], 105, yPos);
                }
            }

            // Footer instructions
            g2d.setFont(new Font("Monospaced", Font.PLAIN, 8));
            g2d.setColor(new Color(100, 100, 120));
            g2d.drawString("W/S or UP/DOWN: Move | ENTER: Select", 55, 155);
        }
    }
}
