package com.petstore.pojo;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

public record Pet(
        @JsonProperty("id") Long id,
        @JsonProperty("category") Category category,
        @JsonProperty("name") String name,
        @JsonProperty("photoUrls") List<String> photoUrls,
        @JsonProperty("tags") List<Tag> tags,
        @JsonProperty("status") String status
) {
    public record Category(
            @JsonProperty("id") Long id,
            @JsonProperty("name") String name
    ) {}

    public record Tag(
            @JsonProperty("id") Long id,
            @JsonProperty("name") String name
    ) {}
}