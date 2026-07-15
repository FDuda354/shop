package pl.dudios.shopmvn;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * Smoke test całej aplikacji: podnosi pełny kontekst Springa na prawdziwym
 * Postgresie z Testcontainers, razem z migracjami Flyway (schemat + seedy).
 */
@SpringBootTest
class ShopmvnApplicationIntegrationTest extends AbstractIntegrationTest {

    @Test
    void contextLoads() {
    }
}
