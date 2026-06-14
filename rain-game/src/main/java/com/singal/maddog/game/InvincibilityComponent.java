package com.singal.maddog.game;

import com.singal.maddog.entity.Component;

/**
 * Custom game component that tracks player invincibility frames.
 */
public class InvincibilityComponent extends Component {
    private float timer = 0.0f;

    public void trigger(float duration) {
        this.timer = duration;
    }

    public void update(double deltaTime) {
        if (timer > 0.0f) {
            timer -= deltaTime;
        }
    }

    public boolean isInvincible() {
        return timer > 0.0f;
    }

    public float getRemainingTime() {
        return timer;
    }
}
