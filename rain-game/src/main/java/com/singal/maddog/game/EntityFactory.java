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
import com.singal.maddog.renderer.RenderContext;
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
        SpriteComponent spriteComp = new SpriteComponent(new Sprite(32, 32, 0xFFFF00FF)) {
            @Override
            public void render(RenderContext context) {
                InvincibilityComponent inv = owner.getComponent(InvincibilityComponent.class);
                if (inv != null && inv.isInvincible()) {
                    if (((int)(System.currentTimeMillis() / 80) % 2) == 0) {
                        return;
                    }
                }
                super.render(context);
            }
        };
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

        // 4. Collider (14x14 box, centered inside the 32x32 sprite bounds)
        ColliderComponent collider = new ColliderComponent(9, 9, 14, 14);
        collider.setLayer(ColliderComponent.CollisionLayer.PLAYER);
        player.addComponent(collider);

        // 5. Controller
        player.addComponent(new PlayerControllerComponent(input, level));

        // 6. Health
        player.addComponent(new HealthComponent(100));

        // 7. Invincibility tracker
        player.addComponent(new InvincibilityComponent());

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

        // 4. Collider (8x8 box, centered inside the 16x16 sprite bounds)
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

        // 2. Sprite & Animations
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

        // 4. Collider (14x14 box, centered inside the 32x32 sprite bounds)
        ColliderComponent collider = new ColliderComponent(9, 9, 14, 14);
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

    public static Entity createHealthPotion(float xGrid, float yGrid) {
        Entity potion = new Entity("HealthPotion", "potion");
        
        // Transform
        potion.addComponent(new TransformComponent(xGrid * 16.0f + 4.0f, yGrid * 16.0f + 4.0f));
        
        // Green color block sprite (8x8) representing a potion
        potion.addComponent(new SpriteComponent(new Sprite(8, 8, 0xFF00FF00)));
        
        // Collider for pickups (8x8)
        ColliderComponent collider = new ColliderComponent(0, 0, 8, 8);
        collider.setLayer(ColliderComponent.CollisionLayer.NONE); // trigger layer
        potion.addComponent(collider);
        
        return potion;
    }

    public static Entity createBossEnemy(float xGrid, float yGrid, Entity target, Level level, SpriteSheet chaserSheet) {
        Entity boss = new Entity("Boss", "boss");
        
        // 1. Transform
        boss.addComponent(new TransformComponent(xGrid * 16.0f, yGrid * 16.0f));

        // 2. Sprite & Animations using custom 2x scale render component
        SpriteComponent bossSpriteComp = new SpriteComponent(new Sprite(32, 32, 0xFFFF00FF)) {
            @Override
            public void render(RenderContext context) {
                TransformComponent transform = owner.getComponent(TransformComponent.class);
                if (transform == null) return;

                Sprite activeSprite = getActiveSprite();
                if (activeSprite != null) {
                    int xPos = (int) transform.getX();
                    int yPos = (int) transform.getY();
                    int sw = activeSprite.getWidth();
                    int sh = activeSprite.getHeight();
                    int[] pixels = activeSprite.getPixels();
                    
                    // Render 2x scaled sprite pixel-by-pixel
                    for (int sy = 0; sy < sh; sy++) {
                        int sourceY = isFlipY() ? (sh - 1 - sy) : sy;
                        for (int sx = 0; sx < sw; sx++) {
                            int sourceX = isFlipX() ? (sw - 1 - sx) : sx;
                            int color = pixels[sourceX + sourceY * sw];
                            
                            // Check pink transparency key
                            if ((color & 0xFFFFFF) == 0xFFFF00FF) continue;
                            
                            // Draw a 2x2 block of pixels
                            for (int dy = 0; dy < 2; dy++) {
                                for (int dx = 0; dx < 2; dx++) {
                                    context.drawPixel(xPos + sx * 2 + dx, yPos + sy * 2 + dy, color, true);
                                }
                            }
                        }
                    }
                }
            }
        };

        bossSpriteComp.addAnimation("down", new Animation(chaserSheet.getSubSheet(0, 0, 1, 3), 12));
        bossSpriteComp.addAnimation("up", new Animation(chaserSheet.getSubSheet(1, 0, 1, 3), 12));
        bossSpriteComp.addAnimation("left", new Animation(chaserSheet.getSubSheet(2, 0, 1, 3), 12));
        bossSpriteComp.addAnimation("right", new Animation(chaserSheet.getSubSheet(3, 0, 1, 3), 12));
        bossSpriteComp.playAnimation("down");
        boss.addComponent(bossSpriteComp);

        // 3. Movement (Boss moves slightly faster than regular chasers)
        MovementComponent movement = new MovementComponent();
        movement.setSpeed(1.1f);
        boss.addComponent(movement);

        // 4. Collider (Large 28x28 box, centered inside the 2x scaled sprite bounds of 64x64)
        ColliderComponent collider = new ColliderComponent(18, 18, 28, 28);
        collider.setLayer(ColliderComponent.CollisionLayer.ENEMY);
        boss.addComponent(collider);

        // 5. Health (Boss is tanky!)
        boss.addComponent(new HealthComponent(200));

        // 6. AI behavior (Boss chases player and shoots occasionally!)
        boss.addComponent(new com.singal.maddog.entity.Component() {
            private float shootTimer = 0.0f;

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

                    // Always chase if distance is reasonable
                    if (distance < 240.0f) {
                        float xa = dx > 0 ? 1 : (dx < 0 ? -1 : 0);
                        float ya = dy > 0 ? 1 : (dy < 0 ? -1 : 0);
                        mv.move(xa * mv.getSpeed(), ya * mv.getSpeed(), level.getTileMap());
                        
                        // Update animations
                        if (sc != null) {
                            if (ya < 0) sc.playAnimation("up");
                            else if (ya > 0) sc.playAnimation("down");
                            else if (xa < 0) sc.playAnimation("left");
                            else if (xa > 0) sc.playAnimation("right");

                            Animation activeAnim = sc.getActiveAnimation();
                            if (activeAnim != null) activeAnim.setPlaying(true);
                        }

                        // Shoot projectiles at player every 1.5 seconds
                        shootTimer -= deltaTime;
                        if (shootTimer <= 0.0f) {
                            shootTimer = 1.5f;
                            float bx = tc.getX() + 32.0f; // Center of 64x64 boss
                            float by = tc.getY() + 32.0f;
                            float targetX = targetTc.getX() + 16.0f;
                            float targetY = targetTc.getY() + 16.0f;
                            
                             float pdx = targetX - bx;
                             float pdy = targetY - by;
                             float len = (float) Math.sqrt(pdx * pdx + pdy * pdy);
                             if (len > 0) {
                                 final float dirX = pdx / len;
                                 final float dirY = pdy / len;
                                 // Create projectile that is hostle to the player
                                 Entity proj = new Entity("BossProjectile", "boss_projectile");
                                 proj.addComponent(new TransformComponent(bx, by));
                                 // Red color block projectile (6x6)
                                 proj.addComponent(new SpriteComponent(new Sprite(6, 6, 0xFFFF0000)));
                                 
                                 MovementComponent pmv = new MovementComponent() {
                                     @Override
                                     public void update(double dt) {
                                         TransformComponent ptc = owner.getComponent(TransformComponent.class);
                                         if (ptc != null) {
                                             ptc.setX(ptc.getX() + dirX * getSpeed());
                                             ptc.setY(ptc.getY() + dirY * getSpeed());
                                         }
                                     }
                                 };
                                pmv.setSpeed(2.5f);
                                proj.addComponent(pmv);

                                ColliderComponent pCol = new ColliderComponent(1, 1, 4, 4);
                                pCol.setLayer(ColliderComponent.CollisionLayer.PROJECTILE);
                                proj.addComponent(pCol);

                                proj.addComponent(new LifetimeComponent(3.0));
                                level.addEntity(proj);
                            }
                        }
                    }
                }
            }
        });

        return boss;
    }
}
