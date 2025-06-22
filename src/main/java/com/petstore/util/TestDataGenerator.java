package com.petstore.util;

import com.petstore.pojo.Order;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;
import java.time.ZonedDateTime;
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
}