package tracker.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "store_offers")
public class StoreOffer {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String storeName;
    private BigDecimal price;
    private String affiliateUrl;
    private LocalDateTime lastUpdated;

    // relacja n:m
    @ManyToOne
    @JoinColumn(name = "lego_set_id")
    @JsonIgnore
    private LegoSet legoSet;
    public StoreOffer() {

    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getStoreName() {
        return storeName;
    }

    public void setStoreName(String storeName) {
        this.storeName = storeName;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public String getAffiliateUrl() {
        return affiliateUrl;
    }

    public void setAffiliateUrl(String affiliateUrl) {
        this.affiliateUrl = affiliateUrl;
    }

    public LocalDateTime getLastUpdated() {
        return lastUpdated;
    }

    public void setLastUpdated(LocalDateTime lastUpdated) {
        this.lastUpdated = lastUpdated;
    }

    public LegoSet getLegoSet() {
        return legoSet;
    }

    public void setLegoSet(LegoSet legoSet) {
        this.legoSet = legoSet;
    }
}
