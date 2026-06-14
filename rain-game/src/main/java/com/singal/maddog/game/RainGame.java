package com.singal.maddog.game;

import com.singal.maddog.core.Application;
import com.singal.maddog.input.InputManager;

/**
 * 🎓 RainGame is the main executable entry point for the MadDawg game.
 * 
 * 💡 TECH NOTE: Java AWT and Swing JFrame Structure
 * Java uses Abstract Window Toolkit (AWT) and Swing for drawing native windows. 
 * The {@link Application} class subclassing {@link java.awt.Canvas} represents 
 * a drawable surface, which we pack inside a {@link javax.swing.JFrame} window.
 */
public class RainGame {
    
    /**
     * The main execution entry point for the JVM.
     * 
     * 💡 JAVA TECHNIQUE: Static Main Methods
     * `public static void main` is the standard signature the JVM searches for to 
     * launch an application. We instantiate our engine configuration here, set up 
     * hardware input bindings, register the starting Scene, and kick off the loop thread.
     * 
     * @param args Command-line arguments passed to the JVM.
     */
    public static void main(String[] args) {
        // Instantiate the application at standard 300x168 buffer scaled by 3 (900x504 window size)
        // 💡 DESIGN PATTERN: Software Backbuffering
        // We render graphics to a small 300x168 canvas for a crisp retro pixel-art aesthetic,
        // then scale it up by 3x when drawing to the 900x504 screen.
        Application app = new Application("MadDawg", 300, 168, 3);
        
        // Instantiate our custom InputManager
        InputManager input = new InputManager();
        
        // Register key and mouse listeners to intercept OS window inputs.
        // 💡 JAVA EVENT HANDLING: Listener Interfaces
        // We register our input object to the Canvas so it receives hardware interrupts 
        // when keys are pressed or mouse clicks occur.
        app.addKeyListener(input);
        app.addMouseListener(input);
        app.addMouseMotionListener(input);

        // Bind the initial scene to the Application container
        // 💡 DESIGN PATTERN: Scene Graph / State Machine
        // Rather than hardcoding screen branches inside a single big loop, we swap out
        // the active Scene (e.g. MainMenuScene -> GameplayScene), delegating update
        // and render ticks to the active scene object.
        app.setScene(new MainMenuScene(app, input));

        // Start the game loop thread
        // 💡 CONCURRENCY NOTE: Game Loop Threading
        // This launches the GameLoop runner. It spins up a separate background thread
        // to handle update ticks and drawing ticks separately from the main OS launcher thread.
        app.start();
    }
}
