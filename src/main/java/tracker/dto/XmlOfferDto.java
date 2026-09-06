package tracker.dto;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class XmlOfferDto {
    @JacksonXmlProperty(localName = "storeName")
    private String storeName;

    @JacksonXmlProperty(localName = "setNumber")
    private String setNumber;

    @JacksonXmlProperty(localName = "price")
    private String price;

    @JacksonXmlProperty(localName = "url")
    private String url;

    public String getStoreName() {
        return storeName;
    }

    public void setStoreName(String storeName) {
        this.storeName = storeName;
    }

    public String getSetNumber() {
        return setNumber;
    }

    public void setSetNumber(String setNumber) {
        this.setNumber = setNumber;
    }

    public String getPrice() {
        return price;
    }

    public void setPrice(String price) {
        this.price = price;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }
}
