package tracker.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestClient;
import tracker.dto.LegoSetRequest;
import tracker.dto.RebrickableResponse;
import tracker.exception.DuplicateSetException;
import tracker.exception.SetNotFoundException;
import tracker.dto.RebrickableSetDto;
import tracker.feed.SetNumberExtractor;
import tracker.model.LegoSet;
import tracker.repository.LegoSetRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class LegoSetService {

    private static final Logger log = LoggerFactory.getLogger(LegoSetService.class);

    private final LegoSetRepository repository;
    private final SetNumberExtractor setNumberExtractor;
    private final RestClient restClient;
    private final String apiKey;

    public LegoSetService(LegoSetRepository repository,
                          SetNumberExtractor setNumberExtractor,
                          @Value("${rebrickable.api.key}") String apiKey) {
        this.repository = repository;
        this.setNumberExtractor = setNumberExtractor;
        this.apiKey = apiKey;
        this.restClient = RestClient.create("https://rebrickable.com/api/v3");
    }

    public List<LegoSet> getAllLegoSets() {
        return repository.findAll();
    }

    public LegoSet getLegoSet(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new SetNotFoundException("Zestaw o id " + id + " nie istnieje w bazie"));
    }

    public LegoSet addLegoSet(LegoSetRequest request) {
        String setNum = toCanonical(request.setNum());
        if (repository.findBySetNum(setNum).isPresent()) {
            throw new DuplicateSetException("Zestaw " + setNum + " jest juz w bazie");
        }

        LegoSet legoSet = new LegoSet();
        legoSet.setSetNum(setNum);
        legoSet.setName(request.name());
        legoSet.setReleaseYear(request.releaseYear());
        legoSet.setThemeId(request.themeId());
        legoSet.setNumParts(request.numParts() != null ? request.numParts() : 0);
        legoSet.setSetImgUrl(request.setImgUrl());

        return repository.save(legoSet);
    }

    // w bazie trzymamy numer w formacie Rebrickable ("75192-1") - tak samo jak
    // zwraca go SetNumberExtractor przy synchronizacji ofert z feedow
    private String toCanonical(String setNum) {
        return setNumberExtractor.normalize(setNum)
                .orElseThrow(() -> new IllegalArgumentException("Niepoprawny numer zestawu: " + setNum));
    }

    public LegoSet fetchAndSaveSet(String setNum) {
        String apiSetNum = toCanonical(setNum);

        // Unikamy duplikatów
        Optional<LegoSet> existingSet = repository.findBySetNum(apiSetNum);
        if (existingSet.isPresent()) {
            return existingSet.get();
        }

        RebrickableSetDto dto = restClient.get()
                .uri("/lego/sets/{setNum}/", apiSetNum)
                .header("Authorization", "key " + apiKey)
                .retrieve()
                .body(RebrickableSetDto.class);

        if (dto == null) {
            throw new RuntimeException("Nie znaleziono zestawu na Rebrickable: " + apiSetNum);
        }

        LegoSet legoSet = new LegoSet();
        legoSet.setSetNum(apiSetNum);
        legoSet.setName(dto.name());
        legoSet.setReleaseYear(dto.releaseYear());
        legoSet.setThemeId(dto.themeId());
        legoSet.setNumParts(dto.numParts() != null ? dto.numParts() : 0);
        legoSet.setSetImgUrl(dto.setImgUrl());

        return repository.save(legoSet);
    }

    // NOWA METODA: Masowe pobieranie zestawów Star Wars
    @Transactional
    public void fetchStarWarsSets() {
        log.info("Rozpoczynam masowe pobieranie zestawów Star Wars z API...");

        try {
            RebrickableResponse response = restClient.get()
                    .uri("/lego/sets/?theme_id=158&page_size=1000")
                    .header("Authorization", "key " + apiKey)
                    .retrieve()
                    .body(RebrickableResponse.class);

            if (response != null && response.results() != null) {
                List<LegoSet> setsToSave = new ArrayList<>();

                for (RebrickableSetDto dto : response.results()) {
                    // Rebrickable zwraca juz numer w docelowym formacie, np. "75192-1"
                    String setNum = dto.setNum();

                    if (repository.findBySetNum(setNum).isEmpty()) {
                        LegoSet legoSet = new LegoSet();
                        legoSet.setSetNum(setNum);
                        legoSet.setName(dto.name());
                        legoSet.setReleaseYear(dto.releaseYear());
                        legoSet.setThemeId(dto.themeId());
                        legoSet.setNumParts(dto.numParts() != null ? dto.numParts() : 0);
                        legoSet.setSetImgUrl(dto.setImgUrl());

                        setsToSave.add(legoSet);
                    }
                }

                if (!setsToSave.isEmpty()) {
                    repository.saveAll(setsToSave);
                    log.info("Zapisano {} nowych zestawów Star Wars!", setsToSave.size());
                } else {
                    log.info("Wszystkie zestawy Star Wars są już w bazie.");
                }
            }
        } catch (Exception e) {
            log.error("Błąd podczas pobierania zestawów Star Wars: {}", e.getMessage());
        }
    }

    public void deleteLegoSet(Long id) {
        if (!repository.existsById(id)) {
            throw new SetNotFoundException("Zestaw o id " + id + " nie istnieje w bazie");
        }
        repository.deleteById(id);
    }

    public List<LegoSet> searchLegoSetsByName(String keyword) {
        return repository.findByNameContainingIgnoreCase(keyword);
    }
}