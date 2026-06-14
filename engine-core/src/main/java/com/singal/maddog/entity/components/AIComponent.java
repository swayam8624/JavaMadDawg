package com.singal.maddog.entity.components;

import com.singal.maddog.entity.Component;
import com.singal.maddog.math.Vector2f;

/**
 * Handles basic state machine decisions for entity AI (Wander, Chase, Attack).
 */
public abstract class AIComponent extends Component {
    public enum AIState {
        WANDER, CHASE, ATTACK
    }

    protected AIState currentState = AIState.WANDER;
    protected float detectionRange = 100.0f;
    protected float attackRange = 32.0f;

    @Override
    public void update(double deltaTime) {
        if (owner == null) return;
        updateBehavior(deltaTime);
    }

    protected abstract void updateBehavior(double deltaTime);

    public AIState getCurrentState() {
        return currentState;
    }

    public void setCurrentState(AIState currentState) {
        this.currentState = currentState;
    }

    public float getDetectionRange() {
        return detectionRange;
    }

    public void setDetectionRange(float detectionRange) {
        this.detectionRange = detectionRange;
    }

    public float getAttackRange() {
        return attackRange;
    }

    public void setAttackRange(float attackRange) {
        this.attackRange = attackRange;
    }
}
