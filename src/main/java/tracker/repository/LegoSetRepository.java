package tracker.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import tracker.model.LegoSet;
import java.util.List;
@Repository
public interface LegoSetRepository extends JpaRepository<LegoSet,Long> {
    List<LegoSet> findByNameContainingIgnoreCase(String name);
}
