package com.petstore.test;

import io.restassured.RestAssured;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.BeforeClass;
import com.petstore.config.ConfigManager;

public abstract class BaseTest {
    private static final Logger logger = LoggerFactory.getLogger(BaseTest.class);

    @BeforeClass
    protected void setup() {
        String env = System.getProperty("env", "dev");
        setupEnvironment(env);
    }

    protected void setupEnvironment(String env) {
        logger.info("Setting up environment: {}", env);
        ConfigManager config = ConfigManager.getInstance(env);
        String baseUrl = config.getBaseUrl();
        if (baseUrl == null || baseUrl.trim().isEmpty()) {
            logger.error("base.url is missing or empty for env: {}", env);
            throw new RuntimeException("base.url is missing or empty");
        }
        RestAssured.baseURI = config.getBaseUrl();
        logger.info("Base URI set to: {}", RestAssured.baseURI);
    }
}

