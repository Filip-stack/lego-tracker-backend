package tracker.feed;

import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Optional;

@Component
public class PriceParser {

    public Optional<BigDecimal> parse(String priceStr) {
        if (priceStr == null || priceStr.isBlank()) {
            return Optional.empty();
        }

        try {
            // 1. Oczyszczamy string ze spacji, walut i liter (zostawiamy tylko cyfry, kropki i przecinki)
            String cleanStr = priceStr.replaceAll("[^\\d.,]", "");
            if (cleanStr.isEmpty()) {
                return Optional.empty();
            }

            // 2. Szukamy ostatniego wystąpienia kropki lub przecinka w tekście
            int lastDot = cleanStr.lastIndexOf('.');
            int lastComma = cleanStr.lastIndexOf(',');

            // Indeks właściwego separatora groszy (tego, który jest najdalej po prawej)
            int decimalSeparatorIndex = Math.max(lastDot, lastComma);

            if (decimalSeparatorIndex == -1) {
                // Brak separatorów, to po prostu pełna liczba całkowita (np. "1299")
                return Optional.of(new BigDecimal(cleanStr));
            }

            // 3. Rozdzielamy część całkowitą od groszy
            // Usuwamy wszystkie kropki i przecinki z części całkowitej (separatory tysięcy)
            String integerPart = cleanStr.substring(0, decimalSeparatorIndex).replaceAll("[.,]", "");
            String fractionalPart = cleanStr.substring(decimalSeparatorIndex + 1);

            // 4. Łączymy w idealny format akceptowany przez BigDecimal
            String finalNumber = integerPart + "." + fractionalPart;

            return Optional.of(new BigDecimal(finalNumber));

        } catch (Exception e) {
            return Optional.empty();
        }
    }
}