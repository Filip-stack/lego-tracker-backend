package tracker.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import tracker.dto.RebrickableSetDto;
import tracker.model.LegoSet;
import tracker.repository.LegoSetRepository;

import java.util.List;

@Service
public class LegoSetService {

    private final LegoSetRepository repository;
    private final RestClient restClient;
    private final String apiKey;

    public LegoSetService(LegoSetRepository repository, @Value("${rebrickable.api.key}") String apiKey)
    {
        this.repository = repository;
        this.apiKey = apiKey;
        // wstrzykiwanie klucza z application.properties dzieki @Value
        this.restClient = RestClient.create("https://rebrickable.com/api/v3");
        // TEST DETEKTYWISTYCZNY - wypisujemy klucz do konsoli
        System.out.println("=========================================");
        System.out.println("MÓJ KLUCZ TO: ->" + this.apiKey + "<-");
        System.out.println("=========================================");

    }

    public List<LegoSet> getAllLegoSets()
    {
        return repository.findAll();
    }
    public LegoSet addLegoSet(LegoSet legoSet)
    {
        return repository.save(legoSet);
    }
    public LegoSet fetchAndSaveSet(String setNum)
    {
        RebrickableSetDto dto = restClient.get()
            .uri("/lego/sets/{setNum}/", setNum)
            .header("Authorization", "key " + apiKey)
            .retrieve()
            .body(RebrickableSetDto.class);
        if (dto == null)
        {
            throw new RuntimeException("Nie znaleziono zestawu na Rebrickable");
        }

        LegoSet legoSet = new LegoSet();
        legoSet.setSetNum(dto.setNum());
        legoSet.setName(dto.name());
        legoSet.setReleaseYear(dto.releaseYear());
        legoSet.setThemeId(dto.themeId());
        legoSet.setNumParts(dto.numParts());
        legoSet.setSetImgUrl(dto.setImgUrl());

        // zapis do bazy
        return repository.save(legoSet);

    }

    public void deleteLegoSet(Long id)
    {
        if(!repository.existsById(id))
        {
            throw new RuntimeException("Zestaw o podanym numerze nie istnieje w bazie");
        }

        repository.deleteById(id);
    }
}
