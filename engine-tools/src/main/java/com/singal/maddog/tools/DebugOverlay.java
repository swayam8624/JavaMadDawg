package com.singal.maddog.tools;

import com.singal.maddog.core.Application;
import com.singal.maddog.world.Level;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;

/**
 * Renders statistical diagnostic overlay text (FPS, UPS, memory, entities) to screen.
 */
public class DebugOverlay {
    private static final Font DIAGNOSTIC_FONT = new Font("Monospaced", Font.BOLD, 12);
    private static final Color BACKGROUND_SHADOW = new Color(0, 0, 0, 150);
    private static final Color TEXT_COLOR = Color.GREEN;

    public void render(Graphics2D g2d, Application app, Level level, float camX, float camY) {
        g2d.setFont(DIAGNOSTIC_FONT);

        // Fetch metrics
        int fps = app.getFps();
        int ups = app.getUps();
        int entityCount = level != null ? level.getEntities().size() : 0;
        
        long totalMemory = Runtime.getRuntime().totalMemory();
        long freeMemory = Runtime.getRuntime().freeMemory();
        long usedMemory = (totalMemory - freeMemory) / (1024 * 1024);

        // Prepare diagnostic text lines
        String[] debugLines = {
            "FPS: " + fps + " | UPS: " + ups,
            "Entities: " + entityCount,
            "Camera Pos: X=" + (int) camX + ", Y=" + (int) camY,
            "Used Memory: " + usedMemory + " MB"
        };

        // Render black semi-transparent background box
        int boxWidth = 220;
        int boxHeight = debugLines.length * 16 + 10;
        g2d.setColor(BACKGROUND_SHADOW);
        g2d.fillRect(5, 5, boxWidth, boxHeight);

        // Render text lines
        g2d.setColor(TEXT_COLOR);
        for (int i = 0; i < debugLines.length; i++) {
            g2d.drawString(debugLines[i], 10, 20 + (i * 16));
        }
    }
}
