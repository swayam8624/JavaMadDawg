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
 * 🎓 EntityFactory is a factory class designed to instantiate fully configured game entities.
 * 
 * 💡 DESIGN PATTERN: Factory Pattern
 * Creating entities in a component system requires attaching multiple components, setting speed,
 * defining animation sub-sheets, and specifying AABB colliders. Rather than repeating this
 * setup code across different scenes, the Factory pattern centralizes entity construction,
 * keeping gameplay code clean and maintainable.
 */
public class EntityFactory {

    /**
     * Creates a playable player entity with animations, inputs, and a centered physical collider.
     * 
     * 💡 GRAPHICS NOTE: Sub-Sheet Animation Slicing
     * The character sheets are vertically sliced columns where each column represents a direction
     * (0=Down, 1=Up, 2=Left, 3=Right) and the rows are the walk frames (0, 1, 2). Slicing columns 
     * vertically ensures that walk frames match the character's facing direction.
     * 
     * @param xGrid       The starting tile coordinate along the X axis.
     * @param yGrid       The starting tile coordinate along the Y axis.
     * @param input       The hardware input manager.
     * @param level       The active level the player resides in.
     * @param playerSheet The character's spritesheet textures.
     * @return A fully populated Player entity.
     */
    public static Entity createPlayer(float xGrid, float yGrid, InputManager input, Level level, SpriteSheet playerSheet) {
        Entity player = new Entity("Player", "player");
        
        // 1. Transform: Convert tile coordinates to pixel coordinates (1 tile = 16 pixels)
        player.addComponent(new TransformComponent(xGrid * 16.0f, yGrid * 16.0f));

        // 2. Sprite & Animations
        // We override the render method of SpriteComponent here to flash the player sprite on invincibility!
        SpriteComponent spriteComp = new SpriteComponent(new Sprite(32, 32, 0xFFFF00FF)) {
            @Override
            public void render(RenderContext context) {
                InvincibilityComponent inv = owner.getComponent(InvincibilityComponent.class);
                if (inv != null && inv.isInvincible()) {
                    // Flash by skipping rendering every alternate 80 milliseconds
                    if (((int)(System.currentTimeMillis() / 80) % 2) == 0) {
                        return; // Skip drawing this frame to simulate flickering
                    }
                }
                super.render(context);
            }
        };
        // Slice columns vertically to load animations
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
        // 💡 COLLISION NOTE: Centered Box Offset
        // Sprites are 32x32. A 14x14 collider is centered by offsetting it by (32 - 14)/2 = 9 pixels.
        // This ensures combat projectiles aiming at the character's visual center will intersect the box.
        ColliderComponent collider = new ColliderComponent(9, 9, 14, 14);
        collider.setLayer(ColliderComponent.CollisionLayer.PLAYER);
        player.addComponent(collider);

        // 5. Controller: Translates key inputs to movement actions
        player.addComponent(new PlayerControllerComponent(input, level));

        // 6. Health
        player.addComponent(new HealthComponent(100));

        // 7. Invincibility tracker: manages invincibility timers
        player.addComponent(new InvincibilityComponent());

        return player;
    }

    /**
     * Creates a wizard projectile entity traveling in a specified direction.
     * 
     * @param x       Spawn position X (pixels).
     * @param y       Spawn position Y (pixels).
     * @param dx      Horizontal direction component (normalized).
     * @param dy      Vertical direction component (normalized).
     * @param level   The active level.
     * @param sprite  The visual sprite representing the projectile.
     * @return A wizard projectile Entity.
     */
    public static Entity createWizardProjectile(float x, float y, float dx, float dy, Level level, Sprite sprite) {
        Entity projectile = new Entity("WizardProjectile", "projectile");
        
        // 1. Transform
        projectile.addComponent(new TransformComponent(x, y));

        // 2. Sprite
        projectile.addComponent(new SpriteComponent(sprite));

        // 3. Movement: Overrides update to move constantly in a straight trajectory
        // 💡 JAVA TECHNIQUE: Anonymous Inner Classes
        // Instead of writing a separate Java file for projectile movement, we instantiate
        // MovementComponent on the fly and override update() to implement straight-line motion.
        MovementComponent movement = new MovementComponent() {
            @Override
            public void update(double deltaTime) {
                TransformComponent tc = owner.getComponent(TransformComponent.class);
                if (tc != null) {
                    // Constant velocity movement (not using slide collision check since projectiles pass through walls to explode)
                    tc.setX(tc.getX() + dx * getSpeed());
                    tc.setY(tc.getY() + dy * getSpeed());
                }
            }
        };
        movement.setSpeed(3.0f);
        projectile.addComponent(movement);

        // 4. Collider (8x8 box, centered inside the 16x16 projectile sprite bounds)
        ColliderComponent collider = new ColliderComponent(4, 4, 8, 8);
        collider.setLayer(ColliderComponent.CollisionLayer.PROJECTILE);
        projectile.addComponent(collider);

        // 5. Lifetime (destroys itself after 2 seconds to free memory)
        projectile.addComponent(new LifetimeComponent(2.0));

        return projectile;
    }

    /**
     * Creates a basic chaser enemy that follows the target.
     * 
     * @param xGrid       The starting tile coordinate X.
     * @param yGrid       The starting tile coordinate Y.
     * @param target      The player entity to chase.
     * @param level       The active level.
     * @param chaserSheet The chaser's animations sheet.
     * @return A chaser enemy Entity.
     */
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

        // 6. AI Behavior Component (Anonymous component override)
        // 💡 MATH NOTE: 2D Distance Calculation
        // Distance is computed using the Pythagorean theorem: distance = sqrt(dx^2 + dy^2).
        // If the player is within 120 pixels, we track the player's position and move towards it.
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

                    if (distance < 120.0f) {
                        // Move toward player coordinate (checking tile solidity to slide along corners)
                        float xa = dx > 0 ? 1 : (dx < 0 ? -1 : 0);
                        float ya = dy > 0 ? 1 : (dy < 0 ? -1 : 0);
                        mv.move(xa * mv.getSpeed(), ya * mv.getSpeed(), level.getTileMap());
                        
                        // Play appropriate directional animations
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

    /**
     * Creates a static health potion pickup that heals the player when touched.
     * 
     * @param xGrid Grid coordinate X.
     * @param yGrid Grid coordinate Y.
     * @return A health potion Entity.
     */
    public static Entity createHealthPotion(float xGrid, float yGrid) {
        Entity potion = new Entity("HealthPotion", "potion");
        
        // Transform (Centered slightly in the 16x16 tile space)
        potion.addComponent(new TransformComponent(xGrid * 16.0f + 4.0f, yGrid * 16.0f + 4.0f));
        
        // Green color block sprite (8x8) representing a potion
        potion.addComponent(new SpriteComponent(new Sprite(8, 8, 0xFF00FF00)));
        
        // Collider for pickups (8x8 trigger collider)
        ColliderComponent collider = new ColliderComponent(0, 0, 8, 8);
        collider.setLayer(ColliderComponent.CollisionLayer.NONE); // Set to NONE layer since it doesn't block movement
        potion.addComponent(collider);
        
        return potion;
    }

    /**
     * Creates a giant scaled Boss chaser enemy.
     * 
     * 💡 GRAPHICS NOTE: Software Sprite Pixel Scaling
     * Standard rendering loops draw 1-to-1 pixels. To draw a "Giant" boss without loading larger
     * image sheets, we override {@link SpriteComponent#render} to scale the sprite dynamically.
     * For every source pixel, we draw a 2x2 grid block onto the screen buffer, effectively 
     * doubling the sprite's dimensions (from 32x32 to 64x64).
     * 
     * @param xGrid       The starting tile coordinate X.
     * @param yGrid       The starting tile coordinate Y.
     * @param target      The player entity to target.
     * @param level       The active level.
     * @param chaserSheet The animations sheet.
     * @return A Boss enemy Entity.
     */
    public static Entity createBossEnemy(float xGrid, float yGrid, Entity target, Level level, SpriteSheet chaserSheet) {
        Entity boss = new Entity("Boss", "boss");
        
        // 1. Transform
        boss.addComponent(new TransformComponent(xGrid * 16.0f, yGrid * 16.0f));

        // 2. Sprite & Animations (with custom 2x scale render component)
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
                            
                            // Check pink transparency key (0xFFFF00FF)
                            if ((color & 0xFFFFFF) == 0xFFFF00FF) continue;
                            
                            // Draw a 2x2 block of pixels for every single sprite pixel (doubling size)
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

        // 3. Movement
        MovementComponent movement = new MovementComponent();
        movement.setSpeed(1.1f);
        boss.addComponent(movement);

        // 4. Collider (Large 28x28 box, centered inside the 2x scaled sprite bounds of 64x64)
        // Center calculation: (64 - 28)/2 = 18 offset
        ColliderComponent collider = new ColliderComponent(18, 18, 28, 28);
        collider.setLayer(ColliderComponent.CollisionLayer.ENEMY);
        boss.addComponent(collider);

        // 5. Health (Tanky!)
        boss.addComponent(new HealthComponent(200));

        // 6. Boss AI behavior (Chases player and shoots fireballs every 1.5 seconds)
        // 💡 JAVA TECHNIQUE: effectively final variables in inner classes
        // Inner classes reference variables from the surrounding method scope. These variables 
        // must be final or effectively final (never reassigned). To pass direction variables 
        // to the anonymous MovementComponent inside, we compute them and assign them to 
        // `final float dirX` and `final float dirY`.
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

                    // Boss chases player
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

                        // Shoot fireball projectiles at player every 1.5 seconds
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
                                // Guard variables inside inner class by making them final
                                final float dirX = pdx / len;
                                final float dirY = pdy / len;
                                
                                // Create projectile that is hostile to the player
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
