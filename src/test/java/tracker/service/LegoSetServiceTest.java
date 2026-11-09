package tracker.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import tracker.dto.LegoSetRequest;
import tracker.exception.DuplicateSetException;
import tracker.exception.SetNotFoundException;
import tracker.feed.SetNumberExtractor;
import tracker.model.LegoSet;
import tracker.repository.LegoSetRepository;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull; // <-- Dodany brakujący import
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class LegoSetServiceTest {

    @Mock
    private LegoSetRepository repository;

    private LegoSetService service;

    @BeforeEach
    void setUp() {
        service = new LegoSetService(repository, new SetNumberExtractor(), "test-api-key");
    }

    @Test // <-- Poprawiono zduplikowane @Test @Test
    void shouldReturnAllLegoSets() {
        // Przygotowanie danych
        LegoSet legoSet = new LegoSet();
        legoSet.setSetNum("75192-1");

        // Tworzymy fałszywe żądanie paginacji i stronę wyników z jednym zestawem
        Pageable pageable = PageRequest.of(0, 20);
        Page<LegoSet> mockPage = new PageImpl<>(List.of(legoSet));

        // Uczymy mocka repozytorium, aby zwracał stronę
        when(repository.findAll(pageable)).thenReturn(mockPage);

        // Wywołanie metody w serwisie (używamy zmiennej 'service', a nie 'legoSetService')
        Page<LegoSet> result = service.getAllLegoSets(pageable);

        // Asercje - wyciągamy listę ze strony za pomocą .getContent()
        assertNotNull(result);
        assertEquals(1, result.getContent().size());
        assertEquals("75192-1", result.getContent().get(0).getSetNum());

        // Weryfikujemy, czy repozytorium faktycznie zostało wywołane z obiektem pageable
        verify(repository).findAll(pageable);
    }

    @Test
    void shouldSaveSetNumWithVariant() {
        when(repository.findBySetNum("75192-1")).thenReturn(Optional.empty());
        when(repository.save(any(LegoSet.class))).thenAnswer(inv -> inv.getArgument(0));

        LegoSet saved = service.addLegoSet(new LegoSetRequest("75192", "Sokol Millennium", 2017, 158, 7541, null));

        // ten sam format, ktory zwraca SetNumberExtractor przy synchronizacji ofert
        assertEquals("75192-1", saved.getSetNum());
    }

    @Test
    void shouldRejectDuplicateSet() {
        when(repository.findBySetNum("75192-1")).thenReturn(Optional.of(new LegoSet()));

        assertThrows(DuplicateSetException.class,
                () -> service.addLegoSet(new LegoSetRequest("75192-1", "Sokol Millennium", null, null, null, null)));
        verify(repository, never()).save(any());
    }

    @Test
    void shouldThrowWhenDeletingMissingSet() {
        when(repository.existsById(42L)).thenReturn(false);

        assertThrows(SetNotFoundException.class, () -> service.deleteLegoSet(42L));
        verify(repository, never()).deleteById(any());
    }
}