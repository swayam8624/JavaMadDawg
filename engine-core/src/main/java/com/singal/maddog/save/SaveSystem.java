package com.singal.maddog.save;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Handles JSON-based game state serialization and deserialization.
 */
public class SaveSystem {
    private static final Logger LOGGER = Logger.getLogger(SaveSystem.class.getName());
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    public static class GameSettings {
        public int width = 300;
        public int height = 168;
        public int scale = 3;
        public String title = "Rain Remastered";
        public boolean soundEnabled = true;
    }

    public static class PlayerSaveState {
        public String name = "Player";
        public float x = 19 * 16;
        public float y = 42 * 16;
        public int health = 100;
        public int score = 0;
    }

    public static boolean saveSettings(GameSettings settings, String filePath) {
        return serialize(settings, filePath);
    }

    public static GameSettings loadSettings(String filePath) {
        GameSettings settings = deserialize(filePath, GameSettings.class);
        return settings != null ? settings : new GameSettings();
    }

    public static boolean savePlayerState(PlayerSaveState state, String filePath) {
        return serialize(state, filePath);
    }

    public static PlayerSaveState loadPlayerState(String filePath) {
        PlayerSaveState state = deserialize(filePath, PlayerSaveState.class);
        return state != null ? state : new PlayerSaveState();
    }

    private static boolean serialize(Object data, String filePath) {
        File file = new File(filePath);
        
        // Ensure directories exist
        File parent = file.getParentFile();
        if (parent != null && !parent.exists()) {
            parent.mkdirs();
        }

        try (FileWriter writer = new FileWriter(file)) {
            GSON.toJson(data, writer);
            LOGGER.info("Successfully serialized data to: " + filePath);
            return true;
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Failed to serialize data to: " + filePath, e);
            return false;
        }
    }

    private static <T> T deserialize(String filePath, Class<T> type) {
        File file = new File(filePath);
        if (!file.exists()) {
            LOGGER.warning("Save file not found: " + filePath);
            return null;
        }

        try (FileReader reader = new FileReader(file)) {
            T data = GSON.fromJson(reader, type);
            LOGGER.info("Successfully deserialized data from: " + filePath);
            return data;
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Failed to deserialize data from: " + filePath, e);
            return null;
        }
    }
}
