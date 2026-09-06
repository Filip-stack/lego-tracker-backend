package tracker.service;
import org.apache.catalina.Store;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import tracker.repository.LegoSetRepository;
import tracker.repository.StoreOfferRepository;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;
import tracker.dto.XmlFeedDto;
import tracker.dto.XmlOfferDto;
import tracker.model.LegoSet;
import tracker.model.StoreOffer;
import org.springframework.web.client.RestClient;

@Service
public class AffiliateSyncService {

    private final StoreOfferRepository storeOfferRepository;
    private final LegoSetRepository legoSetRepository;

    public AffiliateSyncService(StoreOfferRepository storeOfferRepository, LegoSetRepository legoSetRepository) {
        this.storeOfferRepository = storeOfferRepository;
        this.legoSetRepository = legoSetRepository;
    }

    @Scheduled(fixedRate = 360000)
    public void fetchOffersFromXml()
    {

        String feedUrl = "adres-z-sieci";
        String downloadedXml = "";
        try {

            System.out.println("Łączę się z serwerem sklepu...");
            RestClient restClient = RestClient.create();
            downloadedXml = restClient.get()
                    .uri(feedUrl)
                    .retrieve()
                    .body(String.class);
            System.out.println("Plik pobrany pomyślnie!");
            XmlMapper xmlMapper = new XmlMapper();
            XmlFeedDto feed = xmlMapper.readValue(downloadedXml, XmlFeedDto.class);

            // przetwarzanie znalezionej oferty
            for (XmlOfferDto dto : feed.getOffers()) {
                // szukanie zestawu w bazie po numerze z xml
                Optional<LegoSet> optionalSet = legoSetRepository.findBySetNum(dto.getSetNumber());

                if (optionalSet.isPresent()) {
                    LegoSet set = optionalSet.get();

                    Optional<StoreOffer> existingOffer = storeOfferRepository.findByLegoSetAndStoreName(set, dto.getStoreName());

                    StoreOffer offer;
                    if (existingOffer.isPresent()) {
                        // pobieranie istniejacej oferty
                        offer = existingOffer.get();
                        System.out.println("aktualizuje istniejaca oferte dla" + set.getName() + " (" + dto.getStoreName() + ")");

                    }
                    else {
                        // dodanie nowej oferty
                        offer = new StoreOffer();
                        offer.setLegoSet(set);
                        offer.setStoreName(dto.getStoreName());
                        System.out.println("dodaje nowa oferte dla: " + set.getName() + dto.getStoreName());
                    }

                    offer.setPrice(new BigDecimal(dto.getPrice()));
                    offer.setAffiliateUrl(dto.getUrl());
                    offer.setLastUpdated(LocalDateTime.now());

                    storeOfferRepository.save(offer);
                    System.out.println("Zapisano nową ofertę dla: " + set.getName());
                }
                else
                {
                    System.out.println("Zestaw " + dto.getSetNumber() + " nie istnieje w bazie. Pomijam ofertę.");
                }
            }
        } catch (Exception ex) {
            System.err.println("Błąd przetwarazania XML " + ex.getMessage());
        }


    }


}
