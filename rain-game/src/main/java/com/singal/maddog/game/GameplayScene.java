package com.singal.maddog.game;

import com.singal.maddog.assets.AssetManager;
import com.singal.maddog.core.Application;
import com.singal.maddog.core.Scene;
import com.singal.maddog.entity.Entity;
import com.singal.maddog.entity.components.ColliderComponent;
import com.singal.maddog.entity.components.HealthComponent;
import com.singal.maddog.entity.components.TransformComponent;
import com.singal.maddog.entity.components.SpriteComponent;
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
import com.singal.maddog.physics.AABB;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

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

    private boolean showDebug = false;
    private float shootCooldown = 0.0f;

    // Game states
    private enum GameState {
        LEVEL_START, PLAYING, GAME_OVER, VICTORY
    }
    private GameState gameState = GameState.LEVEL_START;
    private float levelStartTimer = 2.0f;
    private int currentLevel = 1;
    private int playerLives = 3;
    private int score = 0;

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

        // Setup input bindings (F3 is already bound in MainMenuScene)
        inputManager.bindAction("menu_select", java.awt.event.KeyEvent.VK_ENTER);

        // Load the first level
        loadLevel(1);
    }

    private void loadLevel(int levelNum) {
        // Remove old level from layer list
        if (level != null) {
            removeLayer(level);
        }

        TileMap tileMap;

        if (levelNum == 1) {
            // Level 1: Spawn Garden (Loads image)
            BufferedImage mapImage = assetManager.loadImage("/levels/spawn.png");
            int mapW = mapImage.getWidth();
            int mapH = mapImage.getHeight();
            int[] mapPixels = new int[mapW * mapH];
            mapImage.getRGB(0, 0, mapW, mapH, mapPixels, 0, mapW);

            tileMap = new TileMap(mapW, mapH);
            registerTiles(tileMap);

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

            // Spawn player at (19, 42)
            player = EntityFactory.createPlayer(19, 42, inputManager, level, playerSheet);
            level.addEntity(player);

            // Spawn enemies safely
            spawnChaserSafely(22, 45, tileMap);
            spawnChaserSafely(15, 40, tileMap);
            spawnChaserSafely(25, 38, tileMap);
        } 
        else if (levelNum == 2) {
            // Level 2: Forest Arena (Procedural)
            int mapW = 40;
            int mapH = 40;
            tileMap = new TileMap(mapW, mapH);
            registerTiles(tileMap);

            Random rand = new Random(2233);
            for (int y = 0; y < mapH; y++) {
                for (int x = 0; x < mapW; x++) {
                    if (x == 0 || y == 0 || x == mapW - 1 || y == mapH - 1) {
                        tileMap.setTile(x, y, 1); // Border wall
                    } else if (rand.nextFloat() < 0.12f && (x > 5 || y > 5)) {
                        // Random wall pillars (excluding player starting zone)
                        tileMap.setTile(x, y, rand.nextBoolean() ? 1 : 2);
                    } else {
                        tileMap.setTile(x, y, 0); // Grass
                    }
                }
            }

            level = new Level(tileMap);
            addLayer(level);

            // Spawn player at (3, 3)
            player = EntityFactory.createPlayer(3, 3, inputManager, level, playerSheet);
            level.addEntity(player);

            // Spawn enemies safely
            spawnChaserSafely(15, 15, tileMap);
            spawnChaserSafely(25, 10, tileMap);
            spawnChaserSafely(10, 25, tileMap);
            spawnChaserSafely(30, 30, tileMap);
            spawnChaserSafely(20, 35, tileMap);
        } 
        else if (levelNum == 3) {
            // Level 3: Maze Dungeon (Procedural)
            int mapW = 40;
            int mapH = 40;
            tileMap = new TileMap(mapW, mapH);
            registerTiles(tileMap);

            Random rand = new Random(3344);
            for (int y = 0; y < mapH; y++) {
                for (int x = 0; x < mapW; x++) {
                    if (x == 0 || y == 0 || x == mapW - 1 || y == mapH - 1) {
                        tileMap.setTile(x, y, 2); // Dungeon border
                    } else if (x < 5 && y < 5) {
                        tileMap.setTile(x, y, 3); // Guaranteed clear floor for player starting zone
                    } else {
                        // 18% chance of wall blocks creating corridors
                        if (rand.nextFloat() < 0.18f) {
                            tileMap.setTile(x, y, 2);
                        } else {
                            tileMap.setTile(x, y, 3); // Dungeon floor
                        }
                    }
                }
            }

            level = new Level(tileMap);
            addLayer(level);

            // Spawn player at (2, 2)
            player = EntityFactory.createPlayer(2, 2, inputManager, level, playerSheet);
            level.addEntity(player);

            // Spawn enemies safely
            spawnChaserSafely(12, 12, tileMap);
            spawnChaserSafely(24, 6, tileMap);
            spawnChaserSafely(8, 28, tileMap);
            spawnChaserSafely(32, 32, tileMap);
            spawnChaserSafely(18, 30, tileMap);
            spawnChaserSafely(34, 14, tileMap);
        } 
        else {
            // Level 4: Boss Castle
            int mapW = 40;
            int mapH = 40;
            tileMap = new TileMap(mapW, mapH);
            registerTiles(tileMap);

            for (int y = 0; y < mapH; y++) {
                for (int x = 0; x < mapW; x++) {
                    if (x == 0 || y == 0 || x == mapW - 1 || y == mapH - 1) {
                        tileMap.setTile(x, y, 1);
                    } else if ((x == 10 || x == 30) && (y == 10 || y == 30)) {
                        tileMap.setTile(x, y, 2); // Corner pillars
                    } else {
                        tileMap.setTile(x, y, 3); // Castle floor
                    }
                }
            }

            level = new Level(tileMap);
            addLayer(level);

            // Spawn player at (20, 32)
            player = EntityFactory.createPlayer(20, 32, inputManager, level, playerSheet);
            level.addEntity(player);

            // Spawn giant boss Cherno
            level.addEntity(EntityFactory.createBossEnemy(20, 10, player, level, enemySheet));

            // Spawn 3 guardian minions safely
            spawnChaserSafely(8, 12, tileMap);
            spawnChaserSafely(32, 12, tileMap);
            spawnChaserSafely(20, 22, tileMap);
        }

        // Lock camera position instantly on player spawn
        TransformComponent playerTransform = player.getComponent(TransformComponent.class);
        if (playerTransform != null) {
            camera.setPosition(
                playerTransform.getX() - app.getBufferWidth() / 2.0f,
                playerTransform.getY() - app.getBufferHeight() / 2.0f
            );
        }

        gameState = GameState.LEVEL_START;
        levelStartTimer = 2.0f;
    }

    private void spawnChaserSafely(int tx, int ty, TileMap tileMap) {
        for (int r = 0; r < 20; r++) {
            for (int dy = -r; dy <= r; dy++) {
                for (int dx = -r; dx <= r; dx++) {
                    int nx = tx + dx;
                    int ny = ty + dy;
                    if (nx >= 0 && nx < tileMap.getWidth() && ny >= 0 && ny < tileMap.getHeight()) {
                        if (!tileMap.getTile(nx, ny).isSolid()) {
                            level.addEntity(EntityFactory.createChaserEnemy(nx, ny, player, level, enemySheet));
                            return;
                        }
                    }
                }
            }
        }
        // Fallback
        level.addEntity(EntityFactory.createChaserEnemy(tx, ty, player, level, enemySheet));
    }

    private void registerTiles(TileMap tileMap) {
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
    }

    @Override
    public void update(double deltaTime) {
        inputManager.update();

        if (gameState == GameState.LEVEL_START) {
            levelStartTimer -= deltaTime;
            if (levelStartTimer <= 0.0f) {
                gameState = GameState.PLAYING;
            }
            return;
        }

        if (gameState == GameState.GAME_OVER || gameState == GameState.VICTORY) {
            if (inputManager.isActionPressed("menu_select")) {
                app.setScene(new MainMenuScene(app, inputManager));
            }
            return;
        }

        super.update(deltaTime);

        // Toggle debug overlay
        if (inputManager.isActionReleased("toggle_debug")) {
            showDebug = !showDebug;
        }

        // Check if player died
        if (player == null || player.isDestroyed()) {
            playerLives--;
            if (playerLives > 0) {
                // Respawn player
                float rx = (currentLevel == 1) ? 19 : (currentLevel == 4 ? 20 : 3);
                float ry = (currentLevel == 1) ? 42 : (currentLevel == 4 ? 32 : 3);
                player = EntityFactory.createPlayer(rx, ry, inputManager, level, playerSheet);
                level.addEntity(player);

                // Give 2.0s invincibility
                InvincibilityComponent inv = player.getComponent(InvincibilityComponent.class);
                if (inv != null) {
                    inv.trigger(2.0f);
                }
            } else {
                gameState = GameState.GAME_OVER;
                return;
            }
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

        // Level clear check
        checkLevelCleared();
    }

    private boolean checkTileCollision(AABB box) {
        TileMap tileMap = level.getTileMap();
        if (tileMap == null) return false;

        int minX = (int) (box.x / Tile.SIZE);
        int maxX = (int) ((box.x + box.width) / Tile.SIZE);
        int minY = (int) (box.y / Tile.SIZE);
        int maxY = (int) ((box.y + box.height) / Tile.SIZE);

        for (int y = minY; y <= maxY; y++) {
            for (int x = minX; x <= maxX; x++) {
                if (tileMap.getTile(x, y).isSolid()) {
                    return true;
                }
            }
        }
        return false;
    }

    private void resolveCollisions() {
        if (gameState != GameState.PLAYING || player == null || player.isDestroyed()) return;

        List<Entity> allEntities = level.getEntities();
        List<Entity> projectiles = new ArrayList<>();
        List<Entity> bossProjectiles = new ArrayList<>();
        List<Entity> enemies = new ArrayList<>();
        List<Entity> potions = new ArrayList<>();

        for (int i = 0; i < allEntities.size(); i++) {
            Entity e = allEntities.get(i);
            if (e.isDestroyed()) continue;
            
            String tag = e.getTag();
            if ("projectile".equals(tag)) {
                projectiles.add(e);
            } else if ("boss_projectile".equals(tag)) {
                bossProjectiles.add(e);
            } else if ("enemy".equals(tag) || "boss".equals(tag)) {
                enemies.add(e);
            } else if ("potion".equals(tag)) {
                potions.add(e);
            }
        }

        ColliderComponent playerCol = player.getComponent(ColliderComponent.class);
        InvincibilityComponent playerInv = player.getComponent(InvincibilityComponent.class);
        HealthComponent playerHealth = player.getComponent(HealthComponent.class);

        // 1. Check projectile-vs-enemy intersections
        for (Entity p : projectiles) {
            ColliderComponent pCol = p.getComponent(ColliderComponent.class);
            if (pCol == null) continue;

            // Destroy projectile if it hits a solid tile
            if (checkTileCollision(pCol.getAABB())) {
                p.destroy();
                continue;
            }

            for (Entity enemy : enemies) {
                ColliderComponent eCol = enemy.getComponent(ColliderComponent.class);
                if (eCol == null) continue;

                if (pCol.getAABB().intersects(eCol.getAABB())) {
                    HealthComponent enemyHealth = enemy.getComponent(HealthComponent.class);
                    if (enemyHealth != null) {
                        enemyHealth.takeDamage(10);
                        if (enemyHealth.isDead()) {
                            score += 100;
                            
                            // 30% chance to drop health potion reward
                            if (new Random().nextFloat() < 0.30f) {
                                TransformComponent enemyTc = enemy.getComponent(TransformComponent.class);
                                if (enemyTc != null) {
                                    level.addEntity(EntityFactory.createHealthPotion(
                                        enemyTc.getX() / 16.0f,
                                        enemyTc.getY() / 16.0f
                                    ));
                                }
                            }
                        }
                    }
                    p.destroy();
                    break;
                }
            }
        }

        // 2. Check boss projectile vs player intersections
        if (playerCol != null && playerHealth != null) {
            for (Entity bp : bossProjectiles) {
                ColliderComponent bpCol = bp.getComponent(ColliderComponent.class);
                if (bpCol == null) continue;

                if (checkTileCollision(bpCol.getAABB())) {
                    bp.destroy();
                    continue;
                }

                if (bpCol.getAABB().intersects(playerCol.getAABB())) {
                    if (playerInv == null || !playerInv.isInvincible()) {
                        playerHealth.takeDamage(15);
                        if (playerInv != null) {
                            playerInv.trigger(1.0f);
                        }
                    }
                    bp.destroy();
                }
            }
        }

        // 3. Check enemy vs player intersections (contact damage)
        if (playerCol != null && playerHealth != null && (playerInv == null || !playerInv.isInvincible())) {
            for (Entity enemy : enemies) {
                ColliderComponent eCol = enemy.getComponent(ColliderComponent.class);
                if (eCol == null) continue;

                if (playerCol.getAABB().intersects(eCol.getAABB())) {
                    playerHealth.takeDamage(15);
                    if (playerInv != null) {
                        playerInv.trigger(1.0f);
                    }
                    break; // Deal damage only once per frame
                }
            }
        }

        // 4. Check player vs potion pickups
        if (playerCol != null && playerHealth != null) {
            for (Entity potion : potions) {
                ColliderComponent potCol = potion.getComponent(ColliderComponent.class);
                if (potCol == null) continue;

                if (playerCol.getAABB().intersects(potCol.getAABB())) {
                    playerHealth.heal(25);
                    score += 50;
                    potion.destroy();
                }
            }
        }
    }

    private void checkLevelCleared() {
        if (gameState != GameState.PLAYING) return;

        List<Entity> allEntities = level.getEntities();
        int activeFoes = 0;
        for (int i = 0; i < allEntities.size(); i++) {
            Entity e = allEntities.get(i);
            if (e.isDestroyed()) continue;
            
            String tag = e.getTag();
            if ("enemy".equals(tag) || "boss".equals(tag)) {
                activeFoes++;
            }
        }

        if (activeFoes == 0) {
            if (currentLevel < 4) {
                currentLevel++;
                loadLevel(currentLevel);
            } else {
                gameState = GameState.VICTORY;
            }
        }
    }

    @Override
    public void render(Graphics2D g2d) {
        // Set camera offset on software context
        renderContext.setCameraOffset((int) camera.getX(), (int) camera.getY());
        
        // Clean screen backBuffer
        renderContext.clear(0xFF0F0F0F);

        // Blit level background tiles and entities into backBuffer
        if (level != null) {
            level.render(renderContext);
        }

        int bw = app.getBufferWidth();
        int bh = app.getBufferHeight();

        // Overlays using AWT Graphics2D on top of scaled buffer
        if (gameState == GameState.LEVEL_START) {
            g2d.setColor(new Color(0, 0, 0, 180));
            g2d.fillRect(0, 0, bw, bh);

            g2d.setColor(new Color(255, 204, 0)); // Golden yellow
            g2d.setFont(new Font("Monospaced", Font.BOLD, 18));
            g2d.drawString("LEVEL " + currentLevel, 110, 75);

            g2d.setColor(Color.WHITE);
            g2d.setFont(new Font("Monospaced", Font.PLAIN, 9));
            String name = (currentLevel == 1) ? "Spawn Garden" : 
                          (currentLevel == 2) ? "Forest Arena" : 
                          (currentLevel == 3) ? "Maze Dungeon" : "Boss Castle";
            g2d.drawString(name, 150 - (g2d.getFontMetrics().stringWidth(name) / 2), 95);
        }
        else if (gameState == GameState.PLAYING) {
            // Draw HP, Lives, Level, Score HUD elements at bottom of screen
            if (player != null && !player.isDestroyed()) {
                HealthComponent playerHealth = player.getComponent(HealthComponent.class);
                if (playerHealth != null) {
                    renderPlayerHpBar(g2d, playerHealth);
                }
            }
        }
        else if (gameState == GameState.GAME_OVER) {
            g2d.setColor(new Color(20, 0, 0, 220));
            g2d.fillRect(0, 0, bw, bh);

            g2d.setColor(Color.RED);
            g2d.setFont(new Font("Monospaced", Font.BOLD, 22));
            g2d.drawString("GAME OVER", 95, 65);

            g2d.setColor(Color.WHITE);
            g2d.setFont(new Font("Monospaced", Font.PLAIN, 10));
            g2d.drawString("Final Score: " + score, 105, 90);

            g2d.setColor(new Color(150, 150, 150));
            g2d.drawString("[ Press ENTER to Restart ]", 70, 120);
        }
        else if (gameState == GameState.VICTORY) {
            g2d.setColor(new Color(0, 20, 10, 220));
            g2d.fillRect(0, 0, bw, bh);

            g2d.setColor(Color.GREEN);
            g2d.setFont(new Font("Monospaced", Font.BOLD, 22));
            g2d.drawString("VICTORY!", 100, 50);

            g2d.setColor(Color.WHITE);
            g2d.setFont(new Font("Monospaced", Font.PLAIN, 9));
            g2d.drawString("You Defeated the Boss Cherno!", 70, 75);
            g2d.drawString("Final Score: " + score, 105, 95);

            g2d.setColor(new Color(180, 255, 180));
            g2d.drawString("[ Press ENTER to return to Menu ]", 60, 125);
        }

        // Diagnostic overlay
        if (showDebug) {
            debugOverlay.render(g2d, app, level, camera.getX(), camera.getY());
        }
    }

    private void renderPlayerHpBar(Graphics2D g2d, HealthComponent health) {
        int barW = 60;
        int barH = 6;
        int x = 5;
        int y = 156; // Positioned near bottom left

        // Background shadow (dark red)
        g2d.setColor(new Color(0x3a, 0x05, 0x05));
        g2d.fillRect(x, y, barW, barH);

        // Foreground filled progress (cyan/green)
        g2d.setColor(new Color(0x00, 0xff, 0x66));
        int filledW = (int) (barW * health.getHealthPercentage());
        g2d.fillRect(x, y, filledW, barH);

        // Border outline
        g2d.setColor(Color.WHITE);
        g2d.drawRect(x, y, barW, barH);

        // Text HUD elements
        g2d.setFont(new Font("Monospaced", Font.PLAIN, 8));

        // Draw Lives hearts
        g2d.setColor(new Color(0xff, 0x33, 0x66));
        String hearts = "";
        for (int i = 0; i < playerLives; i++) {
            hearts += "♥";
        }
        g2d.drawString("LIVES: " + hearts, 75, 162);

        // Draw current Level
        g2d.setColor(new Color(0x00, 0xee, 0xff));
        g2d.drawString("LVL: " + currentLevel + "/4", 145, 162);

        // Draw Score
        g2d.setColor(new Color(0xff, 0xcc, 0x00));
        g2d.drawString("SCORE: " + score, 210, 162);
    }
}
