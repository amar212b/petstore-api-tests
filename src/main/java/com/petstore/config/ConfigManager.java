package com.petstore.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class ConfigManager {
    private static final Logger logger = LoggerFactory.getLogger(ConfigManager.class);
    private static ConfigManager instance;
    private final Properties properties;

    private ConfigManager(String env) {
        properties = new Properties();
        String propFile = "env-" + env + ".properties";
        try (InputStream input = getClass().getClassLoader().getResourceAsStream(propFile)) {
            if (input == null) {
                logger.error("Configuration file {} not found", propFile);
            }

            properties.load(input);
            String baseUrl = properties.getProperty("base.url");
            if (baseUrl == null || baseUrl.trim().isEmpty()) {
                logger.error("base.url is missing or empty in {}", propFile);
                throw new RuntimeException("base.url is missing or empty in " + propFile);
            }
            logger.info("Loaded configuration for environment: {}. Set baseURI: {}", env, baseUrl);
//            logger.info("Loaded configuration for environment: {}", env);
        } catch (Exception e) {
            logger.error("Failed to load configuration: {}", e.getMessage());
            throw new RuntimeException("Failed to load configuration: " + e.getMessage());
        }
    }

    public static synchronized ConfigManager getInstance(String env) {
        if (instance == null) {
            instance = new ConfigManager(env);
        }
        return instance;
    }

    public static synchronized void reset() {
        instance = null;
    }

    public String getBaseUrl() {
        return properties.getProperty("base.url");
    }

    public String getApiKey() {
        return properties.getProperty("api.key");
    }
    public String getProperty(String key) {
        return properties.getProperty(key);
    }

}