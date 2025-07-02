package com.petstore.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.petstore.config.ConfigManager;
import io.restassured.filter.log.RequestLoggingFilter;
import io.restassured.filter.log.ResponseLoggingFilter;
import io.restassured.http.Method;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Map;

import static io.restassured.RestAssured.given;

public class InitApiClient {
    protected final ObjectMapper mapper;
    private static final Logger logger = LoggerFactory.getLogger(InitApiClient.class);

    public InitApiClient() {
        this.mapper = new ObjectMapper();
        logger.debug("Initialized ApiClient with default configuration");
    }

    protected Response request(Method method, String path, Object body, Map<String, String> headers, Map<String, String> queryParams) {
        logger.info("{} request to {}", method, path);
        RequestSpecification spec = given()
                .filter(new RequestLoggingFilter())
                .filter(new ResponseLoggingFilter())
                .contentType("application/json");

        // Apply authentication
        String apiKey = ConfigManager.getInstance(System.getProperty("env", "dev")).getApiKey();
        if (apiKey != null && !apiKey.isEmpty()) {
            spec.header("api_key", apiKey);
        }
        String oauthToken = ConfigManager.getInstance(System.getProperty("env", "dev")).getProperty("oauth.token");
        if (oauthToken != null && !oauthToken.isEmpty()) {
            spec.header("Authorization", "Bearer " + oauthToken);
        }

        // Apply headers and query parameters
        if (headers != null) {
            spec.headers(headers);
        }
        if (queryParams != null) {
            spec.queryParams(queryParams);
        }

        // Set body for POST/PUT/PATCH
        if (body != null && (method == Method.POST || method == Method.PUT || method == Method.PATCH)) {
            try {
                String jsonBody = mapper.writeValueAsString(body);
                spec.body(jsonBody);
            } catch (IOException e) {
                logger.error("Failed to serialize request body for path {}: {}", path, e.getMessage());
                throw new RuntimeException("Failed to serialize request body", e);
            }
        }

        // Execute request
        return spec.request(method, path);
    }

    public Response get(String path) {
        return request(Method.GET, path, null, null, null);
    }

    public Response get(String path, Map<String, String> queryParams) {
        return request(Method.GET, path, null, null, queryParams);
    }

    public <T> Response post(String path, T body) {
        return request(Method.POST, path, body, null, null);
    }

    public <T> Response post(String path, T body, Map<String, String> headers) {
        return request(Method.POST, path, body, headers, null);
    }

    public Response delete(String path) {
        return request(Method.DELETE, path, null, null, null);
    }

    public Response put(String path, Object body) {
        return request(Method.PUT, path, body, null, null);
    }

    public Response patch(String path, Object body) {
        return request(Method.PATCH, path, body, null, null);
    }


    public Response post(String path, Map<String, String> formParams) {
        logger.info("POST request with form data to {}", path);
        RequestSpecification spec = given()
                .filter(new RequestLoggingFilter())
                .filter(new ResponseLoggingFilter())
                .contentType("application/x-www-form-urlencoded");

        // Apply authentication
        String apiKey = ConfigManager.getInstance(System.getProperty("env", "dev")).getApiKey();
        if (apiKey != null && !apiKey.isEmpty()) {
            spec.header("api_key", apiKey);
        }
        String oauthToken = ConfigManager.getInstance(System.getProperty("env", "dev")).getProperty("oauth.token");
        if (oauthToken != null && !oauthToken.isEmpty()) {
            spec.header("Authorization", "Bearer " + oauthToken);
        }

        // Apply form parameters
        if (formParams != null) {
            spec.formParams(formParams);
        }

        // Execute request
        return spec.request(Method.POST, path);
    }

}