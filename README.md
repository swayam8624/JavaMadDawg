# MadDawg Remastered Game & Engine

**MadDawg** is a high-performance, portfolio-grade modernization of The Cherno's classic 2D Java Game Programming series. Rebuilt from the ground up as a decoupled, 13-module Gradle project using **Java 22**, it demonstrates advanced game engine architectural patterns, CPU-based software rendering pipelines, modular composition (ECS-Lite), UDP client-server networking, procedural level generation, and custom physics collision systems.

---

## 🚀 Key Systems & Architectures

### 1. Fixed-Timestep Accumulator Game Loop
The engine implements a thread-safe, precise game loop decoupled from variable rendering frame rates:
* **Fixed Timestep (Fixed Update)**: Runs at exactly 60 Hz to ensure physics, AI, and collision updates are deterministic and identical on all systems.
* **Variable Render (Render Tick)**: Blits as fast as the monitor/OS allows, interpolating entity positions to achieve ultra-smooth visual frame rates.

### 2. Composition-driven ECS-Lite
Instead of traditional deep inheritance structures (e.g., `Player extends Mob extends Entity`), MadDawg uses composition:
* **`Entity`**: A lightweight container holding a collection of components.
* **`Component`**: Independent state and logic wrappers (e.g., `TransformComponent`, `ColliderComponent`, `MovementComponent`, `HealthComponent`) queried dynamically at runtime.

### 3. CPU Software Rendering Pipeline
Rather than drawing directly to AWT graphics, the engine writes 32-bit ARGB pixels into a flat 1D integer array backing a `BufferedImage`. 
* Decoupled via a `RenderContext` interface to allow seamless hot-swapping to a modern OpenGL/Vulkan backend (via LWJGL) in the future.
* Performs fast software sprite blitting, directional walk cycles, and transparency key skipping.

### 4. Procedural & Seeded Map Generation
Features a level generation pipeline spanning 4 distinct stages:
* **Stage 1 (Spawn Garden)**: Loads from resource image maps.
* **Stage 2 (Forest Arena)**: Procedurally generates tree/wall barriers on a grassy grid using seeded pseudorandom distribution.
* **Stage 3 (Maze Dungeon)**: Generates a randomized corridor labyrinth, guaranteeing a clear `5x5` walkable starting zone for the player spawn.
* **Stage 4 (Boss Castle)**: Spawns a custom-scaled giant Cherno Boss entity with specialized projectile attacks and tanky HP stats.

---

## 📂 Multi-Module Layout

```
ChernaMadDog/
├── settings.gradle        # Declares 13 submodules
├── build.gradle           # Configures toolchains & dependencies
├── engine-math/           # Vector2i, Vector2f, MathUtils
├── engine-core/           # Application, GameLoop, Scene, Component, Entity, Level
├── engine-renderer/       # Texture, Sprite, SpriteSheet, Animation, Camera2D, RenderContext
├── engine-assets/         # AssetManager (with asset memory caching)
├── engine-input/          # InputManager (action-map input bindings)
├── engine-physics2d/      # Axis-Aligned Bounding Box (AABB) collisions
├── engine-ai/             # Decoupled A* Pathfinding navigation
├── engine-network/        # UDP networking packets and sockets
├── engine-tools/          # Diagnostic debug overlays (FPS, UPS, Memory)
├── rain-game/             # Game client executable (MadDawg)
├── rain-server/           # Standalone game server
└── tests/                 # JUnit 5 automated test suites
```

---

## 🛠️ Quick Start for Developers

### Prerequisites
* **Java SDK 21 or 22** configured on your local system path.

### Development Commands

1. **Compile the Entire Project**:
   ```bash
   ./gradlew compileJava
   ```

2. **Run the Game Client**:
   ```bash
   ./gradlew :rain-game:run
   ```

3. **Run the Standalone UDP Server**:
   ```bash
   ./gradlew :rain-server:run
   ```

4. **Execute Unit Tests**:
   ```bash
   ./gradlew test
   ```

5. **Clean All Build Folders**:
   ```bash
   ./gradlew clean
   ```

---

## 📖 Deep Dives & Guides
For in-depth explanations on custom systems, refer to:
* [DEV_LOG.md](DEV_LOG.md) - Chronological logs, milestones, updates, and time metrics.
* [ARCHITECTURE.md](ARCHITECTURE.md) - Deep architectural boundaries and module interactions.
* [ORIGINAL_CHERNO_COMPARISON.md](ORIGINAL_CHERNO_COMPARISON.md) - Audit comparing legacy bottlenecks to this modernized remaster.
* [LEARNING_NOTES.md](LEARNING_NOTES.md) - Core concepts and programming patterns used.
