# Development Log: MadDawg Remastered

This development log tracks the timeline, feature implementations, bug fixes, and engineering hours spent modernizing and refining the **MadDawg** game and engine.

---

## ⏱️ Development Hours Log

| Milestone / Task | Description | Hours Spent |
| :--- | :--- | :--- |
| **Phase 1: Build System** | Migrating legacy codebase, establishing 13-module Gradle workspace, and JDK toolchain. | 3.0 hrs |
| **Phase 2: Core Loop & Loop Abstractions** | Implementing precise fixed-timestep accumulator, variable rendering, and layer/scene classes. | 4.0 hrs |
| **Phase 3: Software Renderer & Assets** | Setting up flat integer pixel blitting, Y-sorted render queues, texture parsing, and Asset caching. | 5.0 hrs |
| **Phase 4: ECS-Lite Composition** | Implementing `Entity` containers and components (`Transform`, `Sprite`, `Movement`, `Health`, etc.). | 5.0 hrs |
| **Phase 5: Physics & Colliders** | Writing Axis-Aligned Bounding Box (AABB) intersection check and tile collision slide mechanics. | 4.0 hrs |
| **Phase 6: Action Input System** | Mapping Action bindings for Keyboard and Mouse inputs to drive character states. | 3.0 hrs |
| **Phase 7: Background Pathfinding** | Writing decoupled A* pathfinder and navigation grid calculations. | 4.0 hrs |
| **Phase 8: UDP Networking Demo** | Creating packet serializers, UDP socket managers, client connections, and position replication. | 6.0 hrs |
| **Phase 9: Diagnostic Tools & Tests** | Designing F3 HUD overlay, CPU memory monitors, and writing automated JUnit test suites. | 4.0 hrs |
| **Phase 10: Gameplay & Polish (MadDawg)** | Redesigning main menu, centering colliders, damage cooldowns, 4 procedural levels, boss fight, pickups. | 6.0 hrs |
| **Total Engineering Time** | | **44.0 hrs** |

---

## 🛠️ Chronological Milestones & Core Updates

### 1. Build System Decoupling
* **Problem**: The original codebase was built as a single monolithic Eclipse project, which led to circular dependencies and poor separation of concerns.
* **Update**: Decoupled the codebase into a 13-module Gradle project. Dependencies flow strictly from math -> physics -> renderer -> core -> game.

### 2. AWT/Swing Scale Coordinates Mismatch
* **Problem**: The main menu text was completely offscreen, and the player HP bar was invisible.
* **Reason**: The software backbuffer size was set to `300x168` (scaled up `3x` to fit a `900x504` window). The original UI text coordinates were coded using scaled screen metrics (e.g. drawing at `x=180, y=240` and font size `36`), which bled outside the `300x168` graphics bounds.
* **Update**: Adjusted all UI text, fonts, borders, and hearts to render inside the `300x168` coordinate space. HP bar and stats were shifted to the bottom (`y = 156`), keeping the top clear for the F3 debug overlay.

### 3. Sprite vs. Physical Collider Mismatch
* **Problem**: Player attacks (projectiles) passed directly through enemies without registering hits, and enemies standing on the player dealt no damage.
* **Reason**: The spritesheets contain `32x32` pixel assets, but the physical colliders were defined as `14x14` offset at `(1,1)` (top-left corner). When player shot at the center of the enemy, the projectile's AABB checked coordinates around `(16,16)` which completely missed the `(1,1)` to `(15,15)` collider. Furthermore, there was no player-enemy contact damage check in the collision resolution step.
* **Update**: 
  - Centered player and enemy body colliders to `(9,9)` of size `14x14` relative to the `32x32` sprite bounds.
  - Implemented enemy-to-player collision checks dealing `15` damage on contact.
  - Added a custom `InvincibilityComponent` to trigger `1.0s` of damage immunity on hit, flashing the player's sprite opacity.

### 4. Stuck Enemy Bug (Level 1)
* **Problem**: One of the enemies spawned in Level 1 was trapped inside wall tiles and could not be killed, meaning the level could never be cleared.
* **Update**: Implemented `spawnChaserSafely(xGrid, yGrid, tileMap)`. This function checks if the target spawn coordinate is solid. If solid, it runs a concentric search pattern outwards to locate the nearest walkable grass/floor tile, ensuring no enemy is ever spawned trapped inside walls.

### 5. Level 3 Solid Spawn Bug
* **Problem**: Upon entering Level 3 (Maze Dungeon), the player was frozen and unable to move.
* **Reason**: The procedural maze generator constructed pillars at every even grid node (`x % 2 == 0 && y % 2 == 0`). Since the player was spawned at `(2, 2)`, the player was spawned directly inside a solid pillar.
* **Update**: Modified the procedural loop to explicitly clear a starting region of `5x5` tiles (`x < 5 && y < 5`) as walkable floor tiles (`ID 3`), ensuring a safe player spawn room.

### 6. Boss & Rewards Logic
* **Update**: Added a health potion reward drop (30% chance on enemy defeat, healing +25 HP) and a scaled `2x` giant Boss Cherno in Level 4 with custom projectile attacks and 200 HP.
