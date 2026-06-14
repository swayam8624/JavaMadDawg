package com.singal.maddog.game;

import com.singal.maddog.entity.Entity;
import com.singal.maddog.entity.components.ColliderComponent;
import com.singal.maddog.entity.components.HealthComponent;
import com.singal.maddog.entity.components.LifetimeComponent;
import com.singal.maddog.entity.components.MovementComponent;
import com.singal.maddog.entity.components.PlayerControllerComponent;
import com.singal.maddog.entity.components.SpriteComponent;
import com.singal.maddog.entity.components.TransformComponent;
import com.singal.maddog.input.InputManager;
import com.singal.maddog.renderer.Animation;
import com.singal.maddog.renderer.Sprite;
import com.singal.maddog.renderer.SpriteSheet;
import com.singal.maddog.world.Level;

/**
 * Factory class for creating configured game entities with correct components.
 */
public class EntityFactory {

    public static Entity createPlayer(float xGrid, float yGrid, InputManager input, Level level, SpriteSheet playerSheet) {
        Entity player = new Entity("Player", "player");
        
        // 1. Transform
        player.addComponent(new TransformComponent(xGrid * 16.0f, yGrid * 16.0f));

        // 2. Sprite & Animations (Slices columns vertically representing walks)
        SpriteComponent spriteComp = new SpriteComponent(new Sprite(32, 32, 0xFFFF00FF));
        spriteComp.addAnimation("down", new Animation(playerSheet.getSubSheet(0, 0, 1, 3), 10));
        spriteComp.addAnimation("up", new Animation(playerSheet.getSubSheet(1, 0, 1, 3), 10));
        spriteComp.addAnimation("left", new Animation(playerSheet.getSubSheet(2, 0, 1, 3), 10));
        spriteComp.addAnimation("right", new Animation(playerSheet.getSubSheet(3, 0, 1, 3), 10));
        spriteComp.playAnimation("down");
        player.addComponent(spriteComp);

        // 3. Movement
        MovementComponent movement = new MovementComponent();
        movement.setSpeed(1.5f);
        player.addComponent(movement);

        // 4. Collider (14x14 box, slightly offset to fit nicely)
        ColliderComponent collider = new ColliderComponent(1, 1, 14, 14);
        collider.setLayer(ColliderComponent.CollisionLayer.PLAYER);
        player.addComponent(collider);

        // 5. Controller
        player.addComponent(new PlayerControllerComponent(input, level));

        // 6. Health
        player.addComponent(new HealthComponent(100));

        return player;
    }

    public static Entity createWizardProjectile(float x, float y, float dx, float dy, Level level, Sprite sprite) {
        Entity projectile = new Entity("WizardProjectile", "projectile");
        
        // 1. Transform
        projectile.addComponent(new TransformComponent(x, y));

        // 2. Sprite
        projectile.addComponent(new SpriteComponent(sprite));

        // 3. Movement
        MovementComponent movement = new MovementComponent() {
            @Override
            public void update(double deltaTime) {
                // Projectiles move constantly in their trajectory direction
                TransformComponent tc = owner.getComponent(TransformComponent.class);
                if (tc != null) {
                    tc.setX(tc.getX() + dx * getSpeed());
                    tc.setY(tc.getY() + dy * getSpeed());
                }
            }
        };
        movement.setSpeed(3.0f);
        projectile.addComponent(movement);

        // 4. Collider (8x8 box)
        ColliderComponent collider = new ColliderComponent(4, 4, 8, 8);
        collider.setLayer(ColliderComponent.CollisionLayer.PROJECTILE);
        projectile.addComponent(collider);

        // 5. Lifetime (destroys itself after 2 seconds)
        projectile.addComponent(new LifetimeComponent(2.0));

        return projectile;
    }

    public static Entity createChaserEnemy(float xGrid, float yGrid, Entity target, Level level, SpriteSheet chaserSheet) {
        Entity enemy = new Entity("Chaser", "enemy");
        
        // 1. Transform
        enemy.addComponent(new TransformComponent(xGrid * 16.0f, yGrid * 16.0f));

        // 2. Sprite & Animations (Slices columns vertically representing walks)
        SpriteComponent spriteComp = new SpriteComponent(new Sprite(32, 32, 0xFFFF00FF));
        spriteComp.addAnimation("down", new Animation(chaserSheet.getSubSheet(0, 0, 1, 3), 12));
        spriteComp.addAnimation("up", new Animation(chaserSheet.getSubSheet(1, 0, 1, 3), 12));
        spriteComp.addAnimation("left", new Animation(chaserSheet.getSubSheet(2, 0, 1, 3), 12));
        spriteComp.addAnimation("right", new Animation(chaserSheet.getSubSheet(3, 0, 1, 3), 12));
        spriteComp.playAnimation("down");
        enemy.addComponent(spriteComp);

        // 3. Movement
        MovementComponent movement = new MovementComponent();
        movement.setSpeed(0.8f);
        enemy.addComponent(movement);

        // 4. Collider (14x14 box)
        ColliderComponent collider = new ColliderComponent(1, 1, 14, 14);
        collider.setLayer(ColliderComponent.CollisionLayer.ENEMY);
        enemy.addComponent(collider);

        // 5. Health
        enemy.addComponent(new HealthComponent(30));

        // 6. Simple AI Behavior component
        enemy.addComponent(new com.singal.maddog.entity.Component() {
            @Override
            public void update(double deltaTime) {
                if (target.isDestroyed()) return;

                TransformComponent tc = owner.getComponent(TransformComponent.class);
                TransformComponent targetTc = target.getComponent(TransformComponent.class);
                MovementComponent mv = owner.getComponent(MovementComponent.class);
                SpriteComponent sc = owner.getComponent(SpriteComponent.class);

                if (tc != null && targetTc != null && mv != null) {
                    float dx = targetTc.getX() - tc.getX();
                    float dy = targetTc.getY() - tc.getY();
                    float distance = (float) Math.sqrt(dx * dx + dy * dy);

                    // If target is in range, chase it
                    if (distance < 120.0f) {
                        float xa = dx > 0 ? 1 : (dx < 0 ? -1 : 0);
                        float ya = dy > 0 ? 1 : (dy < 0 ? -1 : 0);
                        mv.move(xa * mv.getSpeed(), ya * mv.getSpeed(), level.getTileMap());
                        
                        // Update directional animations
                        if (sc != null) {
                            if (ya < 0) {
                                sc.playAnimation("up");
                            } else if (ya > 0) {
                                sc.playAnimation("down");
                            } else if (xa < 0) {
                                sc.playAnimation("left");
                            } else if (xa > 0) {
                                sc.playAnimation("right");
                            }

                            Animation activeAnim = sc.getActiveAnimation();
                            if (activeAnim != null) {
                                activeAnim.setPlaying(true);
                            }
                        }
                    } else {
                        // Idle state
                        if (sc != null) {
                            Animation activeAnim = sc.getActiveAnimation();
                            if (activeAnim != null) {
                                activeAnim.setPlaying(false);
                                activeAnim.setFrame(0);
                            }
                        }
                    }
                }
            }
        });

        return enemy;
    }
}
