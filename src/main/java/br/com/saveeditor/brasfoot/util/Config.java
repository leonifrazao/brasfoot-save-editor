package br.com.saveeditor.brasfoot.util;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;

public class Config {

    private static final String CONFIG_FILE = "config.properties";
    private static final Properties properties = new Properties();

    static {
        load();
    }

    public static void load() {
        try (FileInputStream in = new FileInputStream(CONFIG_FILE)) {
            properties.load(in);
        } catch (IOException e) {
            // Provide defaults or ignore if file doesn't exist
        }
    }

    public static void save() {
        try (FileOutputStream out = new FileOutputStream(CONFIG_FILE)) {
            properties.store(out, "Brasfoot Save Editor Configuration");
        } catch (IOException e) {
            System.err.println("Failed to save config: " + e.getMessage());
        }
    }

    public static String get(String key) {
        return properties.getProperty(key);
    }

    public static String get(String key, String defaultValue) {
        return properties.getProperty(key, defaultValue);
    }

    public static void set(String key, String value) {
        properties.setProperty(key, value);
        save();
    }

    public static String getDefaultSavePath() {
        return get("default_save_path", "");
    }

    public static void setDefaultSavePath(String path) {
        set("default_save_path", path);
    }
}
