package com.serkowski.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;

public record MovieRecommendation(
        @JsonPropertyDescription("Here should be a movie title") String title,
        @JsonProperty(required = true) String genre,
        String description,
        String director,
        int releaseYear,
        double rating) {
}
