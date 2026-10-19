package tracker.feed;

import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class PriceParserTest {

    // Tworzymy instancję naszego parsera do testów
    private final PriceParser parser = new PriceParser();

    @Test
    void shouldParseStandardFormat() {
        Optional<BigDecimal> result1 = parser.parse("129.99");
        Optional<BigDecimal> result2 = parser.parse("1299");

        assertTrue(result1.isPresent());
        assertEquals(new BigDecimal("129.99"), result1.get());

        assertTrue(result2.isPresent());
        assertEquals(new BigDecimal("1299"), result2.get());
    }

    @Test
    void shouldParsePolishFormatWithCurrency() {
        // Sklepy często dodają twarde spacje i dopiski "zł" lub "PLN"
        assertEquals(new BigDecimal("1299.99"), parser.parse("1 299,99 zł").get());
        assertEquals(new BigDecimal("349.50"), parser.parse("349,50 PLN").get());
        assertEquals(new BigDecimal("99.99"), parser.parse("99,99zł").get());
    }

    @Test
    void shouldHandleThousandsSeparators() {
        // Ekstremalne przypadki: kropki tysięcy mieszane z kropkami/przecinkami ułamków
        assertEquals(new BigDecimal("1299.99"), parser.parse("1.299.99").get());
        assertEquals(new BigDecimal("1299.99"), parser.parse("1.299,99 zł").get());
        assertEquals(new BigDecimal("15299.99"), parser.parse("15.299,99").get());
    }

    @Test
    void shouldReturnEmptyForInvalidData() {
        // Symulacja totalnie zepsutych danych w XML (np. błąd mapowania po stronie sklepu)
        assertTrue(parser.parse("brak").isEmpty());
        assertTrue(parser.parse("zapytaj o cenę").isEmpty());
        assertTrue(parser.parse("").isEmpty());
        assertTrue(parser.parse(null).isEmpty());
    }
}