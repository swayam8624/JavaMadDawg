package com.singal.maddog.entity.components;

import com.singal.maddog.entity.Component;
import com.singal.maddog.renderer.Animation;
import com.singal.maddog.renderer.RenderContext;
import com.singal.maddog.renderer.Sprite;
import java.util.HashMap;
import java.util.Map;

/**
 * Handles rendering of static Sprites or dynamic Animations.
 * Supports multiple named animations (e.g. "walk_up", "walk_down").
 */
public class SpriteComponent extends Component {
    private Sprite sprite;
    private final Map<String, Animation> animations = new HashMap<>();
    private String activeAnimationName;
    private boolean flipX = false;
    private boolean flipY = false;

    public SpriteComponent(Sprite sprite) {
        this.sprite = sprite;
    }

    public SpriteComponent(Animation defaultAnimation) {
        this.animations.put("default", defaultAnimation);
        this.activeAnimationName = "default";
    }

    @Override
    public void update(double deltaTime) {
        Animation activeAnim = getActiveAnimation();
        if (activeAnim != null) {
            activeAnim.update();
        }
    }

    @Override
    public void render(RenderContext context) {
        TransformComponent transform = owner.getComponent(TransformComponent.class);
        if (transform == null) return;

        Sprite activeSprite = getActiveSprite();
        if (activeSprite != null) {
            int xPos = (int) transform.getX();
            int yPos = (int) transform.getY();
            
            context.drawSprite(xPos, yPos, activeSprite, flipX, flipY, true);
        }
    }

    public Sprite getActiveSprite() {
        Animation activeAnim = getActiveAnimation();
        if (activeAnim != null) {
            return activeAnim.getSprite();
        }
        return sprite;
    }

    public void addAnimation(String name, Animation animation) {
        animations.put(name, animation);
        if (activeAnimationName == null) {
            activeAnimationName = name;
        }
    }

    public void playAnimation(String name) {
        if (animations.containsKey(name)) {
            if (!name.equals(activeAnimationName)) {
                activeAnimationName = name;
                animations.get(name).reset();
            }
        }
    }

    public Animation getActiveAnimation() {
        if (activeAnimationName != null) {
            return animations.get(activeAnimationName);
        }
        return null;
    }

    public String getActiveAnimationName() {
        return activeAnimationName;
    }

    public void setSprite(Sprite sprite) {
        this.sprite = sprite;
        this.activeAnimationName = null;
    }

    public boolean isFlipX() {
        return flipX;
    }

    public void setFlipX(boolean flipX) {
        this.flipX = flipX;
    }

    public boolean isFlipY() {
        return flipY;
    }

    public void setFlipY(boolean flipY) {
        this.flipY = flipY;
    }
}
