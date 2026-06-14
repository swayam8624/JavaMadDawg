package com.singal.maddog.entity.components;

import com.singal.maddog.entity.Component;

/**
 * Manages entity health state, damage, healing, and death callbacks.
 */
public class HealthComponent extends Component {
    private int maxHealth;
    private int currentHealth;

    public HealthComponent(int maxHealth) {
        this.maxHealth = maxHealth;
        this.currentHealth = maxHealth;
    }

    public void takeDamage(int amount) {
        if (owner.isDestroyed()) return;
        
        currentHealth -= amount;
        if (currentHealth <= 0) {
            currentHealth = 0;
            owner.destroy(); // Destroy entity when health hits zero
        }
    }

    public void heal(int amount) {
        currentHealth += amount;
        if (currentHealth > maxHealth) {
            currentHealth = maxHealth;
        }
    }

    public int getCurrentHealth() {
        return currentHealth;
    }

    public int getMaxHealth() {
        return maxHealth;
    }

    public float getHealthPercentage() {
        return (float) currentHealth / maxHealth;
    }

    public boolean isDead() {
        return currentHealth <= 0;
    }
}
