package com.petstore.client;

import com.petstore.pojo.Order;
import io.restassured.response.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

public class StoreApiClient extends InitApiClient {
    private static final Logger logger = LoggerFactory.getLogger(StoreApiClient.class);
    private static final String BASE = "/store";
    public Response placeOrder(Order order) {
        logger.debug("Placing order with body: {}", order.toString());
        return post(BASE +"/order", order);
    }

    public Response getOrderById(Long orderId) {
        logger.debug("Retrieving order with id: {}", orderId);
        return get(BASE +"/order/" + orderId);
    }

    public Response deleteOrder(Long orderId) {
        logger.debug("Deleting order with id: {}", orderId);
        return delete(BASE +"/order/" + orderId);
    }

    public Response getInventory() {
        logger.debug("Retrieving store inventory");
        return get(BASE +"/inventory");
    }

    public <T> T deserializeResponse(Response response, Class<T> clazz) {
        try {
            return mapper.readValue(response.asString(), clazz);
        } catch (IOException e) {
            logger.error("Failed to deserialize response: {}", e.getMessage());
            throw new RuntimeException("Failed to deserialize response", e);
        }
    }




//    public Response placeOrder(Order order) {
//        return RestAssured.given()
//                .contentType("application/json")
//                .body(order)
//                .post(BASE + "/order");
//    }
//
//    public Response getOrder(long id) {
//        return RestAssured.given()
//                .get(BASE + "/order/" + id);
//    }
//
//    public Response deleteOrder(long id) {
//        return RestAssured.given()
//                .delete(BASE + "/order/" + id);
//    }
//
//    public Response getInventory() {
//        return RestAssured.given().get(BASE + "/inventory");
//    }
}