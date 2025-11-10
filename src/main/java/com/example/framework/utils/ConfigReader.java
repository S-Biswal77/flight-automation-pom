package com.example.framework.utils;

import java.io.InputStream;
import java.util.Properties;

public class ConfigReader {
    private static Properties prop = new Properties();

    static {
        try (InputStream is = ConfigReader.class.getClassLoader().getResourceAsStream("config.properties")) {
            prop.load(is);
        } catch (Exception e) {
            throw new RuntimeException("Failed to load config.properties", e);
        }
    }

    public static String get(String key) {
        return prop.getProperty(key);
    }

    public static int getInt(String key, int defaultVal) {
        try {
            return Integer.parseInt(prop.getProperty(key));
        } catch (Exception e) {
            return defaultVal;
        }
    }
}
