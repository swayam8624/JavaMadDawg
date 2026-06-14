package com.singal.maddog.renderer;

/**
 * An abstraction for rendering operations, allowing software pixel blitting, 
 * AWT graphics, or future OpenGL/Vulkan rendering backends.
 */
public interface RenderContext {
    void clear(int color);
    
    void setCameraOffset(int xOffset, int yOffset);
    
    int getXOffset();
    
    int getYOffset();
    
    void drawPixel(int x, int y, int color, boolean useCamera);
    
    void drawRect(int x, int y, int width, int height, int color, boolean useCamera);
    
    void fillRect(int x, int y, int width, int height, int color, boolean useCamera);
    
    void drawSprite(int x, int y, Sprite sprite, boolean useCamera);
    
    void drawSprite(int x, int y, Sprite sprite, boolean flipX, boolean flipY, boolean useCamera);
    
    int getWidth();
    
    int getHeight();
}
