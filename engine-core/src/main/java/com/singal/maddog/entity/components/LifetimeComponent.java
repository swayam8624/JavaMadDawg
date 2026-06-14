package com.singal.maddog.entity.components;

import com.singal.maddog.entity.Component;

/**
 * Automatically destroys the parent entity after a set duration.
 */
public class LifetimeComponent extends Component {
    private double lifetimeSeconds;
    private double elapsedSeconds = 0.0;

    public LifetimeComponent(double lifetimeSeconds) {
        this.lifetimeSeconds = lifetimeSeconds;
    }

    @Override
    public void update(double deltaTime) {
        elapsedSeconds += deltaTime;
        if (elapsedSeconds >= lifetimeSeconds) {
            owner.destroy();
        }
    }
}
