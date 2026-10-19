package tracker.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

// record w Javie to automatycznie generowana klasa która służy tylko do trzymania danych
@JsonIgnoreProperties(ignoreUnknown = true)
public record RebrickableSetDto(
        @JsonProperty("set_num") String setNum,
        String name,
        @JsonProperty("year") Integer releaseYear,
        @JsonProperty("theme_id") Integer themeId,
        @JsonProperty("num_parts") Integer numParts,
        @JsonProperty("set_img_url") String setImgUrl
) {}