package tracker.feed;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import tracker.dto.AltoXmlFeedDto;
import tracker.dto.StandardizedOffer;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Component
public class SalesMastersXmlFeedParser implements FeedParser {

    private static final Logger log = LoggerFactory.getLogger(SalesMastersXmlFeedParser.class);

    @Override
    public String getProviderName() {
        return "SALESMASTERS_ALTO";
    }

    @Override
    public List<StandardizedOffer> parse(String feedUrl, RestClient restClient) {
        try {
            // Tymczasowo czytamy plik z dysku
            java.io.InputStream inputStream = new org.springframework.core.io.ClassPathResource("alto_feed.xml").getInputStream();

            XmlMapper xmlMapper = new XmlMapper();
            // Wyłączamy sypanie błędami przy nieznanych atrybutach (np. weight, avail)
            xmlMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

            AltoXmlFeedDto feed = xmlMapper.readValue(inputStream, AltoXmlFeedDto.class);
            List<StandardizedOffer> standardizedOffers = new ArrayList<>();

            if (feed != null && feed.getGroups() != null) {
                for (AltoXmlFeedDto.AltoGroup group : feed.getGroups()) {
                    if (group.getOffers() != null) {
                        for (AltoXmlFeedDto.AltoOffer dto : group.getOffers()) {

                            // Klocki LEGO mogą mieć CDATA i spacje, upewnijmy się
                            if (dto.getCategory() != null && dto.getCategory().contains("Klocki LEGO")) {
                                standardizedOffers.add(new StandardizedOffer(
                                        dto.getName().trim(),
                                        dto.getPrice(),
                                        dto.getUrl(),
                                        "al.to"
                                ));
                            }
                        }
                    }
                }
            }
            log.info("Zmapowano {} ofert LEGO z feedu al.to.", standardizedOffers.size());
            return standardizedOffers;

        } catch (Exception e) {
            log.error("Błąd parsowania feedu al.to: {}", e.getMessage());
            return Collections.emptyList();
        }
    }
}