package tracker.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import tracker.dto.StandardizedOffer;
import tracker.feed.FeedParser;
import tracker.feed.PriceParser;
import tracker.feed.SetNumberExtractor;
import tracker.model.LegoSet;
import tracker.model.StoreOffer;
import tracker.repository.LegoSetRepository;
import tracker.repository.StoreOfferRepository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AffiliateSyncServiceTest {

    @Mock
    private StoreOfferRepository storeOfferRepository;
    @Mock
    private LegoSetRepository legoSetRepository;
    @Mock
    private FeedParser feedParser;

    private AffiliateSyncService service;

    @BeforeEach
    void setUp() {
        when(feedParser.getProviderName()).thenReturn("TEST");
        service = new AffiliateSyncService(storeOfferRepository, legoSetRepository,
                new SetNumberExtractor(), new PriceParser(), List.of(feedParser));
    }

    @Test
    @SuppressWarnings("unchecked")
    void shouldMatchOfferToSetStoredInDatabase() {
        LegoSet falcon = new LegoSet();
        falcon.setSetNum("75192-1");

        when(feedParser.parse(anyString(), any())).thenReturn(List.of(
                new StandardizedOffer("LEGO Star Wars 75192 Sokół Millennium", "3 499,99 zł", "https://al.to/75192", "al.to")
        ));
        // numer z tytulu oferty musi trafic w format, w jakim zestaw lezy w bazie
        when(legoSetRepository.findBySetNum("75192-1")).thenReturn(Optional.of(falcon));
        when(storeOfferRepository.findByLegoSetAndStoreName(falcon, "al.to")).thenReturn(Optional.empty());

        service.fetchOffersFromXml();

        ArgumentCaptor<List<StoreOffer>> captor = ArgumentCaptor.forClass(List.class);
        verify(storeOfferRepository).saveAll(captor.capture());

        List<StoreOffer> saved = captor.getValue();
        assertEquals(1, saved.size());
        assertSame(falcon, saved.get(0).getLegoSet());
        assertEquals(new BigDecimal("3499.99"), saved.get(0).getPrice());
        assertEquals("al.to", saved.get(0).getStoreName());
    }

    @Test
    void shouldSkipOfferWhenSetIsNotInDatabase() {
        when(feedParser.parse(anyString(), any())).thenReturn(List.of(
                new StandardizedOffer("LEGO Technic 42115 Lamborghini", "1499,00 zł", "https://al.to/42115", "al.to")
        ));
        when(legoSetRepository.findBySetNum("42115-1")).thenReturn(Optional.empty());

        service.fetchOffersFromXml();

        verify(storeOfferRepository, never()).saveAll(any());
    }
}
