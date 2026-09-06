package tracker.model;


import jakarta.persistence.*;
import jakarta.persistence.CascadeType;
import jakarta.persistence.OneToMany;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "lego_sets")
public class LegoSet {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String setNum;
    private String name;
    private Integer releaseYear;
    private Integer themeId;
    private Integer numParts;
    private String setImgUrl;

    @OneToMany(mappedBy = "legoSet", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<StoreOffer> offers = new ArrayList<>();

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getSetNum() { return setNum; }
    public void setSetNum(String setNum) { this.setNum = setNum; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public Integer getReleaseYear() { return releaseYear; }
    public void setReleaseYear(Integer releaseYear) { this.releaseYear = releaseYear; }

    public Integer getThemeId() { return themeId; }
    public void setThemeId(Integer themeId) { this.themeId = themeId; }

    public Integer getNumParts() { return numParts; }
    public void setNumParts(Integer numParts) { this.numParts = numParts; }

    public String getSetImgUrl() { return setImgUrl; }
    public void setSetImgUrl(String setImgUrl) { this.setImgUrl = setImgUrl; }

    public List<StoreOffer> getOffers() {
        return offers;
    }

    public void setOffers(List<StoreOffer> offers) {
        this.offers = offers;
    }
}


