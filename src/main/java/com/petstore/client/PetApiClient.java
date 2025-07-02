package com.petstore.client;

import com.petstore.pojo.Pet;
import io.restassured.response.ExtractableResponse;
import io.restassured.response.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PetApiClient extends InitApiClient {
    private static final Logger logger = LoggerFactory.getLogger(PetApiClient.class);
    private static final String BASE = "/pet";

    public Response addPet(Pet pet) {
        logger.debug("Adding pet with body: {}", pet.toString());
        return post(BASE, pet);
    }

    public Response getPetById(Long petId) {
        logger.debug("Retrieving pet with id: {}", petId);
        return get(BASE + "/" + petId);
    }

    public Response updatePet(Pet pet) {
        logger.debug("Updating pet with body: {}", pet.toString());
        return put(BASE, pet);
    }

    public Response deletePet(Long petId) {
        logger.debug("Deleting pet with id: {}", petId);
        return delete(BASE + "/" + petId);
    }

    public Response findPetsByStatus(String status) {
        logger.debug("Finding pets by status: {}", status);
        return get(BASE + "/findByStatus?status=" + status);
    }

    public Response updatePetWithFormData(Long petId, String name, String status) {
        logger.debug("Updating pet with id: {}, name: {}, status: {}", petId, name, status);
        Map<String, String> formParams = new HashMap<>();
        if (name != null) {
            formParams.put("name", name);
        }
        if (status != null) {
            formParams.put("status", status);
        }
        return post(BASE + "/" + petId, formParams);
    }


    public <T> T deserializeResponse(Response response, Class<T> clazz) {
        try {
            return mapper.readValue(response.asString(), clazz);
        } catch (IOException e) {
            logger.error("Failed to deserialize response: {}", e.getMessage());
            throw new RuntimeException("Failed to deserialize response", e);
        }
    }

    public List<Pet> deserializePetListResponse(Response response) {
        try {
            return mapper.readValue(response.asString(), mapper.getTypeFactory().constructCollectionType(List.class, Pet.class));
        } catch (IOException e) {
            logger.error("Failed to deserialize pet list response: {}", e.getMessage());
            throw new RuntimeException("Failed to deserialize pet list response", e);
        }
    }
}
