package tracker.dto;

import java.math.BigDecimal;

public record StandardizedOffer(
        String name,
        BigDecimal price,
        String url,
        String storeName
) {
}
