# Rain Remastered

**Rain Remastered** is a professional, portfolio-grade modernization of The Cherno's classic Java Game Programming tutorial series. Rebuilt from the ground up as a decoupled, multi-module Gradle project using **Java 21/22**, it demonstrates advanced game engine architectural patterns, software rendering pipelines, modular component design (ECS-Lite), UDP networking, and precise timing loops.

---

## 🚀 Features

* **Strict Decoupled Architecture**: Separation of core engine code (math, input, assets, physics, rendering, networking) from game-specific logic.
* **Double/Triple Buffered Software Renderer**: CPU-based pixel-blitting pipeline writing to flat integer arrays, decoupled through a `RenderContext` abstraction to support future hardware acceleration (LWJGL/OpenGL/Vulkan).
* **Precise Accumulator Game Loop**: Fixed-timestep updates with variable rendering rates, delta-timing, and frame interpolation support.
* **Component-based Entity System (ECS-Lite)**: Replaces deep inheritance hierarchies with composition-driven entities and lightweight component behaviors.
* **A* Pathfinding**: Decoupled grid-based pathfinding running on background worker scopes.
* **UDP Networking Demo**: Clean client-server packet-based position replication and connection lifecycle handshakes.
* **JSON Serialization**: Modern save/load system for user configurations and game progress.
* **HUD Diagnostics**: Green-screen overlay monitoring active entities, CPU memory, camera coordinates, FPS, and UPS.
* **Automated Unit Testing**: Complete JUnit 5 test suite verifying core engine mathematics, physics boxes, serialization, and pathfinding.

---

## 📂 Project Structure

```
├── build.gradle
├── settings.gradle
├── engine-math/           # Vector2i, Vector2f, MathUtils
├── engine-core/           # Application, GameLoop, Scene, Screen, Layer
├── engine-renderer/       # Texture, Sprite, SpriteSheet, Animation, Camera2D
├── engine-assets/         # AssetManager, Resource loaders
├── engine-input/          # InputManager, ActionMap, Bindings
├── engine-physics2d/      # AABB, Colliders, PhysicsWorld
├── engine-ai/             # Pathfinding (A*), NavigationGrid
├── engine-network/        # Packets, NetClient, NetServer
├── engine-tools/          # Debug tools, loggers, overlay
├── rain-game/             # Game client executable (Rain Remastered)
├── rain-server/           # Standalone game server executable
└── tests/                 # JUnit 5 test suites
```

---

## 🛠️ Getting Started

### Prerequisites
* JDK 21 or 22 (configured on your path)

### Compiling
To compile all modules and classes:
```bash
./gradlew compileJava
```

### Running the Game
To start the remastered game client:
```bash
./gradlew :rain-game:run
```

### Running the Server
To start the standalone game server:
```bash
./gradlew :rain-server:run
```

### Running Tests
To execute the automated JUnit 5 test suite:
```bash
./gradlew test
```

---

## 📖 Documentation

* [ARCHITECTURE.md](ARCHITECTURE.md) - Deep dive into the modular structure, components, and rendering context.
* [ORIGINAL_CHERNO_COMPARISON.md](ORIGINAL_CHERNO_COMPARISON.md) - Audit of legacy bottlenecks versus modernized architectural design choices.
* [ROADMAP.md](ROADMAP.md) - Path forward for C++/Vulkan, LWJGL, and multiplayer expansions.
* [LEARNING_NOTES.md](LEARNING_NOTES.md) - Learning base summaries and engine takeaways.
* [docs/](docs/) - Detailed documentation on Game Loop, Rendering, Collision, Networking, Assets, and Entities.
