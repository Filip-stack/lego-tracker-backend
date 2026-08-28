package tracker.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import tracker.model.LegoSet;

public interface LegoSetRepository extends JpaRepository<LegoSet,Long> {
}
