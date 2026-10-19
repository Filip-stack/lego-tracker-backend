package tracker.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

// dane przyjmowane przy recznym dodawaniu zestawu - bez id i ofert,
// zeby klient nie mogl nadpisac istniejacego rekordu
public record LegoSetRequest(
        @NotBlank
        @Pattern(regexp = "\\d{4,7}(-\\d{1,2})?", message = "numer zestawu w formacie 75192 lub 75192-1")
        String setNum,

        @NotBlank
        String name,

        @Min(1949)
        Integer releaseYear,

        Integer themeId,

        @Min(0)
        Integer numParts,

        String setImgUrl
) {
}
