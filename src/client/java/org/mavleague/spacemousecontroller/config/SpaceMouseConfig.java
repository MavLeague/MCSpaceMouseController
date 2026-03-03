package org.mavleague.spacemousecontroller.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;
import org.mavleague.spacemousecontroller.client.SpaceMouseButtonAction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

/**
 * Handles saving and loading the user preferences to a JSON file.
 */
public class SpaceMouseConfig {

    private static final File CONFIG_FILE = new File(FabricLoader.getInstance().getConfigDir().toFile(), "spacemousecontroller.json");
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    public static final Logger LOGGER = LoggerFactory.getLogger("spacemousecontroller");

    // Default configuration values
    public static float lookSensitivity = 150.0f;
    public static boolean invertPitch = false;
    public static boolean invertYaw = false;
    public static SpaceMouseButtonAction button1Action = SpaceMouseButtonAction.ATTACK;
    public static SpaceMouseButtonAction button2Action = SpaceMouseButtonAction.USE;

    public static void load() {
        if (CONFIG_FILE.exists()) {
            try (FileReader reader = new FileReader(CONFIG_FILE)) {
                ConfigData data = GSON.fromJson(reader, ConfigData.class);
                if (data != null) {
                    lookSensitivity = data.lookSensitivity;
                    invertPitch = data.invertPitch;
                    invertYaw = data.invertYaw;
                    button1Action = data.button1Action;
                    button2Action = data.button2Action;
                }
            } catch (IOException e) {
                LOGGER.error("Failed to load config file!", e);
            }
        } else {
            // Create the file with default values if it doesn't exist
            save();
        }
    }

    public static void save() {
        try (FileWriter writer = new FileWriter(CONFIG_FILE)) {
            ConfigData data = new ConfigData();
            data.lookSensitivity = lookSensitivity;
            data.invertPitch = invertPitch;
            data.invertYaw = invertYaw;
            data.button1Action = button1Action;
            data.button2Action = button2Action;
            GSON.toJson(data, writer);
        } catch (IOException e) {
            LOGGER.error("Failed to save config file!", e);
        }
    }

    /**
     * Data Transfer Object for GSON serialization.
     */
    private static class ConfigData {
        public float lookSensitivity = SpaceMouseConfig.lookSensitivity;
        public boolean invertPitch = SpaceMouseConfig.invertPitch;
        public boolean invertYaw = SpaceMouseConfig.invertYaw;
        public SpaceMouseButtonAction button1Action;
        public SpaceMouseButtonAction button2Action;
    }
}