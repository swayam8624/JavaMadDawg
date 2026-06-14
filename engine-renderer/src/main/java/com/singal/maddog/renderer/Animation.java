package com.singal.maddog.renderer;

/**
 * Handles sprite sheet cell cycling for animated game entities.
 */
public class Animation {
    private final Sprite[] frames;
    private final int updateDelay; // In game ticks (assuming 60 UPS)
    
    private int currentFrame = 0;
    private int ticksElapsed = 0;
    private boolean playing = true;

    public Animation(Sprite[] frames, int updateDelay) {
        this.frames = frames;
        this.updateDelay = updateDelay;
    }

    public Animation(SpriteSheet sheet, int updateDelay) {
        this.frames = sheet.getSprites();
        this.updateDelay = updateDelay;
    }

    public void update() {
        if (!playing || frames.length <= 1) return;

        ticksElapsed++;
        if (ticksElapsed >= updateDelay) {
            ticksElapsed = 0;
            currentFrame = (currentFrame + 1) % frames.length;
        }
    }

    public void reset() {
        currentFrame = 0;
        ticksElapsed = 0;
    }

    public void setFrame(int frameIndex) {
        if (frameIndex >= 0 && frameIndex < frames.length) {
            this.currentFrame = frameIndex;
            this.ticksElapsed = 0;
        }
    }

    public Sprite getSprite() {
        if (frames.length == 0) return null;
        return frames[currentFrame];
    }

    public int getFrameIndex() {
        return currentFrame;
    }

    public int getFramesCount() {
        return frames.length;
    }

    public boolean isPlaying() {
        return playing;
    }

    public void setPlaying(boolean playing) {
        this.playing = playing;
    }
}
