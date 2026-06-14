package com.singal.maddog.entity.components;

import com.singal.maddog.entity.Component;
import com.singal.maddog.input.InputManager;
import com.singal.maddog.renderer.Animation;
import com.singal.maddog.world.Level;

/**
 * Reads inputs from the InputManager and drives the owner's movement, animations, and actions.
 */
public class PlayerControllerComponent extends Component {
    private final InputManager inputManager;
    private final Level level;

    public PlayerControllerComponent(InputManager inputManager, Level level) {
        this.inputManager = inputManager;
        this.level = level;
    }

    @Override
    public void update(double deltaTime) {
        MovementComponent movement = owner.getComponent(MovementComponent.class);
        SpriteComponent spriteComp = owner.getComponent(SpriteComponent.class);
        if (movement == null) return;

        float xa = 0;
        float ya = 0;

        if (inputManager.isActionHeld("move_up")) ya -= 1;
        if (inputManager.isActionHeld("move_down")) ya += 1;
        if (inputManager.isActionHeld("move_left")) xa -= 1;
        if (inputManager.isActionHeld("move_right")) xa += 1;

        if (xa != 0 || ya != 0) {
            float speed = movement.getSpeed();
            
            // Normalize direction if moving diagonally
            if (xa != 0 && ya != 0) {
                xa *= 0.7071f;
                ya *= 0.7071f;
            }

            movement.move(xa * speed, ya * speed, level.getTileMap());

            // Update directional animations
            if (spriteComp != null) {
                if (ya < 0) {
                    spriteComp.playAnimation("up");
                } else if (ya > 0) {
                    spriteComp.playAnimation("down");
                } else if (xa < 0) {
                    spriteComp.playAnimation("left");
                } else if (xa > 0) {
                    spriteComp.playAnimation("right");
                }
                
                Animation activeAnim = spriteComp.getActiveAnimation();
                if (activeAnim != null) {
                    activeAnim.setPlaying(true);
                }
            }
        } else {
            // Idle state: stop cycling animation and snap to standing center frame (frame 1)
            if (spriteComp != null) {
                Animation activeAnim = spriteComp.getActiveAnimation();
                if (activeAnim != null) {
                    activeAnim.setPlaying(false);
                    activeAnim.setFrame(0); // Snap to neutral frame
                }
            }
        }
    }
}
