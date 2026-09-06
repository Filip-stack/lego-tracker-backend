package tracker.dto;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
@JacksonXmlRootElement(localName = "offers")
public class XmlFeedDto {

    @JacksonXmlElementWrapper(useWrapping = false)
    @JacksonXmlProperty(localName = "offer")
    private List<XmlOfferDto> offers;

    public List<XmlOfferDto> getOffers() {
        return offers;
    }
    public void setOffers(List<XmlOfferDto> offers) {
        this.offers = offers;
    }
}
