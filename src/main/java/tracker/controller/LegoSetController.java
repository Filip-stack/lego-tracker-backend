package tracker.controller;


import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tracker.dto.LegoSetRequest;
import tracker.model.LegoSet;
import tracker.service.AffiliateSyncService;
import tracker.service.LegoSetService;
import java.util.List;

@RestController
@RequestMapping("/api/lego-sets")
public class LegoSetController {
    private final LegoSetService legoSetService;
    private final AffiliateSyncService affiliateSyncService;

    public LegoSetController(LegoSetService legoSetService, AffiliateSyncService affiliateSyncService) {
        this.legoSetService = legoSetService;
        this.affiliateSyncService = affiliateSyncService;
    }

    // endpoint do pobierania wszystkich zestawow
    @GetMapping
    public List<LegoSet> findAll() {
        return legoSetService.getAllLegoSets();
    }

    @GetMapping("/{id}")
    public LegoSet findById(@PathVariable Long id) {
        return legoSetService.getLegoSet(id);
    }

    // endpoint do dodawania nowego zestawu
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public LegoSet addLegoSet(@Valid @RequestBody LegoSetRequest request) {
        return legoSetService.addLegoSet(request);
    }

    // Endpoint do automatycznego pobierania zestawu po jego numerze
    @PostMapping("/fetch/{setNum}")
    public LegoSet fetchFromRebrickable(
            @PathVariable
            @Pattern(regexp = "\\d{4,7}(-\\d{1,2})?", message = "numer zestawu w formacie 75192 lub 75192-1")
            String setNum) {
        return legoSetService.fetchAndSaveSet(setNum);
    }

    @PostMapping("/fetch-star-wars")
    public ResponseEntity<String> fetchStarWarsSets() {
        legoSetService.fetchStarWarsSets();
        return ResponseEntity.ok("Zakończono pobieranie zestawów Star Wars z API");
    }

    // mozliwosc synchronizacji ofert z feedu na żądanie
    @PostMapping("/sync")
    public ResponseEntity<String> triggerSync() {
        affiliateSyncService.fetchOffersFromXml();
        return ResponseEntity.ok("Synchronizacja ofert z feedu zakończona");
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteLegoSet(@PathVariable Long id) {
        legoSetService.deleteLegoSet(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/search")
    public List<LegoSet> searchLegoSets(@RequestParam @NotBlank String name) {
        return legoSetService.searchLegoSetsByName(name.trim());
    }
}
