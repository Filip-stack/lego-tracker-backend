package tracker.feed;

import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SetNumberExtractorTest {

    private final SetNumberExtractor extractor = new SetNumberExtractor();

    @Test
    void shouldExtractNumberFromRealShopTitles() {
        assertEquals(Optional.of("75192-1"),
                extractor.extract("LEGO Star Wars 75192 Sokol Millennium"));
        assertEquals(Optional.of("60380-1"),
                extractor.extract("Klocki LEGO® City 60380 Centrum miasta"));
        assertEquals(Optional.of("42143-1"),
                extractor.extract("LEGO Technic 42143 Ferrari Daytona SP3 (nowe, MISB)"));
        assertEquals(Optional.of("31058-1"),
                extractor.extract("LEGO Creator 3w1 31058 Potezne dinozaury"));
        assertEquals(Optional.of("10954-1"),
                extractor.extract("LEGO Duplo 10954 Pociag z cyferkami - nauka liczenia"));
    }

    @Test
    void shouldExtractOldFourDigitNumber() {
        assertEquals(Optional.of("6080-1"), extractor.extract("LEGO 6080 King's Castle"));
    }

    @Test
    void shouldKeepExplicitVariant() {
        assertEquals(Optional.of("75192-2"), extractor.extract("LEGO 75192-2 Sokol Millennium"));
        assertEquals(Optional.of("10497-1"), extractor.extract("LEGO Icons 10497-01 Galaxy Explorer"));
    }

    @Test
    void shouldIgnoreEanCode() {
        assertEquals(Optional.of("60380-1"),
                extractor.extract("EAN 5702017416175 Klocki LEGO City 60380"));
    }

    @Test
    void shouldPreferSetNumberOverReleaseYear() {
        assertEquals(Optional.of("75355-1"),
                extractor.extract("LEGO Star Wars 2023 75355 X-wing Starfighter"));
    }

    @Test
    void shouldFallBackToYearLikeNumberWhenItIsTheOnlyCandidate() {
        // zestaw 2000 faktycznie istnieje, wiec gdy nie ma innego kandydata - bierzemy go
        assertEquals(Optional.of("2000-1"), extractor.extract("LEGO 2000 zestaw kolekcjonerski"));
    }

    @Test
    void shouldIgnorePieceCount() {
        assertEquals(Optional.of("10294-1"),
                extractor.extract("Zestaw klockow, 3000 elementow, LEGO 10294 Titanic"));
        assertEquals(Optional.of("10497-1"),
                extractor.extract("LEGO Icons 10497 Galaxy Explorer, 1254 el."));
    }

    @Test
    void shouldReturnEmptyWhenTitleHasNoSetNumber() {
        assertTrue(extractor.extract("Klocki LEGO - zestaw prezentowy").isEmpty());
        assertTrue(extractor.extract("").isEmpty());
        assertTrue(extractor.extract(null).isEmpty());
    }

    @Test
    void shouldPreferEarlierSourceInExtractFrom() {
        assertEquals(Optional.of("75192-1"),
                extractor.extractFrom("75192", "LEGO Star Wars 60380 inny zestaw"));
        assertEquals(Optional.of("60380-1"),
                extractor.extractFrom(null, "brak numeru", "LEGO City 60380 Centrum miasta"));
    }

    @Test
    void shouldNormalizeRawNumber() {
        assertEquals(Optional.of("75192-1"), extractor.normalize("75192"));
        assertEquals(Optional.of("75192-1"), extractor.normalize("  75192-1 "));
        assertEquals(Optional.of("75192-2"), extractor.normalize("75192-2"));
    }

    @Test
    void shouldRejectGarbageInNormalize() {
        assertTrue(extractor.normalize("LEGO 75192").isEmpty());
        assertTrue(extractor.normalize("5702017416175").isEmpty());
        assertTrue(extractor.normalize(null).isEmpty());
    }
}
