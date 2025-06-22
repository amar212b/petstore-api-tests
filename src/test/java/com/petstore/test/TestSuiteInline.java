//package com.petstore.test;
//
//import com.petstore.pojo.Order;
//import io.restassured.response.Response;
//import org.testng.Assert;
//import org.testng.annotations.DataProvider;
//import org.testng.annotations.Test;
//
//public class TestSuiteInline {
//    @DataProvider(name = "orderData")
//    public Object[][] orderData() {
//        return new Object[][]{
//                {new Order(1L, 101, 2, "2025-06-18T12:00:00Z", "placed", true)},
////                {new Order(null, 202, 5, "2025-06-19T14:00:00Z", "approved", false)}
//        };
//    }
//
//    @Test(priority = 1,dataProvider = "orderData")
//    public void testPlaceOrderInlineData(Order inputOrder) {
//        logger.info("Running testPlaceOrderInlineData");
////        Order order = new Order(null, 999, 1, OffsetDateTime.now().toString(), "placed", true);
//        Response response = storeClient.placeOrder(inputOrder);
//        Assert.assertEquals(response.statusCode(), 200, "Expected status code 200");
//        Order createdOrder = storeClient.deserializeResponse(response, Order.class);
//
//        orderId = createdOrder.id();
//        logger.info("Created Order ID: {}", orderId);
//        Assert.assertNotNull(createdOrder.id(), "Order ID should not be null");
//        Assert.assertEquals(createdOrder.petId(), inputOrder.petId(), "Pet ID mismatch");
//        Assert.assertEquals(createdOrder.quantity(), inputOrder.quantity(), "Quantity mismatch");
//        Assert.assertEquals(createdOrder.status(), inputOrder.status(), "Status mismatch");
//        Assert.assertEquals(createdOrder.complete(), inputOrder.complete(), "Complete flag mismatch");
//        logger.info("testPlaceOrderInlineData completed successfully");
//    }
//
//    @DataProvider(name = "invalidOrders")
//    public Object[][] invalidOrders() {
//        return new Object[][]{
////                {new Order(null, null, 1, null, "placed", true)}, // null petId
////                {new Order(null, 101, -1, null, "placed", true)}, // negative quantity
//                {new Order(null, 101, 1, "invalid-date", "placed", true)}, // invalid date
////                {new Order(null, 101, 1, null, null, null)} // missing status and complete
//        };
//    }
//
//    @Test(dataProvider = "invalidOrders")
//    public void testInvalidOrderPlacement(Order invalidOrder) {
//        logger.info("Running testInvalidOrderPlacement");
//        Response response = storeClient.placeOrder(invalidOrder);
//        logger.info("Response Code is: {}", response.statusCode());
//        Assert.assertTrue(response.statusCode() >= 400, "Expected error for invalid order");
//    }
//
//
//    @DataProvider(name = "orderId")
//    public Object[][] orderIds() {
//        return new Object[][]{
//                {100, false},
//                {1, true},
//                {10, true},
//                {5, true},
//                {-1, false}
//        };
//    }
//
//    @Test(priority = 2,dependsOnMethods = "testPlaceOrderInlineData")
//    public void testGetOrderInlineData() {
//        logger.info("Running testGetOrderInlineData");
//        Response response = storeClient.getOrderById(orderId);
//        response.then().statusCode(200)
//                .extract().response();
//
//        Order orderDetails = storeClient.deserializeResponse(response, Order.class);
//        logger.info("Order Details {}", orderDetails);
//        Assert.assertEquals(orderDetails.id(), orderId, "ID mismatch");
//        Assert.assertEquals(orderDetails.status(),"placed","Status Mismatch");
//
//    }
//
//
//    @Test(priority = 3, dependsOnMethods = "testPlaceOrderInlineData")
//    public void testDeleteOrderInlineData() {
//        logger.info("Running testDeleteOrderInlineData");
//        String message=storeClient.deleteOrder(orderId).then().statusCode(200).extract().jsonPath().getString("message");
//        Assert.assertEquals(message, String.valueOf(orderId));
//
//        Response response = storeClient.getOrderById(orderId);
//        Assert.assertEquals(response.statusCode(),404);
//        logger.info("Order deleted successfully.");
//    }
//
//    @Test
//    public void testGetOrderByInvalidId() {
//        logger.info("Running testGetOrderByInvalidId");
//        Response response = storeClient.getOrderById(999999L);
//        Assert.assertEquals( response.getStatusCode(),404,"Status Code Mismatch");
//        Assert.assertEquals( response.jsonPath().getString("type"),"error","Type field mismatch");
//        Assert.assertEquals(  response.jsonPath().getString("message"),"Order not found","Message field mismatch");
//        logger.info("testGetOrderByInvalidId completed successfully");
//    }
//
//    @Test()
//    public void testInventory() {
//        logger.info("Running testInventory");
//        Response response = storeClient.getInventory();
//        response.then().statusCode(200);
//
//        var inventory = response.jsonPath().getMap("$");
//        logger.info("Inventory: {}", inventory);
//        Assert.assertTrue(inventory.containsKey("available"));
//        Assert.assertTrue(inventory.containsKey("sold"));
//        Assert.assertTrue(inventory.containsKey("pending"));
////        Assert.assertTrue(inventory.containsKey("unavailable"));
//    }
//}
