# Contributing Guidelines

This document outlines standard guidelines for developing and contributing to **Rain Remastered**.

---

## Code Quality Standards

* **Strict Module Separation**: Never add imports from high-level modules (e.g. `com.singal.maddog.game`) into low-level engine modules (e.g. `engine-math` or `engine-renderer`).
* **Decoupled Systems**: Ensure components only interact via interfaces or tags. Avoid typecasting and concrete subclass checks.
* **No Out-of-Bounds Indexing**: When working with key codes, coordinates, or array indexing, always perform bounds checking.
* **No Blockers on the Main Thread**: Long-running operations, such as pathfinding or database operations, must be dispatched to background threads.
* **Write Unit Tests**: Every new math utility, physics calculation, or deserialization handler should be accompanied by JUnit tests in the `tests` module.

---

## Development Workflow

1. Create a feature branch off of the `master` branch.
2. Implement your changes following compositional and decoupled architecture.
3. Validate your code by running local tests:
   ```bash
   ./gradlew test
   ```
4. Verify compiling and launching:
   ```bash
   ./gradlew compileJava
   ./gradlew :rain-game:run
   ```
5. Submit a pull request detailing the refactored architecture.
