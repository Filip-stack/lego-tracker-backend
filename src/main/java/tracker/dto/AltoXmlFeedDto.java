package tracker.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;

import java.util.List;

@JacksonXmlRootElement(localName = "offers")
@JsonIgnoreProperties(ignoreUnknown = true)
public class AltoXmlFeedDto {

    // Zmieniamy z bezpośrednich ofert na listę grup
    @JacksonXmlElementWrapper(useWrapping = false)
    @JacksonXmlProperty(localName = "group")
    private List<AltoGroup> groups;

    public List<AltoGroup> getGroups() { return groups; }
    public void setGroups(List<AltoGroup> groups) { this.groups = groups; }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class AltoGroup {
        @JacksonXmlElementWrapper(useWrapping = false)
        @JacksonXmlProperty(localName = "o")
        private List<AltoOffer> offers;

        public List<AltoOffer> getOffers() { return offers; }
        public void setOffers(List<AltoOffer> offers) { this.offers = offers; }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class AltoOffer {
        @JacksonXmlProperty(isAttribute = true, localName = "url")
        private String url;

        @JacksonXmlProperty(isAttribute = true, localName = "price")
        private String price;

        @JacksonXmlProperty(localName = "name")
        private String name;

        @JacksonXmlProperty(localName = "cat")
        private String category;

        public String getUrl() { return url; }
        public void setUrl(String url) { this.url = url; }
        public String getPrice() { return price; }
        public void setPrice(String price) { this.price = price; }
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public String getCategory() { return category; }
        public void setCategory(String category) { this.category = category; }
    }
}