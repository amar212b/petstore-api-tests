package com.petstore.pojo;

public record Order(
        Long id,
        Integer petId,
        Integer quantity,
        String shipDate,
        String status,
        Boolean complete
        ) {}

