package com.petstore.test;

import io.restassured.RestAssured;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.BeforeClass;
import com.petstore.config.ConfigManager;
import org.testng.annotations.Parameters;

public abstract class BaseTest {
    private static final Logger logger = LoggerFactory.getLogger(BaseTest.class);


    @BeforeClass
    @Parameters("env")
    public void setup(String env) {
        logger.info("Setting up test environment: {}", env);
        ConfigManager config = ConfigManager.getInstance(env);
        String baseUrl = config.getBaseUrl();
        if (baseUrl == null || baseUrl.trim().isEmpty()) {
            logger.error("base.url is missing or empty for env: {}", env);
            throw new RuntimeException("base.url is missing or empty");
        }
        RestAssured.baseURI = baseUrl;
        logger.info("Set RestAssured.baseURI to: {}", RestAssured.baseURI);
    }
}

