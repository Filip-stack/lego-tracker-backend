package tracker.repository;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import tracker.model.LegoSet;
import tracker.model.StoreOffer;

import java.util.List;
import java.util.Optional;
import java.util.Collection;

@Repository
public interface StoreOfferRepository extends JpaRepository<StoreOffer, Long> {
    Optional<StoreOffer> findByLegoSetAndStoreName(LegoSet legoSet, String storeName);
    List<StoreOffer> findByStoreName(String storeName);
    List<StoreOffer> findByStoreNameInAndLegoSet_SetNumIn(Collection<String> storeNames, Collection<String> setNums);
}
