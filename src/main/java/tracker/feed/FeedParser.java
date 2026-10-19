package tracker.feed;

import org.springframework.web.client.RestClient;
import tracker.dto.StandardizedOffer;
import java.util.List;

public interface FeedParser {

    // Zwraca nazwę dostawcy (np. "SALESMASTERS_ALTO")
    String getProviderName();

    // glowna metoda ktora przyjmuje URL i klienta HTTP, a zwraca zunifikowaną listę ofert
    List<StandardizedOffer> parse(String feedUrl, RestClient restClient);

}