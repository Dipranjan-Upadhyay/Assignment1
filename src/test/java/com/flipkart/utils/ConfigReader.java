package com.flipkart.utils;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

// reads values from config.properties.
// priority: -Dkey=value from command line, then env variable (login.mobile -> LOGIN_MOBILE), then the file
public final class ConfigReader {

    private static final Logger log = LogManager.getLogger(ConfigReader.class);
    private static final String CONFIG_FILE = "config.properties";
    private static final Properties prop = new Properties();

    static {
        try (InputStream in = ConfigReader.class.getClassLoader().getResourceAsStream(CONFIG_FILE)) {
            if (in == null) {
                throw new IllegalStateException(CONFIG_FILE + " not found");
            }
            prop.load(in);
            log.info("Config loaded from {}", CONFIG_FILE);
        } catch (IOException e) {
            throw new ExceptionInInitializerError(e);
        }
    }

    private ConfigReader() {
    }

    public static String get(String key) {
        String value = getValue(key);
        if (value == null) {
            throw new IllegalArgumentException("Key not found in config: " + key);
        }
        return value;
    }

    public static String get(String key, String defaultValue) {
        String value = getValue(key);
        return value == null ? defaultValue : value;
    }

    public static int getInt(String key, int defaultValue) {
        String value = getValue(key);
        return value == null ? defaultValue : Integer.parseInt(value);
    }

    public static boolean getBoolean(String key, boolean defaultValue) {
        String value = getValue(key);
        return value == null ? defaultValue : Boolean.parseBoolean(value);
    }

    private static String getValue(String key) {
        String value = System.getProperty(key);
        if (isEmpty(value)) {
            value = System.getenv(key.toUpperCase().replace('.', '_'));
        }
        if (isEmpty(value)) {
            value = prop.getProperty(key);
        }
        return isEmpty(value) ? null : value.trim();
    }

    private static boolean isEmpty(String value) {
        return value == null || value.isBlank();
    }
}
