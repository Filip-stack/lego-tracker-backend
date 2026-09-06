package tracker.repository;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import tracker.model.LegoSet;
import tracker.model.StoreOffer;
import java.util.Optional;

@Repository
public interface StoreOfferRepository extends JpaRepository<StoreOffer, Long> {
    Optional<StoreOffer> findByLegoSetAndStoreName(LegoSet legoSet, String storeName);
}
