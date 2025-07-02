package com.petstore.util;

import io.restassured.response.Response;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Predicate;
import java.util.function.Supplier;
import static org.awaitility.Awaitility.await;

public class AwaitUtil {
    private static final Logger logger = LoggerFactory.getLogger(AwaitUtil.class);

    public static Response waitForResponse(
            Supplier<Response> requestSupplier,
            Predicate<Response> successCondition,
            long timeoutSeconds,
            long pollIntervalSeconds,
            String description
    ) {
        AtomicReference<Response> responseRef = new AtomicReference<>();

        await(description)
                .atMost(timeoutSeconds, TimeUnit.SECONDS)
                .pollInterval(pollIntervalSeconds, TimeUnit.SECONDS)
                .ignoreExceptions()
                .until(() -> {
                    Response response = requestSupplier.get();
                    boolean success = successCondition.test(response);
                    if (success) {
                        logger.info("{} succeeded with status: {}", description, response.getStatusCode());
                        responseRef.set(response);
                    } else {
                        logger.warn("{} retrying... Status: {}", description, response.getStatusCode());
                    }
                    return success;
                });

        return responseRef.get();
    }
}
