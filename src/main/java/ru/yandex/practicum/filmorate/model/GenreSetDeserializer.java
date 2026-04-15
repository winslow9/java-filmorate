package ru.yandex.practicum.filmorate.model;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

public class GenreSetDeserializer extends JsonDeserializer<Set<Integer>> {

    @Override
    public Set<Integer> deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
        Set<Integer> genreIds = new HashSet<>();
        JsonNode node = p.getCodec().readTree(p);

        if (node.isArray()) {
            for (JsonNode genreNode : node) {
                if (genreNode.isInt()) {
                    int genreId = genreNode.asInt();
                    // Проверяем существование жанра
                    if (Genre.fromId(genreId) == null) {
                        throw new IllegalArgumentException("Genre with id " + genreId + " not found");
                    }
                    genreIds.add(genreId);
                } else if (genreNode.isObject() && genreNode.has("id")) {
                    int genreId = genreNode.get("id").asInt();
                    // Проверяем существование жанра
                    if (Genre.fromId(genreId) == null) {
                        throw new IllegalArgumentException("Genre with id " + genreId + " not found");
                    }
                    genreIds.add(genreId);
                }
            }
        }

        return genreIds;
    }
}