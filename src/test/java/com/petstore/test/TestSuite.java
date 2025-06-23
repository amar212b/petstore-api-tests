package com.petstore.test;

import com.petstore.client.StoreApiClient;
import com.petstore.pojo.Order;
import io.restassured.response.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.Assert;
import org.testng.annotations.*;
import com.petstore.util.TestDataGenerator;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import static org.awaitility.Awaitility.await;
public class TestSuite extends BaseTest {
    private static final Logger logger = LoggerFactory.getLogger(TestSuite.class);
    private StoreApiClient storeClient;
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
        AtomicReference<Response> createResponseSave=new AtomicReference<>();
        await()
                .atMost(20, TimeUnit.SECONDS)
                .pollInterval(1, TimeUnit.SECONDS)
                .until(() -> {
                    Response response = storeClient.getOrderById(createdOrderId);
                    if (response.getStatusCode() == 200) {
                        createResponseSave.set(response);
                        return true;
                    }
                    return false;
                });
//        Response response = waitForOrder(createdOrderId, 5, 500);
//        Response response=storeClient.getOrderById(createdOrderId);
        Assert.assertEquals(createResponseSave.get().statusCode(), 200, "Failed to fetch order after retries");
        Order orderDetails = storeClient.deserializeResponse(createResponseSave.get(), Order.class);
        logger.info("Order Details {}", orderDetails);
        Assert.assertEquals(orderDetails.id(), createdOrderId, "ID mismatch");
        Assert.assertEquals(orderDetails.petId(), createResponseSave.get().jsonPath().getInt("petId"),"Pet Id Mismatch");
        Assert.assertEquals(orderDetails.quantity(), createResponseSave.get().jsonPath().getInt("quantity"),"Quantity Mismatch");
        Assert.assertEquals(orderDetails.status(),createResponseSave.get().jsonPath().getString("status"),"Status Mismatch");
        Assert.assertEquals(orderDetails.complete(),createResponseSave.get().jsonPath().getBoolean("complete"),"Complete Mismatch");
    }

    @Test(priority = 3, dependsOnMethods = "testPlaceOrder")
    public void testDeleteOrder() {
        logger.info("Running testDeleteOrder");
        AtomicReference<Response> delResponseSave=new AtomicReference<>();
        await()
                .atMost(10, TimeUnit.SECONDS)
                .pollInterval(2, TimeUnit.SECONDS)
                .ignoreExceptions()
                .until(() -> {
                    Response response = storeClient.deleteOrder(createdOrderId);
                    boolean success = response.getStatusCode() == 200;
                    if (success) {
                        delResponseSave.set(response);
                        logger.info("Delete succeeded: {}", response.asString());
                    } else {
                        logger.warn("Delete failed, retrying... Status: {}", response.getStatusCode());
                    }
                    return success;
                });
//        String message=storeClient.deleteOrder(createdOrderId).then().statusCode(200).extract().jsonPath().getString("message");
        Assert.assertEquals(delResponseSave.get().jsonPath().getString("message"), String.valueOf(createdOrderId));
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        Response response = storeClient.getOrderById(createdOrderId);
        Assert.assertEquals(response.statusCode(),404,"order should not be available");
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
}

