package com.singal.maddog.game;

import com.singal.maddog.entity.Component;

/**
 * 🎓 InvincibilityComponent tracks transient state data for invincibility frames.
 * 
 * 💡 TECH NOTE: Entity Component System (ECS) Design Pattern
 * In a pure ECS, components are simple data containers holding no logic, and "Systems" 
 * hold all the behavior. In this "ECS-lite" implementation, we use a hybrid model:
 * components extend the {@link Component} base class and override update() to encapsulate 
 * their own data updates, which makes the architecture simpler and more object-oriented.
 */
public class InvincibilityComponent extends Component {
    
    /**
     * The remaining time (in seconds) the player is immune to damage.
     * When this value is > 0, the player cannot take damage, and the sprite flashes.
     */
    private float timer = 0.0f;

    /**
     * Triggers invincibility by setting the timer to the specified duration.
     * 
     * @param duration The duration in seconds the invincibility should last.
     */
    public void trigger(float duration) {
        this.timer = duration;
    }

    /**
     * Updates the invincibility timer, ticking it down towards zero.
     * 
     * 💡 JAVA TECHNIQUE: Time-Based vs. Frame-Based Updates
     * Ticking is done using `deltaTime` (fractional seconds passed since the last frame) 
     * rather than subtracting a constant per frame. This ensures that the invincibility 
     * period lasts exactly the same duration (e.g. 1.0 second) regardless of whether the 
     * game runs at 30 FPS, 60 FPS, or 200 FPS!
     * 
     * @param deltaTime The elapsed time since the last update tick (in seconds).
     */
    @Override
    public void update(double deltaTime) {
        if (timer > 0.0f) {
            // Decrement the timer by the fraction of a second that passed
            timer -= deltaTime;
            
            // Prevent float underflow below zero
            if (timer < 0.0f) {
                timer = 0.0f;
            }
        }
    }

    /**
     * Checks if the entity is currently invincible.
     * 
     * @return true if the invincibility timer is greater than zero.
     */
    public boolean isInvincible() {
        return timer > 0.0f;
    }

    /**
     * Gets the remaining invincibility time.
     * Useful for debugging or rendering remaining immunity duration.
     * 
     * @return The remaining duration in seconds.
     */
    public float getRemainingTime() {
        return timer;
    }
}
