package com.petstore.test;

import com.petstore.client.StoreApiClient;
import com.petstore.pojo.Order;
import io.restassured.response.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.Assert;
import org.testng.annotations.*;
import com.petstore.util.TestDataGenerator;

public class TestSuite extends BaseTest {
    private static final Logger logger = LoggerFactory.getLogger(TestSuite.class);
    private StoreApiClient storeClient;

    private Long orderId;
    private Long createdOrderId;

    @BeforeClass
    @Parameters("env")
    @Override
    public void setup(String env) {
        super.setup(env);
        logger.info("Initializing StoreApiClient for tests");
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
        Response response = storeClient.placeOrder(order)
                .then()
                .statusCode(200)
                .extract().response();

         createdOrderId = response.jsonPath().getLong("id");
        
        Assert.assertEquals(response.jsonPath().getLong("id"), order.id());
        Assert.assertEquals(response.jsonPath().getInt("petId"), order.petId());
        Assert.assertEquals(response.jsonPath().getInt("quantity"), order.quantity());
        Assert.assertEquals(response.jsonPath().getString("status"), order.status());
        Assert.assertEquals(response.jsonPath().getBoolean("complete"), order.complete());
    }

    @Test(priority = 2,dependsOnMethods = "testPlaceOrder")
    public void testGetOrder() {
        logger.info("Running testGetOrderById");
//        Response response = storeClient.getOrderById(createdOrderId);
        Response response = waitForOrder(createdOrderId, 5, 500);
        Assert.assertEquals(response.statusCode(), 200, "Failed to fetch order after retries");
        Order orderDetails = storeClient.deserializeResponse(response, Order.class);
        logger.info("Order Details {}", orderDetails);
        Assert.assertEquals(orderDetails.id(), createdOrderId, "ID mismatch");
        Assert.assertEquals(orderDetails.petId(), response.jsonPath().getInt("petId"),"Pet Id Mismatch");
        Assert.assertEquals(orderDetails.quantity(), response.jsonPath().getInt("quantity"),"Quantity Mismatch");
        Assert.assertEquals(orderDetails.status(),response.jsonPath().getString("status"),"Status Mismatch");
        Assert.assertEquals(orderDetails.complete(),response.jsonPath().getBoolean("complete"),"Complete Mismatch");
    }

    private Response waitForOrder(Long orderId, int retries, long delayMillis) {
        Response response = null;
        for (int i = 0; i < retries; i++) {
            response = storeClient.getOrderById(orderId);
            if (response.statusCode() == 200) {
                logger.info("Order matched successfully on retry attempt {}", i);
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

    @Test(priority = 3, dependsOnMethods = "testPlaceOrder")
    public void testDeleteOrder() {
        logger.info("Running testDeleteOrder");
        String message=storeClient.deleteOrder(createdOrderId).then().statusCode(200).extract().jsonPath().getString("message");
        Assert.assertEquals(message, String.valueOf(createdOrderId));

        Response response = storeClient.getOrderById(createdOrderId);
        Assert.assertEquals(response.statusCode(),404);
        logger.info("Order deleted successfully {}",response.asString());
    }

    @DataProvider(name = "invalidOrders")
    public Object[][] invalidOrders() {
        return new Object[][]{
                {new Order(null, 101, 1, "invalid-date", "placed", true)}, // invalid date
        };
    }

    @Test(dataProvider = "invalidOrders")
    public void testInvalidOrderPlacement(Order invalidOrder) {
        logger.info("Running testInvalidOrderPlacement");
        Response response = storeClient.placeOrder(invalidOrder);
        logger.info("Response Code is: {}", response.statusCode());
        Assert.assertTrue(response.statusCode() >= 400, "Expected error for invalid order");
    }

    @Test
    public void testGetOrderByInvalidId() {
        logger.info("Running testGetOrderByInvalidId");
        Response response = storeClient.getOrderById(999999L);
        Assert.assertEquals( response.getStatusCode(),404,"Status Code Mismatch");
        Assert.assertEquals( response.jsonPath().getString("type"),"error","Type field mismatch");
        Assert.assertEquals(  response.jsonPath().getString("message"),"Order not found","Message field mismatch");
        logger.info("testGetOrderByInvalidId completed successfully");
    }

    @Test()
    public void testInventory() {
        logger.info("Running testInventory");
        Response response = storeClient.getInventory();
        response.then().statusCode(200);

        var inventory = response.jsonPath().getMap("$");
        logger.info("Inventory: {}", inventory);
        Assert.assertTrue(inventory.containsKey("available"));
        Assert.assertTrue(inventory.containsKey("sold"));
        Assert.assertTrue(inventory.containsKey("pending"));
//        Assert.assertTrue(inventory.containsKey("unavailable"));
    }
}

