# Rendering Pipeline

Rain Remastered uses a software pixel-pushing pipeline that is fully decoupled from Swing graphics buffers.

---

## 1. Blitting Sprites
In `SoftwareRenderContext.java`, a sprite is drawn by copying its pixels onto the backbuffer pixel array:

$$\text{backbuffer}[tx + ty \times \text{width}] = \text{spritePixels}[sx + sy \times \text{spriteWidth}]$$

```java
int color = spritePixels[sourceX + sourceY * spriteWidth];
if ((color & 0xFFFFFF) != 0xFF00FF) { // Skip magenta transparent key
    pixels[targetX + targetY * width] = color;
}
```

---

## 2. Decoupled Viewport Camera
The camera translation (offset coordinates) is set on the `RenderContext` before drawing the scene:
```java
renderContext.setCameraOffset((int) camera.getX(), (int) camera.getY());
```
Any rendering commands marked with `useCamera = true` automatically apply these offsets.
