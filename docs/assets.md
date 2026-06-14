# Asset Management

Asset handling is centralized, avoiding static file reading operations inside constructors.

---

## 1. Centralized Caching
The `AssetManager` maintains an internal map caching loaded `BufferedImage` files. If an asset is loaded multiple times, it is read from the disk once and referenced thereafter:
```java
private final Map<String, BufferedImage> imageCache = new HashMap<>();
```

---

## 2. Fallback Texture Loader
If an asset is missing or throws an `IOException` during load, the `AssetManager` returns a checkerboard fallback texture instead of crashing the JVM. This keeps the application stable under missing-file errors.
