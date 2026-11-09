package tracker.feed;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import tracker.dto.AltoXmlFeedDto;
import tracker.dto.StandardizedOffer;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Component
public class SalesMastersXmlFeedParser implements FeedParser {

    private static final Logger log = LoggerFactory.getLogger(SalesMastersXmlFeedParser.class);

    // 1. Wstrzykujemy Twój kuloodporny parser cen
    private final PriceParser priceParser;

    public SalesMastersXmlFeedParser(PriceParser priceParser) {
        this.priceParser = priceParser;
    }

    @Override
    public String getProviderName() {
        return "SALESMASTERS_ALTO";
    }

    @Override
    public List<StandardizedOffer> parse(String feedUrl, RestClient restClient) {
        try {
            log.info("Rozpoczynam parsowanie lokalnego pliku testowego alto_feed.xml...");
            java.io.InputStream inputStream = new org.springframework.core.io.ClassPathResource("alto_feed.xml").getInputStream();

            XmlMapper xmlMapper = new XmlMapper();
            xmlMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

            AltoXmlFeedDto feed = xmlMapper.readValue(inputStream, AltoXmlFeedDto.class);
            List<StandardizedOffer> standardizedOffers = new ArrayList<>();

            // Liczniki do diagnostyki
            int totalOffersScanned = 0;
            int unparseablePrices = 0;

            if (feed != null && feed.getGroups() != null) {
                for (AltoXmlFeedDto.AltoGroup group : feed.getGroups()) {
                    if (group.getOffers() != null) {
                        for (AltoXmlFeedDto.AltoOffer dto : group.getOffers()) {
                            totalOffersScanned++;

                            if (dto.getCategory() != null && dto.getCategory().contains("Klocki LEGO")) {

                                // 2. Używamy PriceParsera do bezpiecznego wyciągnięcia ceny
                                Optional<BigDecimal> parsedPrice = priceParser.parse(dto.getPrice());

                                if (parsedPrice.isPresent()) {
                                    standardizedOffers.add(new StandardizedOffer(
                                            dto.getName().trim(),
                                            parsedPrice.get(), // Upewnij się, że StandardizedOffer przyjmuje BigDecimal, a nie String
                                            dto.getUrl(),
                                            "al.to"
                                    ));
                                } else {
                                    unparseablePrices++;
                                    log.warn("Pominięto zestaw LEGO: {} z powodu błędnego formatu ceny: {}", dto.getName(), dto.getPrice());
                                }
                            }
                        }
                    }
                }
            }
            log.info("Zakończono. Przeskanowano ogółem {} ofert. Zmapowano {} ofert LEGO. Odrzucono {} ofert LEGO przez błąd ceny.",
                    totalOffersScanned, standardizedOffers.size(), unparseablePrices);

            return standardizedOffers;

        } catch (Exception e) {
            log.error("Krytyczny błąd podczas parsowania feedu al.to: {}", e.getMessage());
            // 3. KRYTYCZNA ZMIANA: Rzucamy wyjątek, a nie zwracamy pustej listy!
            // Dzięki temu Spring wie, że synchronizacja fizycznie zawiodła i wycofa transakcje.
            throw new RuntimeException("Awaria parsowania lokalnego pliku alto_feed.xml", e);
        }
    }
}