package tracker.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

/**
 * jednorazowa migracja: starsze wersje zapisywaly numer zestawu bez wariantu ("75192"),
 * teraz trzymamy format Rebrickable ("75192-1"). dopisuje "-1" tam, gdzie go brakuje.
 * jest idempotentna, wiec mozna ja bezpiecznie odpalac przy kazdym starcie -
 * do usuniecia po przejsciu na Flyway/Liquibase.
 */
@Component
public class SetNumFormatMigration implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(SetNumFormatMigration.class);

    private final JdbcTemplate jdbcTemplate;

    public SetNumFormatMigration(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public void run(ApplicationArguments args) {
        int updated = jdbcTemplate.update(
                "UPDATE lego_sets SET set_num = set_num || '-1' WHERE set_num NOT LIKE '%-%'");
        if (updated > 0) {
            log.info("Zmigrowano {} numerow zestawow do formatu z wariantem (np. 75192-1)", updated);
        }
    }
}
