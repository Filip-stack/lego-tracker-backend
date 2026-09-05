package tracker.controller;


import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tracker.model.LegoSet;
import tracker.service.LegoSetService;
import java.util.List;

@RestController
@RequestMapping("/api/lego-sets")
public class LegoSetController {
    private final LegoSetService legoSetService;

    public LegoSetController(LegoSetService legoSetService) {
        this.legoSetService = legoSetService;
    }

    // endpoint do pobierania wszystkich zestawow
    @GetMapping
    public List<LegoSet> findAll() {
        return legoSetService.getAllLegoSets();
    }
    // endpoint do dodawania nowego zestawu
    @PostMapping
    public LegoSet addLegoSet(@RequestBody LegoSet legoSet) {
        return legoSetService.addLegoSet(legoSet);
    }

    // Endpoint do automatycznego pobierania zestawu po jego numerze
    @PostMapping("/fetch/{setNum}")
    public LegoSet fetchFromRebrickable(@PathVariable String setNum) {
        return legoSetService.fetchAndSaveSet(setNum);
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<Void> deleteLegoSet(@PathVariable("id") Long id) {
        legoSetService.deleteLegoSet(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/search")
    public ResponseEntity<List<LegoSet>> searchLegoSets(@RequestParam String name) {
        List<LegoSet> foundSets = legoSetService.searchLegoSetsByName(name);
        return ResponseEntity.ok(foundSets);
    }
}
