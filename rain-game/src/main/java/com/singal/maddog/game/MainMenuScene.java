package com.singal.maddog.game;
 
import com.singal.maddog.core.Application;
import com.singal.maddog.core.Scene;
import com.singal.maddog.input.InputManager;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.event.KeyEvent;

/**
 * 🎓 MainMenuScene renders the initial title screen and handles keyboard menu navigation.
 * 
 * 💡 TECH NOTE: Decoupled Scenes
 * MainMenuScene extends the {@link Scene} class. By overriding lifecycle hooks, 
 * it manages its own state and controls when to transition to {@link GameplayScene}.
 */
public class MainMenuScene extends Scene {
    private final Application app;
    private final InputManager input;
    
    /** Index of the currently highlighted menu option (0 = Play, 1 = Controls, 2 = Quit). */
    private int selectedOption = 0;
    
    /** Flag to toggle rendering between the main menu choices and the controls overlay. */
    private boolean showControls = false;
    
    /** Accumulator for driving sinusoidal micro-animations (e.g. text pulsing). */
    private double animTime = 0.0;

    /**
     * Constructs the main menu scene.
     * 
     * @param app   The application container providing resolution properties.
     * @param input The input manager used to poll key actions.
     */
    public MainMenuScene(Application app, InputManager input) {
        this.app = app;
        this.input = input;
    }

    /**
     * Initializes key action bindings.
     * 
     * 💡 DESIGN PATTERN: Action Map Input Mapping
     * Rather than hardcoding physical key codes (e.g. KeyEvent.VK_W) in update loops,
     * we bind abstract action strings ("menu_up") to multiple keys. This allows 
     * easily changing mapping configurations without modifying the gameplay update code.
     */
    @Override
    protected void onInit() {
        input.bindAction("menu_up", KeyEvent.VK_UP, KeyEvent.VK_W);
        input.bindAction("menu_down", KeyEvent.VK_DOWN, KeyEvent.VK_S);
        input.bindAction("menu_select", KeyEvent.VK_ENTER);
        input.bindAction("menu_quit", KeyEvent.VK_ESCAPE);
    }

    /**
     * Updates logic for menu state and handles keyboard option selections.
     * 
     * 💡 JAVA TECHNIQUE: Modulo Arithmetic for Circular Lists
     * To warp selection from the bottom option back to the top (and vice versa), we use
     * modulo arithmetic: `selectedOption = (selectedOption + 1) % length`. 
     * To go backwards safely without negative values, we add the list length:
     * `(selectedOption - 1 + length) % length`.
     * 
     * @param deltaTime The elapsed time since the last update tick (in seconds).
     */
    @Override
    public void update(double deltaTime) {
        input.update();
        animTime += deltaTime; // Tick animation time tracker

        if (showControls) {
            // If viewing Controls panel, pressing ENTER or ESC goes back to the main menu
            if (input.isActionPressed("menu_select") || input.isActionPressed("menu_quit")) {
                showControls = false;
            }
        } else {
            // Main menu navigation
            if (input.isActionPressed("menu_up")) {
                // Circular scroll up
                selectedOption = (selectedOption - 1 + 3) % 3;
            } else if (input.isActionPressed("menu_down")) {
                // Circular scroll down
                selectedOption = (selectedOption + 1) % 3;
            } else if (input.isActionPressed("menu_select")) {
                // Action triggered by selection
                if (selectedOption == 0) {
                    app.setScene(new GameplayScene(app, input)); // Start the game!
                } else if (selectedOption == 1) {
                    showControls = true; // Show controls overlay
                } else if (selectedOption == 2) {
                    app.stop(); // Exit application
                }
            } else if (input.isActionPressed("menu_quit")) {
                app.stop(); // Exit on ESC
            }
        }
    }

    /**
     * Draws the main menu graphics directly to the Graphics2D software buffer.
     * 
     * 💡 GRAPHICS TECHNIQUE: Sinusoidal Color Pulsing
     * A sine wave ranges from -1.0 to 1.0. We map this range to 0.0 to 1.0 using:
     * `0.5 * sin(time) + 0.5`. This gives us a smooth pulsing modifier to interpolate 
     * colors (e.g. glowing neon title strings) or transparency values.
     * 
     * 💡 RENDER CONFIGURATION: Scanline Simulation
     * By drawing horizontal transparent bars spaced a few pixels apart, we emulate a classic
     * CRT arcade screen aesthetic, enhancing retro presentation.
     * 
     * @param g2d The Graphics2D object targeting the backbuffer (width=300, height=168).
     */
    @Override
    public void render(Graphics2D g2d) {
        int w = app.getBufferWidth();
        int h = app.getBufferHeight();

        // 1. Clear background to dark space color
        g2d.setColor(new Color(0x05, 0x05, 0x0f));
        g2d.fillRect(0, 0, w, h);

        // 2. Draw retro horizontal scanlines (low opacity white lines)
        g2d.setColor(new Color(255, 255, 255, 8)); // 8/255 transparency alpha
        for (int y = 0; y < h; y += 4) {
            g2d.fillRect(0, y, w, 1);
        }

        if (showControls) {
            // --- CONTROLS OVERLAY ---
            g2d.setColor(new Color(0x00, 0xff, 0xff)); // Vibrant neon cyan
            g2d.setFont(new Font("Monospaced", Font.BOLD, 16));
            g2d.drawString("HOW TO PLAY", 100, 40);

            g2d.setColor(Color.WHITE);
            g2d.setFont(new Font("Monospaced", Font.PLAIN, 10));
            g2d.drawString("Move    : WASD / Arrow Keys", 60, 70);
            g2d.drawString("Shoot   : Mouse Left Click", 60, 90);
            g2d.drawString("Debug   : F3 overlay", 60, 110);

            // Blinking prompt to return
            int alpha = (int) (127 + 127 * Math.sin(animTime * 6.0));
            g2d.setColor(new Color(255, 255, 255, alpha));
            g2d.drawString("[ Press ENTER to return ]", 75, 140);
        } else {
            // --- MAIN MENU ---
            
            // Pulse the title color and drop-shadow colors using sine wave
            float glowPulse = (float) (Math.sin(animTime * 3.0) * 0.5f + 0.5f);
            Color shadowColor = new Color((int)(120 + 80 * glowPulse), 0, 120);
            Color titleColor = new Color(255, (int)(100 + 100 * glowPulse), 0);

            g2d.setFont(new Font("Monospaced", Font.BOLD, 28));
            
            // Draw title shadow offset by 2 pixels
            g2d.setColor(shadowColor);
            g2d.drawString("MADDAWG", 88, 52);
            
            // Draw title front text
            g2d.setColor(titleColor);
            g2d.drawString("MADDAWG", 86, 50);

            // Draw menu choices list
            String[] options = { "PLAY GAME", "CONTROLS", "QUIT" };
            g2d.setFont(new Font("Monospaced", Font.BOLD, 11));

            for (int i = 0; i < options.length; i++) {
                int yPos = 85 + i * 20;
                
                if (selectedOption == i) {
                    // Fast pulsing indicator for the selected choice
                    int selectAlpha = (int) (180 + 75 * Math.sin(animTime * 10.0));
                    g2d.setColor(new Color(0, 255, 255, selectAlpha)); // Highlight neon cyan
                    
                    // Draw selector symbol and option name
                    g2d.drawString("►", 85, yPos);
                    g2d.drawString(options[i], 105, yPos);
                } else {
                    g2d.setColor(new Color(150, 150, 180)); // Dim gray-blue for unselected options
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
