package com.petstore.test.testsuites;

import com.petstore.client.StoreApiClient;
import com.petstore.pojo.Order;
import com.petstore.test.BaseTest;
import com.petstore.util.AwaitUtil;
import io.restassured.response.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.Assert;
import org.testng.annotations.*;
import com.petstore.util.TestDataGenerator;
import java.util.UUID;

public class StoreTestSuite extends BaseTest {
    private static final Logger logger = LoggerFactory.getLogger(StoreTestSuite.class);
    private StoreApiClient storeClient;
    private Long createdOrderId;
    private final String testInstanceId = UUID.randomUUID().toString(); // Unique ID for test instance

    @BeforeClass
    @Override
    public void setup() {
        String env = System.getProperty("env", "dev");
        super.setupEnvironment(env);
        logger.info("[{}] Thread-{}: Initializing StoreApiClient for environment: {}", testInstanceId, Thread.currentThread().getId(), env);
        storeClient = new StoreApiClient();
    }

    @DataProvider(name = "orders")
    public Object[][] orders() {
        return new Object[][] {
                { TestDataGenerator.createOrder() },
        };
    }

    @Test(dataProvider = "orders")
    public void testPlaceOrder(Order order) {
        logger.info("[{}] Thread-{}: Running testPlaceOrder", testInstanceId, Thread.currentThread().getId());
        long startTime = System.currentTimeMillis();
        Response response = storeClient.placeOrder(order)
                .then()
                .statusCode(200)
                .extract().response();
        long responseTime = System.currentTimeMillis() - startTime;
        logger.info("[{}] Thread-{}: testPlaceOrder response time: {}ms, status: {}, body: {}",
                testInstanceId, Thread.currentThread().getId(), responseTime, response.getStatusCode(), response.asString());
        createdOrderId = response.jsonPath().getLong("id");
        assertOrderMatches(response, order);
    }

    @Test(priority = 2, dependsOnMethods = "testPlaceOrder")
    public void testGetOrder() {
        logger.info("[{}] Thread-{}: Running testGetOrderById", testInstanceId, Thread.currentThread().getId());
        long startTime = System.currentTimeMillis();
        Response createResponseSave = AwaitUtil.waitForResponse(
                () -> storeClient.getOrderById(createdOrderId),
                res -> res.getStatusCode() == 200,
                30, 1, "Get order request"
        );
        long responseTime = System.currentTimeMillis() - startTime;
        logger.info("[{}] Thread-{}: testGetOrderById response time: {}ms, status: {}, body: {}",
                testInstanceId, Thread.currentThread().getId(), responseTime, createResponseSave.getStatusCode(), createResponseSave.asString());
        Assert.assertEquals(createResponseSave.statusCode(), 200, "Failed to fetch order after retries");
        Order orderDetails = storeClient.deserializeResponse(createResponseSave, Order.class);
        logger.info("[{}] Thread-{}: Order Details {}", testInstanceId, Thread.currentThread().getId(), orderDetails);
        Assert.assertEquals(orderDetails.id(), createdOrderId, "ID mismatch");
        assertOrderMatches(createResponseSave, orderDetails);
    }

    @Test(priority = 3, dependsOnMethods = "testPlaceOrder")
    public void testDeleteOrder() {
        logger.info("[{}] Thread-{}: Running testDeleteOrder", testInstanceId, Thread.currentThread().getId());
        long startTime = System.currentTimeMillis();
        Response delResponseSave = AwaitUtil.waitForResponse(
                () -> storeClient.deleteOrder(createdOrderId),
                res -> res.getStatusCode() == 200,
                10, 1, "Delete order request"
        );
        long responseTime = System.currentTimeMillis() - startTime;
        logger.info("[{}] Thread-{}: testDeleteOrder response time: {}ms, status: {}, body: {}",
                testInstanceId, Thread.currentThread().getId(), responseTime, delResponseSave.getStatusCode(), delResponseSave.asString());
        Assert.assertEquals(delResponseSave.jsonPath().getString("message"), String.valueOf(createdOrderId), "");
        startTime = System.currentTimeMillis();
        Response response = AwaitUtil.waitForResponse(
                () -> storeClient.getOrderById(createdOrderId),
                res -> res.getStatusCode() == 404,
                20, 1, "Get order request after delete"
        );
        responseTime = System.currentTimeMillis() - startTime;
        logger.info("[{}] Thread-{}: testDeleteOrder get response time: {}ms, status: {}, body: {}",
                testInstanceId, Thread.currentThread().getId(), responseTime, response.getStatusCode(), response.asString());
        Assert.assertEquals(response.statusCode(), 404, "Order should not be available");
        logger.info("[{}] Thread-{}: Order deleted successfully {}", testInstanceId, Thread.currentThread().getId(), response.asString());
    }

    @DataProvider(name = "invalidOrders")
    public Object[][] invalidOrders() {
        return new Object[][]{
                {new Order(null, 101, 1, "invalid-date", "placed", true)}, // invalid date
        };
    }

    @Test(dataProvider = "invalidOrders")
    public void testInvalidOrderPlacement(Order invalidOrder) {
        logger.info("[{}] Thread-{}: Running testInvalidOrderPlacement", testInstanceId, Thread.currentThread().getId());
        long startTime = System.currentTimeMillis();
        Response response = storeClient.placeOrder(invalidOrder);
        long responseTime = System.currentTimeMillis() - startTime;
        logger.info("[{}] Thread-{}: testInvalidOrderPlacement response time: {}ms, status: {}, body: {}",
                testInstanceId, Thread.currentThread().getId(), responseTime, response.getStatusCode(), response.asString());
        Assert.assertTrue(response.statusCode() >= 400, "Expected error for invalid order");
    }

    @Test
    public void testGetOrderByInvalidId() {
        logger.info("[{}] Thread-{}: Running testGetOrderByInvalidId", testInstanceId, Thread.currentThread().getId());
        long startTime = System.currentTimeMillis();
        Response response = storeClient.getOrderById(999999L);
        long responseTime = System.currentTimeMillis() - startTime;
        logger.info("[{}] Thread-{}: testGetOrderByInvalidId response time: {}ms, status: {}, body: {}",
                testInstanceId, Thread.currentThread().getId(), responseTime, response.getStatusCode(), response.asString());
        Assert.assertEquals(response.getStatusCode(), 404, "Status Code Mismatch");
        Assert.assertEquals(response.jsonPath().getString("type"), "error", "Type field mismatch");
        Assert.assertEquals(response.jsonPath().getString("message"), "Order not found", "Message field mismatch");
        logger.info("[{}] Thread-{}: testGetOrderByInvalidId completed successfully", testInstanceId, Thread.currentThread().getId());
    }

    @Test
    public void testInventory() {
        logger.info("[{}] Thread-{}: Running testInventory", testInstanceId, Thread.currentThread().getId());
        long startTime = System.currentTimeMillis();
        Response response = storeClient.getInventory();
        long responseTime = System.currentTimeMillis() - startTime;
        logger.info("[{}] Thread-{}: testInventory response time: {}ms, status: {}, body: {}",
                testInstanceId, Thread.currentThread().getId(), responseTime, response.getStatusCode(), response.asString());
        response.then().statusCode(200);
        var inventory = response.jsonPath().getMap("$");
        logger.info("[{}] Thread-{}: Inventory: {}", testInstanceId, Thread.currentThread().getId(), inventory);
        Assert.assertTrue(inventory.containsKey("available"));
        Assert.assertTrue(inventory.containsKey("sold"));
        Assert.assertTrue(inventory.containsKey("pending"));
    }

    private Response waitForOrder(Long orderId, int retries, long delayMillis) {
        Response response = null;
        for (int i = 0; i < retries; i++) {
            response = storeClient.getOrderById(orderId);
            if (response.statusCode() == 200) {
                logger.info("[{}] Thread-{}: Order matched successfully on retry attempt {}", testInstanceId, Thread.currentThread().getId(), i);
                return response;
            }
            try {
                Thread.sleep(delayMillis);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
        return response;
    }

    private void assertOrderMatches(Response response, Order order) {
        Assert.assertEquals(response.jsonPath().getLong("id"), order.id());
        Assert.assertEquals(response.jsonPath().getInt("petId"), order.petId());
        Assert.assertEquals(response.jsonPath().getInt("quantity"), order.quantity());
        Assert.assertEquals(response.jsonPath().getString("status"), order.status());
        Assert.assertEquals(response.jsonPath().getBoolean("complete"), order.complete());
    }

    private void assertOrderEquals(Response response, Order expected) {
        Order actual = storeClient.deserializeResponse(response, Order.class);
        Assert.assertEquals(actual, expected, "Order object mismatch");
    }
}