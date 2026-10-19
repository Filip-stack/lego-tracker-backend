package tracker.feed;

import org.springframework.stereotype.Component;

import java.time.Year;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * wyciaga numer zestawu LEGO z tekstu pochodzacego z feedu sklepu - najczesciej
 * z tytulu oferty, czasem z pola mpn - i zwraca go w postaci uzywanej przez
 * Rebrickable, czyli z wariantem: "75192-1".
 *
 * klasa jest bezstanowa, wiec mozna ja wstrzykiwac jak zwykly bean albo tworzyc
 * przez new w testach.
 */
@Component
public class SetNumberExtractor {

    // numer zestawu to 4-7 cyfr, opcjonalnie z wariantem po myslniku (75192-1).
    // lookaroundy pilnuja, zeby nie zlapac fragmentu dluzszej liczby - bez nich
    // z kodu EAN 5702017416175 dalo by sie wyciac "7416175".
    private static final Pattern CANDIDATE =
            Pattern.compile("(?<!\\d)(\\d{4,7})(?:-(\\d{1,2}))?(?!\\d)");

    // ten sam wzorzec, ale na dopasowanie calego tekstu - do normalizacji gotowej wartosci
    private static final Pattern EXACT =
            Pattern.compile("(\\d{4,7})(?:-(\\d{1,2}))?");

    // jednostka zaraz po liczbie zdradza, ze to nie numer zestawu, a liczba
    // elementow, waga albo wymiar: "3000 elementow", "1254 el.", "48 cm"
    private static final Pattern UNIT_AFTER = Pattern.compile(
            "^\\s*(elementow|elementów|elements|element|elem|klockow|klocków|czesci|części"
                    + "|sztuk|szt|pcs|mm|cm|kg|zl|zł|pln|eur|usd)\\b",
            Pattern.CASE_INSENSITIVE);

    // rok wydania w tytule zaczyna sie realnie od tego rocznika
    private static final int MIN_YEAR = 1995;

    /**
     * szuka numeru zestawu w dowolnym tekscie z feedu.
     *
     * @return numer w postaci "75192-1" albo pusty Optional, gdy nic sensownego nie ma
     */
    public Optional<String> extract(String text) {
        if (text == null || text.isBlank()) {
            return Optional.empty();
        }

        int maxYear = Year.now().getValue() + 1;
        Matcher matcher = CANDIDATE.matcher(text);
        String yearLikeFallback = null;

        while (matcher.find()) {
            String number = matcher.group(1);
            String variant = matcher.group(2);

            // liczba elementow nigdy nie jest numerem zestawu - odrzucamy na twardo
            if (followedByUnit(text, matcher.end())) {
                continue;
            }

            // jawny wariant ("75192-1") to pewny numer zestawu, rok tak nie wyglada
            if (variant != null || !looksLikeYear(number, maxYear)) {
                return Optional.of(canonical(number, variant));
            }

            // "LEGO Star Wars 2023 75355 X-wing" - 2023 to rocznik, nie numer.
            // ale istnieja tez zestawy o numerach z tego zakresu, wiec zapamietujemy
            // kandydata na wypadek, gdyby nic lepszego w tytule nie bylo.
            if (yearLikeFallback == null) {
                yearLikeFallback = canonical(number, null);
            }
        }

        return Optional.ofNullable(yearLikeFallback);
    }

    /**
     * probuje kolejnych pol feedu w podanej kolejnosci i zwraca pierwszy trafiony numer.
     * w praktyce warto podac najpierw mpn (czesto jest to czysty numer zestawu),
     * a dopiero potem tytul oferty.
     */
    public Optional<String> extractFrom(String... sources) {
        if (sources == null) {
            return Optional.empty();
        }

        for (String source : sources) {
            Optional<String> found = extract(source);
            if (found.isPresent()) {
                return found;
            }
        }

        return Optional.empty();
    }

    /**
     * doprowadza gotowa wartosc do postaci Rebrickable: "75192" -> "75192-1".
     * w przeciwienstwie do extract wymaga, zeby caly tekst byl numerem zestawu.
     */
    public Optional<String> normalize(String rawSetNumber) {
        if (rawSetNumber == null) {
            return Optional.empty();
        }

        Matcher matcher = EXACT.matcher(rawSetNumber.trim());
        if (!matcher.matches()) {
            return Optional.empty();
        }

        return Optional.of(canonical(matcher.group(1), matcher.group(2)));
    }

    // brak wariantu w feedzie oznacza wersje podstawowa, czyli "-1" u Rebrickable.
    // parseInt obcina zera wiodace, zeby "-01" nie rozjechalo sie z "-1"
    private String canonical(String number, String variant) {
        int resolvedVariant = variant == null ? 1 : Integer.parseInt(variant);
        return number + "-" + resolvedVariant;
    }

    private boolean followedByUnit(String text, int matchEnd) {
        return UNIT_AFTER.matcher(text.substring(matchEnd)).find();
    }

    private boolean looksLikeYear(String number, int maxYear) {
        if (number.length() != 4) {
            return false;
        }

        int value = Integer.parseInt(number);
        return value >= MIN_YEAR && value <= maxYear;
    }
}
