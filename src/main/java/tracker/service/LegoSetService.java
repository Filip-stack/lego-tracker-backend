package tracker.service;

import org.springframework.stereotype.Service;
import tracker.model.LegoSet;
import tracker.repository.LegoSetRepository;
import java.util.List;

@Service
public class LegoSetService {
    private final LegoSetRepository repository;

    public LegoSetService(LegoSetRepository repository) {
        this.repository = repository;
    }
    public List<LegoSet> getAllLegoSets() {
        return repository.findAll();
    }
    public LegoSet addLegoSet(LegoSet LegoSet)
    {
        return repository.save(LegoSet);
    }
}
