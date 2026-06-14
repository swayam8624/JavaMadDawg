package com.singal.maddog.game;

import com.singal.maddog.assets.AssetManager;
import com.singal.maddog.core.Application;
import com.singal.maddog.core.Scene;
import com.singal.maddog.entity.Entity;
import com.singal.maddog.entity.components.ColliderComponent;
import com.singal.maddog.entity.components.HealthComponent;
import com.singal.maddog.entity.components.TransformComponent;
import com.singal.maddog.input.InputManager;
import com.singal.maddog.renderer.Camera2D;
import com.singal.maddog.renderer.SoftwareRenderContext;
import com.singal.maddog.renderer.Sprite;
import com.singal.maddog.renderer.SpriteSheet;
import com.singal.maddog.renderer.Texture;
import com.singal.maddog.tools.DebugOverlay;
import com.singal.maddog.world.Level;
import com.singal.maddog.world.Tile;
import com.singal.maddog.world.TileMap;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

/**
 * Main gameplay scene managing level updates, collisions, camera tracking, and rendering.
 */
public class GameplayScene extends Scene {
    private final Application app;
    private final InputManager inputManager;
    private final AssetManager assetManager;
    
    private Level level;
    private Entity player;
    private Camera2D camera;
    private SoftwareRenderContext renderContext;
    private DebugOverlay debugOverlay;

    private boolean showDebug = true;
    private float shootCooldown = 0.0f;

    // Sprite resources
    private SpriteSheet spawnLevelSheet;
    private SpriteSheet playerSheet;
    private SpriteSheet enemySheet;
    private Sprite wizardProjectileSprite;

    public GameplayScene(Application app, InputManager inputManager) {
        this.app = app;
        this.inputManager = inputManager;
        this.assetManager = new AssetManager();
    }

    @Override
    protected void onInit() {
        // Initialize renderer
        renderContext = new SoftwareRenderContext(app.getBufferWidth(), app.getBufferHeight(), app.getPixelBuffer());
        debugOverlay = new DebugOverlay();
        camera = new Camera2D();
        camera.setLerpSpeed(0.1f); // Smooth camera

        // Load asset sheets
        Texture spawnLvlTex = new Texture(assetManager.loadImage("/textures/sheets/spawn_lvl.png"));
        spawnLevelSheet = new SpriteSheet(spawnLvlTex, 16);

        Texture playerTex = new Texture(assetManager.loadImage("/textures/sheets/player_sheet.png"));
        playerSheet = new SpriteSheet(playerTex, 32);

        Texture enemyTex = new Texture(assetManager.loadImage("/textures/sheets/king_cherno.png"));
        enemySheet = new SpriteSheet(enemyTex, 32);

        Texture projTex = new Texture(assetManager.loadImage("/textures/sheets/projectiles/wizard.png"));
        wizardProjectileSprite = new SpriteSheet(projTex, 16).getSprite(0, 0);

        // Load level map
        BufferedImage mapImage = assetManager.loadImage("/levels/spawn.png");
        int mapW = mapImage.getWidth();
        int mapH = mapImage.getHeight();
        int[] mapPixels = new int[mapW * mapH];
        mapImage.getRGB(0, 0, mapW, mapH, mapPixels, 0, mapW);

        // Configure TileMap templates
        TileMap tileMap = new TileMap(mapW, mapH);
        Tile grassTile = new Tile(0, spawnLevelSheet.getSprite(0, 0), false);
        Tile wall1Tile = new Tile(1, spawnLevelSheet.getSprite(0, 1), true);
        Tile wall2Tile = new Tile(2, spawnLevelSheet.getSprite(0, 2), true);
        Tile floorTile = new Tile(3, spawnLevelSheet.getSprite(1, 1), false);
        Tile voidTile = new Tile(-1, new Sprite(16, 0xFF000000), true);

        tileMap.registerTileTemplate(grassTile);
        tileMap.registerTileTemplate(wall1Tile);
        tileMap.registerTileTemplate(wall2Tile);
        tileMap.registerTileTemplate(floorTile);
        tileMap.setVoidTile(voidTile);

        // Parse colors to build the map
        for (int y = 0; y < mapH; y++) {
            for (int x = 0; x < mapW; x++) {
                int col = mapPixels[x + y * mapW] & 0xFFFFFF;
                if (col == 0x00FF00) {
                    tileMap.setTile(x, y, 0); // Grass
                } else if (col == 0x808080) {
                    tileMap.setTile(x, y, 1); // Wall 1
                } else if (col == 0x303030) {
                    tileMap.setTile(x, y, 2); // Wall 2
                } else if (col == 0x724715) {
                    tileMap.setTile(x, y, 3); // Floor
                } else {
                    tileMap.setTile(x, y, 0); // Default to grass
                }
            }
        }

        level = new Level(tileMap);
        addLayer(level);

        // Spawn player at grid coordinate (19, 42)
        player = EntityFactory.createPlayer(19, 42, inputManager, level, playerSheet);
        level.addEntity(player);

        // Spawn a couple of chasers around player
        level.addEntity(EntityFactory.createChaserEnemy(22, 45, player, level, enemySheet));
        level.addEntity(EntityFactory.createChaserEnemy(15, 40, player, level, enemySheet));
        level.addEntity(EntityFactory.createChaserEnemy(25, 38, player, level, enemySheet));

        // Lock camera instantly on spawn
        TransformComponent playerTransform = player.getComponent(TransformComponent.class);
        if (playerTransform != null) {
            camera.setPosition(
                playerTransform.getX() - app.getBufferWidth() / 2.0f,
                playerTransform.getY() - app.getBufferHeight() / 2.0f
            );
        }
    }

    @Override
    public void update(double deltaTime) {
        inputManager.update();
        super.update(deltaTime);

        // Toggle debug overlay
        if (inputManager.isActionReleased("toggle_debug")) {
            showDebug = !showDebug;
        }

        // Camera follow
        TransformComponent playerTransform = player.getComponent(TransformComponent.class);
        if (playerTransform != null) {
            float targetCamX = playerTransform.getX() - app.getBufferWidth() / 2.0f;
            float targetCamY = playerTransform.getY() - app.getBufferHeight() / 2.0f;
            camera.setTarget(targetCamX, targetCamY);
        }
        camera.update(deltaTime);

        // Handle shooting projectiles
        if (shootCooldown > 0.0f) {
            shootCooldown -= deltaTime;
        }

        if (inputManager.isActionHeld("shoot") && shootCooldown <= 0.0f && playerTransform != null) {
            // Find direction from player to mouse position
            // Mouse position is in window scale, needs division to buffer scale
            float mouseBufferX = inputManager.getMousePosition().x / app.getRenderScaleX() + camera.getX();
            float mouseBufferY = inputManager.getMousePosition().y / app.getRenderScaleY() + camera.getY();

            float px = playerTransform.getX() + 16.0f; // Center offset (32x32 player sprite)
            float py = playerTransform.getY() + 16.0f;

            float dx = mouseBufferX - px;
            float dy = mouseBufferY - py;
            float length = (float) Math.sqrt(dx * dx + dy * dy);

            if (length > 0) {
                dx /= length;
                dy /= length;
                Entity p = EntityFactory.createWizardProjectile(px, py, dx, dy, level, wizardProjectileSprite);
                level.addEntity(p);
                shootCooldown = 0.3f; // 300ms fire rate
            }
        }

        resolveCollisions();
    }

    private void resolveCollisions() {
        List<Entity> allEntities = level.getEntities();
        List<Entity> projectiles = new ArrayList<>();
        List<Entity> enemies = new ArrayList<>();

        for (int i = 0; i < allEntities.size(); i++) {
            Entity e = allEntities.get(i);
            if (e.isDestroyed()) continue;
            if ("projectile".equals(e.getTag())) projectiles.add(e);
            else if ("enemy".equals(e.getTag())) enemies.add(e);
        }

        // Check projectile-vs-enemy intersections
        for (Entity p : projectiles) {
            ColliderComponent pCol = p.getComponent(ColliderComponent.class);
            if (pCol == null) continue;

            for (Entity enemy : enemies) {
                ColliderComponent eCol = enemy.getComponent(ColliderComponent.class);
                if (eCol == null) continue;

                if (pCol.getAABB().intersects(eCol.getAABB())) {
                    // Deal damage and destroy projectile
                    HealthComponent enemyHealth = enemy.getComponent(HealthComponent.class);
                    if (enemyHealth != null) {
                        enemyHealth.takeDamage(10);
                    }
                    p.destroy();
                    break;
                }
            }
        }
    }

    @Override
    public void render(Graphics2D g2d) {
        // Set camera offset on the software context
        renderContext.setCameraOffset((int) camera.getX(), (int) camera.getY());
        
        // Clean screen backBuffer
        renderContext.clear(0xFF0F0F0F);

        // Blit level background tiles and entities into backBuffer
        level.render(renderContext);

        // Renders HUD overlays using AWT Graphics2D on top of the scaled canvas
        if (player != null && !player.isDestroyed()) {
            HealthComponent playerHealth = player.getComponent(HealthComponent.class);
            if (playerHealth != null) {
                renderPlayerHpBar(g2d, playerHealth);
            }
        }

        // Diagnostic overlay
        if (showDebug) {
            debugOverlay.render(g2d, app, level, camera.getX(), camera.getY());
        }
    }

    private void renderPlayerHpBar(Graphics2D g2d, HealthComponent health) {
        int barW = 200;
        int barH = 20;
        int x = 10;
        int y = app.getBufferHeight() * app.getScale() - 30;

        // Background shadow
        g2d.setColor(Color.RED);
        g2d.fillRect(x, y, barW, barH);

        // Foreground filled progress
        g2d.setColor(Color.GREEN);
        int filledW = (int) (barW * health.getHealthPercentage());
        g2d.fillRect(x, y, filledW, barH);

        // Border
        g2d.setColor(Color.WHITE);
        g2d.drawRect(x, y, barW, barH);
        g2d.drawString("HP: " + health.getCurrentHealth() + "/" + health.getMaxHealth(), x + 5, y + 15);
    }
}
