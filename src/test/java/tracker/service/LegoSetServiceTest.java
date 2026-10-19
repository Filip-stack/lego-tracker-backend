package tracker.service;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import tracker.dto.LegoSetRequest;
import tracker.exception.DuplicateSetException;
import tracker.exception.SetNotFoundException;
import tracker.feed.SetNumberExtractor;
import tracker.model.LegoSet;
import tracker.repository.LegoSetRepository;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
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
    @Test
    void shouldReturnAllLegoSets() {
        // given, zmyslone dane
        LegoSet dummySet = new LegoSet();
        dummySet.setSetNum("12345-1");
        dummySet.setName("testowy sokol milenium");

        when(repository.findAll()).thenReturn(List.of(dummySet));

        List<LegoSet> result = service.getAllLegoSets();
        assertEquals(1, result.size());
        assertEquals("testowy sokol milenium", result.get(0).getName());
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
