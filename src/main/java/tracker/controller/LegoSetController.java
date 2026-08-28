package tracker.controller;


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

}
