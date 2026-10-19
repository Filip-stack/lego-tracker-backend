package tracker.service;

import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestClient;
import tracker.feed.PriceParser;
import tracker.feed.FeedParser;
import tracker.feed.SetNumberExtractor;
import tracker.model.LegoSet;
import tracker.model.StoreOffer;
import tracker.repository.LegoSetRepository;
import tracker.repository.StoreOfferRepository;
import tracker.dto.StandardizedOffer;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class AffiliateSyncService {

    private static final Logger log = LoggerFactory.getLogger(AffiliateSyncService.class);

    private final StoreOfferRepository storeOfferRepository;
    private final LegoSetRepository legoSetRepository;
    private final RestClient restClient;
    private final SetNumberExtractor setNumberExtractor;
    private final PriceParser priceParser;
    private final List<FeedParser> feedParsers;

    public AffiliateSyncService(StoreOfferRepository storeOfferRepository,
                                LegoSetRepository legoSetRepository,
                                SetNumberExtractor setNumberExtractor,
                                PriceParser priceParser, List<FeedParser> feedParsers) {
        this.storeOfferRepository = storeOfferRepository;
        this.legoSetRepository = legoSetRepository;
        this.setNumberExtractor = setNumberExtractor;
        this.priceParser = priceParser;
        this.feedParsers = feedParsers;

        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout(5000);
        requestFactory.setReadTimeout(30000);

        this.restClient = RestClient.builder()
                .requestFactory(requestFactory)
                .build();
    }

    @Scheduled(fixedRate = 86400000)
    @Transactional
    public void fetchOffersFromXml() {
        log.info("Rozpoczynam synchronizację ofert ze wszystkich źródeł...");

        for (FeedParser parser : feedParsers) {
            log.info("Uruchamiam parser: {}", parser.getProviderName());

            // Pytamy parser o gotową listę (na razie feedUrl to atrapa, bo klasa czyta z resources)
            List<StandardizedOffer> offers = parser.parse("atrapa-url", restClient);

            int successCount = 0;
            int failedCount = 0;
            int skippedCount = 0;
            int batchSize = 500;
            List<StoreOffer> batch = new ArrayList<>();

            // Zmieniamy XmlOfferDto na StandardizedOffer
            for (StandardizedOffer dto : offers) {
                try {
                    // Wywołujemy nazwane metody z naszego rekordu (dto.name(), dto.price())
                    Optional<String> extractedNum = setNumberExtractor.extract(dto.name());
                    if (extractedNum.isEmpty()) {
                        skippedCount++;
                        continue;
                    }

                    String setNumber = extractedNum.get();
                    Optional<LegoSet> optionalSet = legoSetRepository.findBySetNum(setNumber);

                    if (optionalSet.isPresent()) {
                        LegoSet set = optionalSet.get();
                        // Zauważ, że storeName też bierzemy z rekordu
                        Optional<StoreOffer> existingOffer = storeOfferRepository.findByLegoSetAndStoreName(set, dto.storeName());

                        StoreOffer offer = existingOffer.orElseGet(StoreOffer::new);
                        offer.setLegoSet(set);
                        offer.setStoreName(dto.storeName());

                        BigDecimal cleanPrice = priceParser.parse(dto.price())
                                .orElseThrow(() -> new IllegalArgumentException("Nierozpoznany format ceny: " + dto.price()));

                        offer.setPrice(cleanPrice);
                        offer.setAffiliateUrl(dto.url());
                        offer.setLastUpdated(LocalDateTime.now());

                        batch.add(offer);
                        successCount++;

                        if (batch.size() >= batchSize) {
                            storeOfferRepository.saveAll(batch);
                            batch.clear();
                        }
                    } else {
                        skippedCount++;
                    }
                } catch (Exception ex) {
                    failedCount++;
                    log.warn("Błąd przetwarzania oferty '{}': {}", dto.name(), ex.getMessage());
                }
            }

            if (!batch.isEmpty()) {
                storeOfferRepository.saveAll(batch);
            }

            log.info("Zakończono {} -> Zapisano: {}, Pominięto: {}, Błędy: {}",
                    parser.getProviderName(), successCount, skippedCount, failedCount);
        }
    }
}