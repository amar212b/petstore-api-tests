package com.petstore.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.petstore.config.ConfigManager;
import io.restassured.filter.log.RequestLoggingFilter;
import io.restassured.filter.log.ResponseLoggingFilter;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.io.IOException;
import static io.restassured.RestAssured.given;

public class InitApiClient {

    protected final RequestSpecification requestSpec;
    protected final ObjectMapper mapper;
    private static final Logger logger = LoggerFactory.getLogger(InitApiClient.class);

    public InitApiClient() {
        this.requestSpec = given()
                .filter(new RequestLoggingFilter())
                .filter(new ResponseLoggingFilter())
                .contentType("application/json");
        this.mapper = new ObjectMapper();
        logger.debug("Initialized ApiClient with default configuration");
    }

    public Response get(String path) {
        logger.info("GET request to {}", path);
        return requestSpec.get(path);
    }

    public <T> Response post(String path, T body) {
        try {
            logger.info("POST request to {}", path);
            String jsonBody = mapper.writeValueAsString(body);
            return requestSpec.body(jsonBody).post(path);
        } catch (IOException e) {
            logger.error("Failed to serialize request body for path {}: {}", path, e.getMessage());
            throw new RuntimeException("Failed to serialize request body", e);
        }
    }

    public Response delete(String path) {
        logger.info("DELETE request to {}", path);
        return requestSpec.delete(path);
    }
}
