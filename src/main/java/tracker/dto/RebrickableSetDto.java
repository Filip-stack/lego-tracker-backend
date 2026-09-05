package tracker.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

// Record w Javie to automatycznie generowana klasa, która służy tylko do trzymania danych.
public record RebrickableSetDto(
        @JsonProperty("set_num") String setNum,
        String name,
        @JsonProperty("year") Integer releaseYear,
        @JsonProperty("theme_id") Integer themeId,
        @JsonProperty("num_parts") Integer numParts,
        @JsonProperty("set_img_url") String setImgUrl
) {}