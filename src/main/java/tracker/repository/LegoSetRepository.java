package tracker.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import tracker.model.LegoSet;

import java.util.Collection;
import java.util.List;
import java.util.Set;

@Repository
public interface LegoSetRepository extends JpaRepository<LegoSet, Long> {

    // Zaktualizowana metoda - przyjmuje Pageable i zwraca Page
    Page<LegoSet> findByNameContainingIgnoreCase(String name, Pageable pageable);

    // Reszta Twoich starych metod, np. ta do HashSetu:
    @Query("SELECT l.setNum FROM LegoSet l")
    Set<String> findAllSetNums();

    // i ta do wyszukiwania po setNum:
    java.util.Optional<LegoSet> findBySetNum(String setNum);
    List<LegoSet> findBySetNumIn(Collection<String> setNums);
}