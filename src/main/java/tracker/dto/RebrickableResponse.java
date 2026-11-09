package tracker.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record RebrickableResponse(
        String next,
        List<RebrickableSetDto> results
) {
}