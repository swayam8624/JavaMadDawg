package com.singal.maddog.game;

import com.singal.maddog.core.Application;
import com.singal.maddog.input.InputManager;

/**
 * Main game executable entry point for Rain Remastered.
 */
public class RainGame {
    public static void main(String[] args) {
        // Instantiate the application at standard 300x168 buffer scaled by 3 (900x504 window size)
        Application app = new Application("MadDawg", 300, 168, 3);
        
        InputManager input = new InputManager();
        app.addKeyListener(input);
        app.addMouseListener(input);
        app.addMouseMotionListener(input);

        // Bind default scenes
        app.setScene(new MainMenuScene(app, input));

        // Start main thread loop
        app.start();
    }
}
