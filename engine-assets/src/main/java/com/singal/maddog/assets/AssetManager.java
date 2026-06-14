package com.singal.maddog.assets;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;

/**
 * Handles resource loading, caching, and management.
 * Avoids duplicate file IO operations by checking memory caches.
 */
public class AssetManager {
    private static final Logger LOGGER = Logger.getLogger(AssetManager.class.getName());
    
    private final Map<String, BufferedImage> imageCache = new HashMap<>();
    private BufferedImage fallbackImage;

    public AssetManager() {
        createFallbackImage();
    }

    private void createFallbackImage() {
        // Create a 16x16 magenta and black checkerboard fallback image
        fallbackImage = new BufferedImage(16, 16, BufferedImage.TYPE_INT_RGB);
        for (int y = 0; y < 16; y++) {
            for (int x = 0; x < 16; x++) {
                boolean isMagenta = ((x / 8) + (y / 8)) % 2 == 0;
                fallbackImage.setRGB(x, y, isMagenta ? 0xFFFF00FF : 0x00000000);
            }
        }
    }

    /**
     * Loads a BufferedImage from the classpath resource path.
     * Caches the image for future requests.
     *
     * @param path Classpath resource path, starting with a slash, e.g., "/textures/sheets/spritesheet.png"
     * @return The loaded BufferedImage or the fallback placeholder image if not found.
     */
    public BufferedImage loadImage(String path) {
        if (imageCache.containsKey(path)) {
            return imageCache.get(path);
        }

        LOGGER.info("Loading image asset from path: " + path);
        try (InputStream is = getClass().getResourceAsStream(path)) {
            if (is == null) {
                LOGGER.warning("Could not find resource: " + path + ". Using fallback image.");
                return fallbackImage;
            }
            BufferedImage image = ImageIO.read(is);
            if (image != null) {
                imageCache.put(path, image);
                return image;
            }
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Failed to load image asset: " + path, e);
        }

        return fallbackImage;
    }

    /**
     * Clears all cached image assets.
     */
    public void clearCache() {
        imageCache.clear();
    }
}
