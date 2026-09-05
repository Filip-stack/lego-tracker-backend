package tracker.service;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import tracker.model.LegoSet;
import tracker.repository.LegoSetRepository;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;
@ExtendWith(MockitoExtension.class)
public class LegoSetServiceTest {

    @Mock
    private LegoSetRepository repository;

    private LegoSetService service;
    @BeforeEach
    void setUp() {
        service = new LegoSetService(repository, "test-api-key");
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
}
