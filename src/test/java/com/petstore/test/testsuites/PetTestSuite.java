package com.petstore.test.testsuites;

import com.petstore.client.PetApiClient;
import com.petstore.pojo.Pet;
import com.petstore.test.BaseTest;
import com.petstore.util.AwaitUtil;
import com.petstore.util.TestDataGenerator;
import io.restassured.response.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

public class PetTestSuite extends BaseTest {
    private static final Logger logger = LoggerFactory.getLogger(PetTestSuite.class);
    private PetApiClient petClient;
    private Long createdPetId;
    private final String testInstanceId = UUID.randomUUID().toString();
    private Pet createdPet;

    @BeforeClass
    @Override
    protected void setup() {
        String env = System.getProperty("env", "dev");
        super.setupEnvironment(env);
        logger.info("[{}] Thread-{}: Initializing PetApiClient for environment: {}", testInstanceId, Thread.currentThread().getId(), env);
        petClient = new PetApiClient();
    }

//    @AfterMethod
//    public void setResponse(ITestResult result) {
//        // Ensure response attribute is set by test methods
//    }

    @Test
    public void testAddPet() {
        logger.info("[{}] Thread-{}: Running testAddPet", testInstanceId, Thread.currentThread().getId());
        createdPet = TestDataGenerator.createPet(testInstanceId);
        long startTime = System.currentTimeMillis();
        Response response = petClient.addPet(createdPet)
                .then()
                .statusCode(200)
                .extract().response();
//        ITestResult result = org.testng.Reporter.getCurrentTestResult();
//        result.setAttribute("response", response);
        long responseTime = System.currentTimeMillis() - startTime;
        logger.info("[{}] Thread-{}: testAddPet response time: {}ms, status: {}, body: {}",
                testInstanceId, Thread.currentThread().getId(), responseTime, response.getStatusCode(), response.asString());
        createdPetId = response.jsonPath().getLong("id");
        Assert.assertNotNull(createdPetId, "Pet ID should not be null");
        Pet actual = petClient.deserializeResponse(response, Pet.class);
        assertPetEquals(response, createdPet);
        assertPetMatches(response, createdPet);
    }

    @Test(priority = 2, dependsOnMethods = "testAddPet")//, retryAnalyzer = com.petstore.test.RetryAnalyzer.class)
    public void testGetPet() {
        logger.info("[{}] Thread-{}: Running testGetPet", testInstanceId, Thread.currentThread().getId());
        long startTime = System.currentTimeMillis();
        Response createResponseSave = AwaitUtil.waitForResponse(
                () -> petClient.getPetById(createdPetId),
                res -> res.getStatusCode() == 200,
                30, 1, "Get order request for pet after post request"
        );
        long responseTime = System.currentTimeMillis() - startTime;
        logger.info("[{}] Thread-{}: testGetPet response time: {}ms, status: {}, body: {}",
                testInstanceId, Thread.currentThread().getId(), responseTime, createResponseSave.getStatusCode(), createResponseSave.asString());
        Pet actual = petClient.deserializeResponse(createResponseSave, Pet.class);
        Assert.assertEquals(actual.id(), createdPetId, "Pet ID mismatch");
        assertPetMatches(createResponseSave, createdPet);
    }

    @Test(priority = 4, dependsOnMethods = {"testUpdatePetWithFormData"})//, retryAnalyzer = com.petstore.test.RetryAnalyzer.class)
    public void testDeletePet() {
        logger.info("[{}] Thread-{}: Running testDeletePet", testInstanceId, Thread.currentThread().getId());
        long startTime = System.currentTimeMillis();
//        Response response = petClient.deletePet(createdPetId)
//                .then()
//                .statusCode(200)
//                .extract().response();
        Response delResponseSave = AwaitUtil.waitForResponse(
                () -> petClient.deletePet(createdPetId),
                res -> res.getStatusCode() == 200,
                30, 1, "delete pet request"
        );
        long responseTime = System.currentTimeMillis() - startTime;
        logger.info("[{}] Thread-{}: testDeletePet response time: {}ms, status: {}, body: {}",
                testInstanceId, Thread.currentThread().getId(), responseTime, delResponseSave.getStatusCode(), delResponseSave.asString());
        Assert.assertEquals(delResponseSave.getStatusCode(), 200, "Failed to delete pet");
        startTime = System.currentTimeMillis();
        Response getResponseSave = AwaitUtil.waitForResponse(
                () -> petClient.getPetById(createdPetId),
                res -> res.getStatusCode() == 404,
                30, 1, "Get order request for pet after delete request"
        );
        responseTime = System.currentTimeMillis() - startTime;
        logger.info("[{}] Thread-{}: testDeletePet get response time: {}ms, status: {}, body: {}",
                testInstanceId, Thread.currentThread().getId(), responseTime, getResponseSave.getStatusCode(), getResponseSave.asString());
        Assert.assertEquals(getResponseSave.getStatusCode(), 404, "Pet should not be available");
        Assert.assertEquals(getResponseSave.jsonPath().getString("message"), "Pet not found", "Pet should not be available");
    }

//    @DataProvider(name = "invalidPetData")
//    public Object[][] invalidPetData() {
//        return new Object[][]{
////                {TestDataGenerator.createPetWithMissingName(testInstanceId), "Missing name", "addPet"},
////                {TestDataGenerator.createPetWithEmptyPhotoUrls(testInstanceId), "Empty photoUrls", "addPet"},
////                {TestDataGenerator.createPetWithInvalidStatus(testInstanceId), "Invalid status", "addPet"}
//        };
//    }
//
//    @Test(dataProvider = "invalidPetData")//, retryAnalyzer = com.petstore.test.RetryAnalyzer.class)
//    public void testAddInvalidPet(Pet invalidPet, String testCase, String operation) {
//        logger.info("[{}] Thread-{}: Running testAddInvalidPet: {}", testInstanceId, Thread.currentThread().getId(), testCase);
//        long startTime = System.currentTimeMillis();
//        Response response = petClient.addPet(invalidPet);
//        long responseTime = System.currentTimeMillis() - startTime;
//        logger.info("[{}] Thread-{}: testAddInvalidPet ({}) response time: {}ms, status: {}, body: {}",
//                testInstanceId, Thread.currentThread().getId(), testCase, responseTime, response.getStatusCode(), response.asString());
//        Assert.assertTrue(response.getStatusCode() >= 400, "Expected error for invalid pet: " + testCase);
//        Assert.assertTrue(response.jsonPath().getString("message").contains("Invalid"), "Error message mismatch for: " + testCase);
//    }

//    @DataProvider(name = "invalidStatusData")
//    public Object[][] invalidStatusData() {
//        return new Object[][]{
//                {"invalid", "Invalid status for findPetsByStatus"},
//                {"", "Empty status for findPetsByStatus"}
//        };
//    }
//
//    @Test(dataProvider = "invalidStatusData")//, retryAnalyzer = com.petstore.test.RetryAnalyzer.class)
//    public void testFindPetsByInvalidStatus(String status, String testCase) {
//        logger.info("[{}] Thread-{}: Running testFindPetsByInvalidStatus: {}", testInstanceId, Thread.currentThread().getId(), testCase);
//        long startTime = System.currentTimeMillis();
//        Response response = petClient.findPetsByStatus(status)
//                .then()
//                .statusCode(400)
//                .extract().response();
//        long responseTime = System.currentTimeMillis() - startTime;
//        logger.info("[{}] Thread-{}: testFindPetsByInvalidStatus ({}) response time: {}ms, status: {}, body: {}",
//                testInstanceId, Thread.currentThread().getId(), testCase, responseTime, response.getStatusCode(), response.asString());
//        Assert.assertEquals(response.getStatusCode(), 400, "Expected 400 for: " + testCase);
//        Assert.assertTrue(response.jsonPath().getString("message").contains("Invalid"), "Error message mismatch for: " + testCase);
//    }

    @Test(priority = 7)//(retryAnalyzer = com.petstore.test.RetryAnalyzer.class)
    public void testFindPetsByStatus() {
        logger.info("[{}] Thread-{}: Running testFindPetsByStatus", testInstanceId, Thread.currentThread().getId());
        // Add a pet to ensure available pets exist
        Pet tempPet = TestDataGenerator.createPet(testInstanceId);
        Response addResponse = petClient.addPet(tempPet)
                .then()
                .statusCode(200)
                .extract().response();
        Long tempPetId = addResponse.jsonPath().getLong("id");

        long startTime = System.currentTimeMillis();
        Response response = petClient.findPetsByStatus("available")
                .then()
                .statusCode(200)
                .extract().response();
//        Response getResponseSave = AwaitUtil.waitForResponse(
//                () -> petClient.findPetsByStatus(createdPetId),
//                res -> res.getStatusCode() == 404,
//                30, 1, "Get order request for pet after delete request"
//        );
        long responseTime = System.currentTimeMillis() - startTime;
        logger.info("[{}] Thread-{}: testFindPetsByStatus response time: {}ms, status: {}, body: {}",
                testInstanceId, Thread.currentThread().getId(), responseTime, response.getStatusCode(), response.asString());
        List<Pet> pets = petClient.deserializePetListResponse(response);
        Assert.assertNotNull(pets, "Pet list should not be null");
        Assert.assertFalse(pets.isEmpty(), "Expected non-empty list of pets");
        for (Pet pet : pets) {
            Assert.assertEquals(pet.status(), "available", "Pet status mismatch");
        }

        // Clean up
        petClient.deletePet(tempPetId);
    }

//    @DataProvider(name = "invalidFormData")
//    public Object[][] invalidFormData() {
//        return new Object[][]{
//                {"", "available", "Empty name"},
//                {null, "available", "Null name"},
//                {"UpdatedPet-" + testInstanceId, "invalid", "Invalid status"}
//        };
//    }

//    @Test(priority = 2, dependsOnMethods = "testAddPet", dataProvider = "invalidFormData")//, retryAnalyzer = com.petstore.test.RetryAnalyzer.class)
//    public void testUpdatePetWithInvalidFormData(String name, String status, String testCase) {
//        logger.info("[{}] Thread-{}: Running testUpdatePetWithInvalidFormData: {}", testInstanceId, Thread.currentThread().getId(), testCase);
//        long startTime = System.currentTimeMillis();
//        Response response = petClient.updatePetWithFormData(createdPetId, name, status)
//                .then()
//                .statusCode(400)
//                .extract().response();
//        long responseTime = System.currentTimeMillis() - startTime;
//        logger.info("[{}] Thread-{}: testUpdatePetWithInvalidFormData ({}) response time: {}ms, status: {}, body: {}",
//                testInstanceId, Thread.currentThread().getId(), testCase, responseTime, response.getStatusCode(), response.asString());
//        Assert.assertEquals(response.getStatusCode(), 404, "Expected 404 for: " + testCase);
//        Assert.assertTrue(response.jsonPath().getString("message").contains("Invalid"), "Error message mismatch for: " + testCase);
//    }

    @Test(priority = 3, dependsOnMethods = {"testAddPet","testGetPet"})//, retryAnalyzer = com.petstore.test.RetryAnalyzer.class)
    public void testUpdatePetWithFormData() {
        logger.info("[{}] Thread-{}: Running testUpdatePetWithFormData", testInstanceId, Thread.currentThread().getId());
        String newName = "UpdatedPet";
        String newStatus = "pending12";
        long startTime = System.currentTimeMillis();
//        Response response = petClient.updatePetWithFormData(createdPetId, newName, newStatus)
//                .then()
//                .statusCode(200)
//                .extract().response();
        Response updateResponseSave = AwaitUtil.waitForResponse(
                () -> petClient.updatePetWithFormData(createdPetId, newName, newStatus),
                res -> res.getStatusCode() == 200,
                30, 1, "Get order request for pet after post request for form data"
        );
        long responseTime = System.currentTimeMillis() - startTime;
        logger.info("[{}] Thread-{}: testUpdatePetWithFormData response time: {}ms, status: {}, body: {}",
                testInstanceId, Thread.currentThread().getId(), responseTime, updateResponseSave.getStatusCode(), updateResponseSave.asString());
        // Verify update
//        Response getResponse = petClient.getPetById(createdPetId)
//                .then()
//                .statusCode(200)
//                .extract().response();

        Response getResponseSave = AwaitUtil.waitForResponse(
                () -> petClient.getPetById(createdPetId),
                res -> res.getStatusCode() == 200,
                30, 1, "Get order request for pet after post request for form data"
        );

        Assert.assertEquals(getResponseSave.jsonPath().getString("name"), newName, "Name update failed");
        Assert.assertEquals(getResponseSave.jsonPath().getString("status"), newStatus, "Status update failed");
    }

    private void assertPetEquals(Response response, Pet expected) {
        Pet actual = petClient.deserializeResponse(response, Pet.class);
        Assert.assertEquals(actual, expected, "Pet object mismatch");
    }

    private void assertPetMatches(Response response, Pet expected) {
        Assert.assertEquals(response.jsonPath().getLong("id"), expected.id());
        Assert.assertEquals(response.jsonPath().getString("category.name"),
                expected.category() != null ? expected.category().name() : null, "Category name mismatch");
        Assert.assertEquals(response.jsonPath().getString("name"), expected.name(), "Name mismatch");
        Assert.assertEquals(response.jsonPath().getList("photoUrls"), expected.photoUrls(), "Photo URLs mismatch");
        Assert.assertEquals(response.jsonPath().getList("tags.name"),
                expected.tags() != null ? expected.tags().stream().map(Pet.Tag::name).toList() : null,
                "Tags mismatch");
        Assert.assertEquals(response.jsonPath().getString("status"), expected.status(), "Status mismatch");
    }
}