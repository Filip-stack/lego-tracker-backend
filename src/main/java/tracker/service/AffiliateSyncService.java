package tracker.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestClient;
import tracker.feed.FeedParser;
import tracker.feed.SetNumberExtractor;
import tracker.model.LegoSet;
import tracker.model.StoreOffer;
import tracker.repository.LegoSetRepository;
import tracker.repository.StoreOfferRepository;
import tracker.dto.StandardizedOffer;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.LinkedHashMap;

@Service
public class AffiliateSyncService {

    private static final Logger log = LoggerFactory.getLogger(AffiliateSyncService.class);

    private final StoreOfferRepository storeOfferRepository;
    private final LegoSetRepository legoSetRepository;
    private final RestClient restClient;
    private final SetNumberExtractor setNumberExtractor;
    private final List<FeedParser> feedParsers;

    public AffiliateSyncService(StoreOfferRepository storeOfferRepository,
                                LegoSetRepository legoSetRepository,
                                SetNumberExtractor setNumberExtractor,
                                List<FeedParser> feedParsers) {
        this.storeOfferRepository = storeOfferRepository;
        this.legoSetRepository = legoSetRepository;
        this.setNumberExtractor = setNumberExtractor;
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
    public synchronized void fetchOffersFromXml() {
        log.info("Rozpoczynam zoptymalizowaną synchronizację ofert ze wszystkich źródeł...");

        for (FeedParser parser : feedParsers) {
            log.info("Uruchamiam parser: {}", parser.getProviderName());
            String providerName = parser.getProviderName();

            List<StandardizedOffer> rawOffers = parser.parse("atrapa-url", restClient);
            if (rawOffers == null || rawOffers.isEmpty()) {
                log.info("Brak ofert z parsera: {}", providerName);
                continue;
            }

            // KROK 1: Wyciągamy numery zestawów i mapujemy surowe oferty do sparsowanych rekordów
            record ParsedItem(StandardizedOffer dto, String setNumber, String storeName) {}
            record OfferKey(String setNumber, String storeName) {}
            List<ParsedItem> validItems = new ArrayList<>();
            Set<String> extractedSetNums = new HashSet<>();
            Set<String> storeNames = new HashSet<>();

            int skippedCount = 0;
            for (StandardizedOffer dto : rawOffers) {
                if (dto == null || dto.price() == null || dto.storeName() == null || dto.storeName().isBlank()) {
                    skippedCount++;
                    continue;
                }
                Optional<String> extractedNum = setNumberExtractor.extract(dto.name());
                if (extractedNum.isPresent()) {
                    String setNum = extractedNum.get();
                    extractedSetNums.add(setNum);
                    storeNames.add(dto.storeName());
                    validItems.add(new ParsedItem(dto, setNum, dto.storeName()));
                } else {
                    skippedCount++;
                }
            }

            // KROK 2: Masowe pobranie zestawów LEGO z bazy jednym zapytaniem (SQL IN)
            if (validItems.isEmpty()) {
                log.info("Brak poprawnych ofert do przetworzenia z parsera: {}", providerName);
                continue;
            }

            Map<String, LegoSet> legoSetMap = legoSetRepository.findBySetNumIn(extractedSetNums)
                    .stream()
                    .collect(java.util.stream.Collectors.toMap(LegoSet::getSetNum, set -> set));

            // KROK 3: Masowe pobranie istniejących ofert dla tego sklepu jednym zapytaniem
            Map<OfferKey, StoreOffer> existingOffersMap = storeOfferRepository
                    .findByStoreNameInAndLegoSet_SetNumIn(storeNames, extractedSetNums)
                    .stream()
                    .collect(java.util.stream.Collectors.toMap(
                            offer -> new OfferKey(offer.getLegoSet().getSetNum(), offer.getStoreName()),
                            offer -> offer,
                            (existing, duplicate) -> existing));

            int successCount = 0;
            int failedCount = 0;
            int batchSize = 500;
            Map<OfferKey, StoreOffer> batch = new LinkedHashMap<>();

            // KROK 4: Przetwarzanie w pamięci (Zero dodatkowych zapytań do bazy w pętli!)
            for (ParsedItem item : validItems) {
                try {
                    LegoSet legoSet = legoSetMap.get(item.setNumber());
                    if (legoSet == null) {
                        skippedCount++;
                        continue; // Zestaw nie istnieje w bazie
                    }

                    OfferKey key = new OfferKey(item.setNumber(), item.storeName());
                    StoreOffer offer = existingOffersMap.get(key);
                    if (offer == null) {
                        offer = new StoreOffer();
                        offer.setLegoSet(legoSet);
                        offer.setStoreName(item.storeName());
                        existingOffersMap.put(key, offer);
                    }

                    offer.setPrice(item.dto().price());
                    offer.setAffiliateUrl(item.dto().url());
                    offer.setLastUpdated(LocalDateTime.now());

                    batch.put(key, offer);
                    successCount++;

                    if (batch.size() >= batchSize) {
                        storeOfferRepository.saveAll(new ArrayList<>(batch.values()));
                        batch.clear();
                    }
                } catch (Exception ex) {
                    failedCount++;
                    log.warn("Błąd przetwarzania oferty '{}': {}", item.dto().name(), ex.getMessage());
                }
            }

            if (!batch.isEmpty()) {
                storeOfferRepository.saveAll(new ArrayList<>(batch.values()));
            }

            log.info("Zakończono {} -> Zapisano: {}, Pominięto: {}, Błędy: {}",
                    providerName, successCount, skippedCount, failedCount);
        }
    }
}
