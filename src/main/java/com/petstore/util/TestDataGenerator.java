package com.petstore.util;

import com.petstore.pojo.Order;
import com.petstore.pojo.Pet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;
import java.time.ZonedDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

public class TestDataGenerator {
    private static final Logger logger = LoggerFactory.getLogger(TestDataGenerator.class);
    public static Order createOrder() {
        logger.info("Generating Test Data for Creating Order");
        long id = ThreadLocalRandom.current().nextLong(1, 10);
        int petId = ThreadLocalRandom.current().nextInt(1, 100);
        int quantity = ThreadLocalRandom.current().nextInt(1, 100);
        String shipDate = Instant.now().toString();
        String[] statuses = {"placed", "approved", "delivered"};
        String status = statuses[ThreadLocalRandom.current().nextInt(statuses.length)];
        boolean complete = ThreadLocalRandom.current().nextBoolean();
        return new Order(id, petId, quantity, shipDate, status, complete);
    }

    public static Pet createPet(String testInstanceId) {
        logger.info("[{}] Generating Test Data for Creating Pet", testInstanceId);
        long id = ThreadLocalRandom.current().nextLong(1, 200);
        long categoryId = ThreadLocalRandom.current().nextLong(1, 100);
        String[] categoryNames = {"Dog", "Cat", "Bird", "Fish", "Rabbit"};
        String categoryName = categoryNames[ThreadLocalRandom.current().nextInt(categoryNames.length)];
        String name = "TestPet-" + testInstanceId + "-" + ThreadLocalRandom.current().nextInt(1000, 10000);
        String photoUrl = "https://example.com/photo-" + UUID.randomUUID().toString() + ".jpg";
        long tagId = ThreadLocalRandom.current().nextLong(1, 100);
        String[] tagNames = {"Friendly", "Playful", "Calm", "Energetic", "Loyal"};
        String tagName = tagNames[ThreadLocalRandom.current().nextInt(tagNames.length)];
        String[] statuses = {"available", "pending", "sold"};
        String status = statuses[ThreadLocalRandom.current().nextInt(statuses.length)];
        return new Pet(
                id, // Let API assign ID
                new Pet.Category(categoryId, categoryName),
                name,
                Arrays.asList(photoUrl),
                Collections.singletonList(new Pet.Tag(tagId, tagName)),
                status
        );
    }

    public static Pet createPetWithMissingName(String testInstanceId) {
        logger.info("[{}] Generating Test Data for Pet with Missing Name", testInstanceId);
        long id = ThreadLocalRandom.current().nextLong(1, 10);
        long categoryId = ThreadLocalRandom.current().nextLong(1, 100);
        String[] categoryNames = {"Dog", "Cat", "Bird"};
        String categoryName = categoryNames[ThreadLocalRandom.current().nextInt(categoryNames.length)];
        String photoUrl = "https://example.com/photo-" + UUID.randomUUID().toString() + ".jpg";
        String[] statuses = {"available", "pending", "sold"};
        String status = statuses[ThreadLocalRandom.current().nextInt(statuses.length)];
        return new Pet(id, new Pet.Category(categoryId, categoryName), null, Arrays.asList(photoUrl), null, status);
    }

    public static Pet createPetWithEmptyPhotoUrls(String testInstanceId) {
        logger.info("[{}] Generating Test Data for Pet with Empty Photo URLs", testInstanceId);
        long id = ThreadLocalRandom.current().nextLong(1, 10);
        long categoryId = ThreadLocalRandom.current().nextLong(1, 100);
        String[] categoryNames = {"Dog", "Cat", "Bird"};
        String categoryName = categoryNames[ThreadLocalRandom.current().nextInt(categoryNames.length)];
        String name = "TestPet-" + testInstanceId + "-" + ThreadLocalRandom.current().nextInt(1000, 10000);
        String[] statuses = {"available", "pending", "sold"};
        String status = statuses[ThreadLocalRandom.current().nextInt(statuses.length)];
        return new Pet(id, new Pet.Category(categoryId, categoryName), name, Collections.emptyList(), null, status);
    }

    public static Pet createPetWithInvalidStatus(String testInstanceId) {
        logger.info("[{}] Generating Test Data for Pet with Invalid Status", testInstanceId);
        long id = ThreadLocalRandom.current().nextLong(1, 10);
        long categoryId = ThreadLocalRandom.current().nextLong(1, 100);
        String[] categoryNames = {"Dog", "Cat", "Bird"};
        String categoryName = categoryNames[ThreadLocalRandom.current().nextInt(categoryNames.length)];
        String name = "TestPet-" + testInstanceId + "-" + ThreadLocalRandom.current().nextInt(1000, 10000);
        String photoUrl = "https://example.com/photo-" + UUID.randomUUID().toString() + ".jpg";
        return new Pet(id, new Pet.Category(categoryId, categoryName), name, Arrays.asList(photoUrl), null, "invalid");
    }
}