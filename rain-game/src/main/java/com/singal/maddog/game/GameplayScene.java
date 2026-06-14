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
 * 🎓 GameplayScene is the main orchestrator of the MadDawg action game.
 * It manages the game loop lifecycle hooks (init, update, render), level construction,
 * camera tracking, AABB-based tile/entity collision resolution, and GUI/HUD rendering.
 * 
 * 💡 GAME PROGRAMMING TECHNIQUE: Scene-Based Architecture
 * Instead of stuffing all game states (menus, settings, play, game over) into a single 
 * massive class or file, we divide them into distinct "Scenes". The main Application container
 * holds a reference to the active Scene and calls its init, update, and render hooks. 
 * This keeps concerns isolated, code reusable, and game states clean.
 */
public class GameplayScene extends Scene {
    // 💡 JAVA KEYWORD: final
    // Marking variables as 'final' ensures their references cannot be changed after assignment.
    // This provides thread-safety and compiler optimizations.
    private final Application app;
    private final InputManager inputManager;
    private final AssetManager assetManager;
    
    // Core game components
    private Level level;
    private Entity player;
    private Camera2D camera;
    private SoftwareRenderContext renderContext;
    private DebugOverlay debugOverlay;

    private boolean showDebug = false;
    private float shootCooldown = 0.0f;

    /**
     * 🎓 State Machine Pattern using Java Enumerations
     * 
     * 💡 JAVA TECHNIQUE: Enumerations (enums)
     * An enum is a special Java type used to define collections of constants.
     * Unlike integer constants (e.g., public static final int STATE_PLAYING = 1),
     * enums are strongly type-safe. They prevent developers from passing invalid integer values
     * (like 99) to state setters and provide built-in support for debugging (via name() printing).
     */
    private enum GameState {
        /** Level intro screen showing name and objective. */
        LEVEL_START,
        /** Dynamic gameplay mode where actors update and collide. */
        PLAYING,
        /** Screen overlay shown when lives hit zero. */
        GAME_OVER,
        /** Screen overlay shown when all levels are completed. */
        VICTORY
    }
    
    // Default initial state
    private GameState gameState = GameState.LEVEL_START;
    private float levelStartTimer = 2.0f;
    private int currentLevel = 1;
    private int playerLives = 3;
    private int score = 0;

    // Sprite resources preloaded and cached to conserve memory
    private SpriteSheet spawnLevelSheet;
    private SpriteSheet playerSheet;
    private SpriteSheet enemySheet;
    private Sprite wizardProjectileSprite;

    /**
     * Constructs the GameplayScene.
     * 
     * @param app The main application window containing scaling configurations and size specifications.
     * @param inputManager The central hardware input interceptor capturing keyboard and mouse actions.
     */
    public GameplayScene(Application app, InputManager inputManager) {
        this.app = app;
        this.inputManager = inputManager;
        this.assetManager = new AssetManager();
    }

    /**
     * Initializes the gameplay components, loads sheet assets, registers inputs, and triggers Level 1.
     * Overrides the Scene lifecycle hook 'onInit'.
     */
    @Override
    protected void onInit() {
        // Initialize renderer
        // 💡 RENDER SYSTEM: Software Backbuffering
        // We write pixels to a low-resolution pixel buffer (300x168) for retro pixel-art consistency,
        // then scale it up when copying (blitting) it to the native operating system JFrame.
        renderContext = new SoftwareRenderContext(app.getBufferWidth(), app.getBufferHeight(), app.getPixelBuffer());
        debugOverlay = new DebugOverlay();
        camera = new Camera2D();
        
        // 💡 CAMERA TECHNIQUE: Linear Interpolation (Lerp)
        // Lerping creates smooth camera movement. Instead of snapping the camera position instantly,
        // we interpolate it by a fraction (0.1f) towards the player's position each frame:
        // camera.x += (target.x - camera.x) * lerpSpeed.
        camera.setLerpSpeed(0.1f); 

        // Load asset sheets
        Texture spawnLvlTex = new Texture(assetManager.loadImage("/textures/sheets/spawn_lvl.png"));
        spawnLevelSheet = new SpriteSheet(spawnLvlTex, 16);

        Texture playerTex = new Texture(assetManager.loadImage("/textures/sheets/player_sheet.png"));
        playerSheet = new SpriteSheet(playerTex, 32);

        Texture enemyTex = new Texture(assetManager.loadImage("/textures/sheets/king_cherno.png"));
        enemySheet = new SpriteSheet(enemyTex, 32);

        Texture projTex = new Texture(assetManager.loadImage("/textures/sheets/projectiles/wizard.png"));
        wizardProjectileSprite = new SpriteSheet(projTex, 16).getSprite(0, 0);

        // Setup input bindings for UI navigation
        inputManager.bindAction("menu_select", java.awt.event.KeyEvent.VK_ENTER);

        // Load the first level
        loadLevel(1);
    }

    /**
     * Builds and initializes a specific level. Clears entities from previous level,
     * and sets up the map (loaded from image or procedurally generated).
     * 
     * @param levelNum The integer identifier of the level to construct (1-4).
     */
    private void loadLevel(int levelNum) {
        // Clean up previous level to prevent memory leaks and dangling entity references
        if (level != null) {
            removeLayer(level);
        }

        TileMap tileMap;

        if (levelNum == 1) {
            // --- LEVEL 1: SPAWN GARDEN ---
            // 💡 TECH DETAIL: Image-Based Level Loading
            // We read a PNG image file where each pixel color represents a specific tile type
            // (e.g. green pixels for grass, gray pixels for walls).
            BufferedImage mapImage = assetManager.loadImage("/levels/spawn.png");
            int mapW = mapImage.getWidth();
            int mapH = mapImage.getHeight();
            int[] mapPixels = new int[mapW * mapH];
            
            // Extract pixel colors in bulk using getRGB to minimize JNI bridge overhead
            mapImage.getRGB(0, 0, mapW, mapH, mapPixels, 0, mapW);

            tileMap = new TileMap(mapW, mapH);
            registerTiles(tileMap);

            for (int y = 0; y < mapH; y++) {
                for (int x = 0; x < mapW; x++) {
                    // 💡 JAVA TECHNIQUE: Bitwise Masking
                    // BufferedImage getRGB returns colors as ARGB (Alpha-Red-Green-Blue).
                    // We mask the value with 0xFFFFFF to strip away the Alpha channel,
                    // leaving us with a clean Hex RGB color representation (e.g. 0x00FF00).
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
                        tileMap.setTile(x, y, 0); // Default fallback
                    }
                }
            }

            level = new Level(tileMap);
            addLayer(level);

            // Spawn player at coordinates (19, 42)
            player = EntityFactory.createPlayer(19, 42, inputManager, level, playerSheet);
            level.addEntity(player);

            // Spawn chaser enemies safely to prevent them from initializing inside solid walls
            spawnChaserSafely(22, 45, tileMap);
            spawnChaserSafely(15, 40, tileMap);
            spawnChaserSafely(25, 38, tileMap);
        } 
        else if (levelNum == 2) {
            // --- LEVEL 2: FOREST ARENA ---
            // 💡 TECH DETAIL: Procedural Generation (PRNG)
            // Procedural generation uses mathematical algorithms to build levels dynamically.
            // We use a fixed Seed (2233) in the Random constructor.
            // This ensures that the random sequence generates exactly the same level on every play,
            // giving us deterministic generation without saving level layouts.
            int mapW = 40;
            int mapH = 40;
            tileMap = new TileMap(mapW, mapH);
            registerTiles(tileMap);

            Random rand = new Random(2233);
            for (int y = 0; y < mapH; y++) {
                for (int x = 0; x < mapW; x++) {
                    // Place solid walls along the perimeter of the 2D grid
                    if (x == 0 || y == 0 || x == mapW - 1 || y == mapH - 1) {
                        tileMap.setTile(x, y, 1); 
                    } else if (rand.nextFloat() < 0.12f && (x > 5 || y > 5)) {
                        // 12% probability of placing tree trunks/pillars outside player spawn
                        tileMap.setTile(x, y, rand.nextBoolean() ? 1 : 2);
                    } else {
                        tileMap.setTile(x, y, 0); // Open grass walk areas
                    }
                }
            }

            level = new Level(tileMap);
            addLayer(level);

            // Spawn player in the safe top-left corner
            player = EntityFactory.createPlayer(3, 3, inputManager, level, playerSheet);
            level.addEntity(player);

            // Spawn enemies across the forest
            spawnChaserSafely(15, 15, tileMap);
            spawnChaserSafely(25, 10, tileMap);
            spawnChaserSafely(10, 25, tileMap);
            spawnChaserSafely(30, 30, tileMap);
            spawnChaserSafely(20, 35, tileMap);
        } 
        else if (levelNum == 3) {
            // --- LEVEL 3: MAZE DUNGEON ---
            // Creates procedural corridor networks with a clear starting zone.
            int mapW = 40;
            int mapH = 40;
            tileMap = new TileMap(mapW, mapH);
            registerTiles(tileMap);

            Random rand = new Random(3344);
            for (int y = 0; y < mapH; y++) {
                for (int x = 0; x < mapW; x++) {
                    if (x == 0 || y == 0 || x == mapW - 1 || y == mapH - 1) {
                        tileMap.setTile(x, y, 2); // Castle wall borders
                    } else if (x < 5 && y < 5) {
                        // Guaranteed empty tiles for starting area so player doesn't spawn inside a wall
                        tileMap.setTile(x, y, 3); 
                    } else {
                        // 18% wall density creates corridor maze shapes
                        if (rand.nextFloat() < 0.18f) {
                            tileMap.setTile(x, y, 2);
                        } else {
                            tileMap.setTile(x, y, 3); // Dungeon stone floor
                        }
                    }
                }
            }

            level = new Level(tileMap);
            addLayer(level);

            // Spawn player at (2, 2)
            player = EntityFactory.createPlayer(2, 2, inputManager, level, playerSheet);
            level.addEntity(player);

            // Spawns enemies safely
            spawnChaserSafely(12, 12, tileMap);
            spawnChaserSafely(24, 6, tileMap);
            spawnChaserSafely(8, 28, tileMap);
            spawnChaserSafely(32, 32, tileMap);
            spawnChaserSafely(18, 30, tileMap);
            spawnChaserSafely(34, 14, tileMap);
        } 
        else {
            // --- LEVEL 4: BOSS CASTLE ---
            // Large boss fight chamber with defensive wall columns in corners.
            int mapW = 40;
            int mapH = 40;
            tileMap = new TileMap(mapW, mapH);
            registerTiles(tileMap);

            for (int y = 0; y < mapH; y++) {
                for (int x = 0; x < mapW; x++) {
                    if (x == 0 || y == 0 || x == mapW - 1 || y == mapH - 1) {
                        tileMap.setTile(x, y, 1);
                    } else if ((x == 10 || x == 30) && (y == 10 || y == 30)) {
                        tileMap.setTile(x, y, 2); // Defensive corner pillars
                    } else {
                        tileMap.setTile(x, y, 3); // Smooth floor
                    }
                }
            }

            level = new Level(tileMap);
            addLayer(level);

            // Spawn player near bottom center
            player = EntityFactory.createPlayer(20, 32, inputManager, level, playerSheet);
            level.addEntity(player);

            // Spawn the giant Boss enemy near top center
            level.addEntity(EntityFactory.createBossEnemy(20, 10, player, level, enemySheet));

            // Spawn guards surrounding the chamber
            spawnChaserSafely(8, 12, tileMap);
            spawnChaserSafely(32, 12, tileMap);
            spawnChaserSafely(20, 22, tileMap);
        }

        // Instantly synchronize the camera over the player spawn coordinates
        TransformComponent playerTransform = player.getComponent(TransformComponent.class);
        if (playerTransform != null) {
            camera.setPosition(
                playerTransform.getX() - app.getBufferWidth() / 2.0f,
                playerTransform.getY() - app.getBufferHeight() / 2.0f
            );
        }

        // Initialize state variables for level entry intro countdown
        gameState = GameState.LEVEL_START;
        levelStartTimer = 2.0f;
    }

    /**
     * 🎓 Concentric Grid Search Algorithm
     * Spawns an enemy at the closest available non-solid tile coordinates,
     * searching outwards in concentric square layers to prevent spawning actors inside solid walls.
     * 
     * 💡 ALGORITHM DESIGN: Concentric Expand
     * Starting with radius `r = 0`, we examine tiles in a grid around the target tile `(tx, ty)`.
     * `dy` and `dx` range from `-r` to `r`. If a tile is within bounds and is NOT solid, we spawn
     * the enemy there and exit. If all tiles at radius `r` are solid, we increment the radius.
     * 
     * @param tx The target tile X index.
     * @param ty The target tile Y index.
     * @param tileMap The TileMap template grid.
     */
    private void spawnChaserSafely(int tx, int ty, TileMap tileMap) {
        for (int r = 0; r < 20; r++) {
            for (int dy = -r; dy <= r; dy++) {
                for (int dx = -r; dx <= r; dx++) {
                    int nx = tx + dx;
                    int ny = ty + dy;
                    
                    // 💡 JAVA SAFETY: Array Bounds Verification
                    // Always verify array index boundaries (nx >= 0 && nx < width) before querying
                    // elements to prevent throwing ArrayIndexOutOfBoundsException.
                    if (nx >= 0 && nx < tileMap.getWidth() && ny >= 0 && ny < tileMap.getHeight()) {
                        if (!tileMap.getTile(nx, ny).isSolid()) {
                            level.addEntity(EntityFactory.createChaserEnemy(nx, ny, player, level, enemySheet));
                            return; // Target found and spawned, break out
                        }
                    }
                }
            }
        }
        // Fallback: spawn at original position if no empty tile is found within a radius of 20
        level.addEntity(EntityFactory.createChaserEnemy(tx, ty, player, level, enemySheet));
    }

    /**
     * 🎓 Flyweight Registry Pattern for Tiles
     * Maps tile identification IDs (0-3) to shared immutable graphical templates
     * to avoid allocating individual Tile objects for every grid cell, which reduces GC pressure.
     * 
     * @param tileMap The TileMap registry container.
     */
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

    /**
     * 🎓 Game Loop Variable Timestep (Delta Time)
     * Handles game logic updates. Overrides Scene lifecycle 'update' method.
     * 
     * 💡 PHYSICS & ANIMATION TECHNIQUE: Delta Time
     * `deltaTime` is the exact fractional second elapsed since the last frame (e.g. 0.016s for 60FPS).
     * Multiplying velocities and cooldowns by `deltaTime` (e.g., movement * deltaTime) makes the game
     * speed independent of rendering performance. The game moves at the same speed on weak hardware
     * and high-refresh-rate gaming screens.
     * 
     * @param deltaTime Elapsed fractional seconds since the previous update frame.
     */
    @Override
    public void update(double deltaTime) {
        inputManager.update();

        // Count down the level title display timer
        if (gameState == GameState.LEVEL_START) {
            levelStartTimer -= deltaTime;
            if (levelStartTimer <= 0.0f) {
                gameState = GameState.PLAYING;
            }
            return; // Pause game updates during level intro
        }

        // Hold loop in end screens, listening for selection button triggers
        if (gameState == GameState.GAME_OVER || gameState == GameState.VICTORY) {
            if (inputManager.isActionPressed("menu_select")) {
                app.setScene(new MainMenuScene(app, inputManager));
            }
            return;
        }

        // Run Entity lifecycle updates (movement, physics, components)
        super.update(deltaTime);

        // Toggle diagnostic overlay display
        if (inputManager.isActionReleased("toggle_debug")) {
            showDebug = !showDebug;
        }

        // Check player status and handle lives and respawning
        if (player == null || player.isDestroyed()) {
            playerLives--;
            if (playerLives > 0) {
                // Respawn at specific level start coordinate points
                float rx = (currentLevel == 1) ? 19 : (currentLevel == 4 ? 20 : 3);
                float ry = (currentLevel == 1) ? 42 : (currentLevel == 4 ? 32 : 3);
                player = EntityFactory.createPlayer(rx, ry, inputManager, level, playerSheet);
                level.addEntity(player);

                // 💡 GAMEPLAY SYSTEM: Invincibility frames
                // Give player temporary damage immunity (2.0 seconds) on spawn to prevent spawn-killing.
                InvincibilityComponent inv = player.getComponent(InvincibilityComponent.class);
                if (inv != null) {
                    inv.trigger(2.0f);
                }
            } else {
                gameState = GameState.GAME_OVER;
                return;
            }
        }

        // Smoothly pan camera target to center on player
        TransformComponent playerTransform = player.getComponent(TransformComponent.class);
        if (playerTransform != null) {
            float targetCamX = playerTransform.getX() - app.getBufferWidth() / 2.0f;
            float targetCamY = playerTransform.getY() - app.getBufferHeight() / 2.0f;
            camera.setTarget(targetCamX, targetCamY);
        }
        camera.update(deltaTime);

        // Track weapon fire rates
        if (shootCooldown > 0.0f) {
            shootCooldown -= deltaTime;
        }

        // Handle firing magic projectiles
        if (inputManager.isActionHeld("shoot") && shootCooldown <= 0.0f && playerTransform != null) {
            // 💡 COORDINATE MATH: Canvas Scale Mapping
            // Since the software buffer is scaled up, mouse coordinates received from AWT events
            // are in window space. We map them back to game buffer coordinates by dividing by the 
            // scale factor, and add the camera offset coordinates to translate them to world coordinates.
            float mouseBufferX = inputManager.getMousePosition().x / app.getRenderScaleX() + camera.getX();
            float mouseBufferY = inputManager.getMousePosition().y / app.getRenderScaleY() + camera.getY();

            // Offset starting coordinate point to player sprite center (32x32 sprite, 16x16 center)
            float px = playerTransform.getX() + 16.0f; 
            float py = playerTransform.getY() + 16.0f;

            // Calculate directional vector pointing to target
            float dx = mouseBufferX - px;
            float dy = mouseBufferY - py;
            
            // 💡 VECTOR MATH: Normalization
            // Normalizing a vector scales its length to 1.0 while preserving direction.
            // We calculate length using Pythagoras' theorem: sqrt(x² + y²),
            // then divide both elements by the length.
            float length = (float) Math.sqrt(dx * dx + dy * dy);

            if (length > 0) {
                dx /= length;
                dy /= length;
                Entity p = EntityFactory.createWizardProjectile(px, py, dx, dy, level, wizardProjectileSprite);
                level.addEntity(p);
                shootCooldown = 0.3f; // 300ms fire rate cooldown
            }
        }

        // Perform AABB intersection tests and collision resolutions
        resolveCollisions();

        // Progress level if all hostile enemies are defeated
        checkLevelCleared();
    }

    /**
     * 🎓 Broad-Phase Tile Collision Check
     * Checks if the given AABB overlaps any solid tile.
     * 
     * 💡 COLLISION OPTIMIZATION: Grid Filtering
     * Instead of checking collision against all tiles on the map, we convert the AABB's float coordinates
     * to tile indices by dividing by Tile.SIZE (16). We then check only the tiles that overlap the bounding box.
     * This reduces the collision checks from O(N*M) to a constant O(1) time complexity.
     * 
     * @param box The Axis-Aligned Bounding Box (AABB) to test.
     * @return True if overlapping a solid tile.
     */
    private boolean checkTileCollision(AABB box) {
        TileMap tileMap = level.getTileMap();
        if (tileMap == null) return false;

        // Convert pixel coordinates to grid coordinate space
        int minX = (int) (box.x / Tile.SIZE);
        int maxX = (int) ((box.x + box.width) / Tile.SIZE);
        int minY = (int) (box.y / Tile.SIZE);
        int maxY = (int) ((box.y + box.height) / Tile.SIZE);

        // Check only overlapping grid cells
        for (int y = minY; y <= maxY; y++) {
            for (int x = minX; x <= maxX; x++) {
                if (tileMap.getTile(x, y).isSolid()) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * 🎓 Collision Resolution Loop
     * Resolves collisions between projectiles, enemies, and player actors.
     * 
     * 💡 JAVA COLLECTIONS TECHNIQUE: List Allocation & Filtering
     * We filter our entities into sub-lists (projectiles, enemies, potions) to avoid testing all entity pairs.
     * When removing dead entities, we modify state flags (`isDestroyed()`) and clean up lists outside the loop
     * to avoid throwing `ConcurrentModificationException` (which happens if you modify a collection while iterating it).
     */
    private void resolveCollisions() {
        if (gameState != GameState.PLAYING || player == null || player.isDestroyed()) return;

        List<Entity> allEntities = level.getEntities();
        List<Entity> projectiles = new ArrayList<>();
        List<Entity> bossProjectiles = new ArrayList<>();
        List<Entity> enemies = new ArrayList<>();
        List<Entity> potions = new ArrayList<>();

        // Group entities by category tag
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

        // --- 1. Projectiles vs Enemies Collision ---
        for (Entity p : projectiles) {
            ColliderComponent pCol = p.getComponent(ColliderComponent.class);
            if (pCol == null) continue;

            // Destroy the projectile if it hits a solid wall tile
            if (checkTileCollision(pCol.getAABB())) {
                p.destroy();
                continue;
            }

            for (Entity enemy : enemies) {
                ColliderComponent eCol = enemy.getComponent(ColliderComponent.class);
                if (eCol == null) continue;

                // 💡 PHYSICS MATH: AABB Intersection Test
                // Two Axis-Aligned Bounding Boxes (A and B) overlap if:
                // A.x < B.x + B.w && A.x + A.w > B.x && A.y < B.y + B.h && A.y + A.h > B.y.
                if (pCol.getAABB().intersects(eCol.getAABB())) {
                    HealthComponent enemyHealth = enemy.getComponent(HealthComponent.class);
                    if (enemyHealth != null) {
                        enemyHealth.takeDamage(10);
                        if (enemyHealth.isDead()) {
                            score += 100;
                            
                            // 💡 PROBABILITY DESIGN: Reward Drops
                            // Defeated enemies have a 30% chance to drop a health potion.
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
                    p.destroy(); // Destroy projectile on hit
                    break; // Stop checking other enemies for this projectile
                }
            }
        }

        // --- 2. Boss Fireballs vs Player Collision ---
        if (playerCol != null && playerHealth != null) {
            for (Entity bp : bossProjectiles) {
                ColliderComponent bpCol = bp.getComponent(ColliderComponent.class);
                if (bpCol == null) continue;

                if (checkTileCollision(bpCol.getAABB())) {
                    bp.destroy();
                    continue;
                }

                if (bpCol.getAABB().intersects(playerCol.getAABB())) {
                    // Deal damage only if player does not have active invincibility frames
                    if (playerInv == null || !playerInv.isInvincible()) {
                        playerHealth.takeDamage(15);
                        if (playerInv != null) {
                            playerInv.trigger(1.0f); // 1.0s invincibility frames
                        }
                    }
                    bp.destroy();
                }
            }
        }

        // --- 3. Enemies vs Player Contact Damage ---
        if (playerCol != null && playerHealth != null && (playerInv == null || !playerInv.isInvincible())) {
            for (Entity enemy : enemies) {
                ColliderComponent eCol = enemy.getComponent(ColliderComponent.class);
                if (eCol == null) continue;

                if (playerCol.getAABB().intersects(eCol.getAABB())) {
                    playerHealth.takeDamage(15);
                    if (playerInv != null) {
                        playerInv.trigger(1.0f); // Apply damage invincibility
                    }
                    break; // Deal damage only once per frame
                }
            }
        }

        // --- 4. Potions vs Player Pickup Collision ---
        if (playerCol != null && playerHealth != null) {
            for (Entity potion : potions) {
                ColliderComponent potCol = potion.getComponent(ColliderComponent.class);
                if (potCol == null) continue;

                if (playerCol.getAABB().intersects(potCol.getAABB())) {
                    playerHealth.heal(25);
                    score += 50;
                    potion.destroy(); // Destroy potion pickup
                }
            }
        }
    }

    /**
     * Checks if all enemies on the current level are defeated.
     * Progresses to the next level, or transitions to the VICTORY state.
     */
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

        // If no active enemies remain, progress
        if (activeFoes == 0) {
            if (currentLevel < 4) {
                currentLevel++;
                loadLevel(currentLevel);
            } else {
                gameState = GameState.VICTORY;
            }
        }
    }

    /**
     * Clears buffers and draws overlays (titles, HUD, game state menus) on top of the screen.
     * Overrides the Scene lifecycle 'render' method.
     * 
     * 💡 JAVA GRAPHICS TECHNIQUE: AWT Graphics2D Rendering
     * We pass a Graphics2D context pointing to our scaled window buffer.
     * Graphics2D allows us to render transparent geometries, handle vector graphics
     * and use high-performance text blitting.
     * 
     * @param g2d The Graphics2D AWT context.
     */
    @Override
    public void render(Graphics2D g2d) {
        // Position camera view offsets on the software renderer
        renderContext.setCameraOffset((int) camera.getX(), (int) camera.getY());
        
        // Clear screen using a dark neon background color (0xFF0F0F0F)
        renderContext.clear(0xFF0F0F0F);

        // Blit level graphics and active layers into pixel buffers
        if (level != null) {
            level.render(renderContext);
        }

        int bw = app.getBufferWidth();
        int bh = app.getBufferHeight();

        // Render game overlays based on current GameState
        if (gameState == GameState.LEVEL_START) {
            // Draw transparent background shadow overlay
            g2d.setColor(new Color(0, 0, 0, 180));
            g2d.fillRect(0, 0, bw, bh);

            // Level text
            g2d.setColor(new Color(255, 204, 0)); // Golden yellow
            g2d.setFont(new Font("Monospaced", Font.BOLD, 18));
            g2d.drawString("LEVEL " + currentLevel, 110, 75);

            g2d.setColor(Color.WHITE);
            g2d.setFont(new Font("Monospaced", Font.PLAIN, 9));
            String name = (currentLevel == 1) ? "Spawn Garden" : 
                          (currentLevel == 2) ? "Forest Arena" : 
                          (currentLevel == 3) ? "Maze Dungeon" : "Boss Castle";
            // Centered name offset calculation
            g2d.drawString(name, 150 - (g2d.getFontMetrics().stringWidth(name) / 2), 95);
        }
        else if (gameState == GameState.PLAYING) {
            // Render HUD bar containing player HP, Lives, Level and Score
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

        // Draw F3 diagnostic overlay
        if (showDebug) {
            debugOverlay.render(g2d, app, level, camera.getX(), camera.getY());
        }
    }

    /**
     * Renders HUD metrics at the bottom of the screen (health bar, lives hearts, score).
     * 
     * @param g2d The Graphics2D context.
     * @param health The HealthComponent of the player containing HP values.
     */
    private void renderPlayerHpBar(Graphics2D g2d, HealthComponent health) {
        int barW = 60;
        int barH = 6;
        int x = 5;
        int y = 156; // Fixed bottom position, keeping HUD out of main play area

        // Background shadow (dark red)
        g2d.setColor(new Color(0x3a, 0x05, 0x05));
        g2d.fillRect(x, y, barW, barH);

        // Foreground filled health ratio
        g2d.setColor(new Color(0x00, 0xff, 0x66)); // Cyber green
        
        // 💡 MATH RATIO CALCULATION
        // We calculate the filled bar width by multiplying the max width by the health fraction (0.0 to 1.0),
        // then casting the result to an integer to match pixel dimensions.
        int filledW = (int) (barW * health.getHealthPercentage());
        g2d.fillRect(x, y, filledW, barH);

        // White border outline
        g2d.setColor(Color.WHITE);
        g2d.drawRect(x, y, barW, barH);

        g2d.setFont(new Font("Monospaced", Font.PLAIN, 8));

        // Renders player lives hearts symbol indicator
        g2d.setColor(new Color(0xff, 0x33, 0x66));
        String hearts = "";
        for (int i = 0; i < playerLives; i++) {
            hearts += "♥";
        }
        g2d.drawString("LIVES: " + hearts, 75, 162);

        // Render current Level
        g2d.setColor(new Color(0x00, 0xee, 0xff));
        g2d.drawString("LVL: " + currentLevel + "/4", 145, 162);

        // Render Score
        g2d.setColor(new Color(0xff, 0xcc, 0x00));
        g2d.drawString("SCORE: " + score, 210, 162);
    }
}
