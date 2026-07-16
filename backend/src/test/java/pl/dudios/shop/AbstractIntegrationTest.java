package pl.dudios.shop;

import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

/**
 * Wspólna baza testów integracyjnych: jeden współdzielony kontener Postgresa 18
 * na cały przebieg JVM (start przy pierwszej podklasie), schemat i seedy
 * stawia Flyway — dokładnie te same migracje co na produkcji.
 * Subklasy dokładają własne @SpringBootTest (MOCK albo RANDOM_PORT).
 */
@ActiveProfiles("test")
@Testcontainers
public abstract class AbstractIntegrationTest {

    @SuppressWarnings("resource")
    protected static final PostgreSQLContainer<?> POSTGRES = new PostgreSQLContainer<>(
            DockerImageName.parse("postgres:18-alpine"))
            .withDatabaseName("shop-test")
            .withUsername("test")
            .withPassword("test")
            .withReuse(true);

    static {
        POSTGRES.start();
    }

    @DynamicPropertySource
    static void overrideDatasource(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", POSTGRES::getJdbcUrl);
        registry.add("spring.datasource.username", POSTGRES::getUsername);
        registry.add("spring.datasource.password", POSTGRES::getPassword);
        registry.add("spring.datasource.driver-class-name", POSTGRES::getDriverClassName);
    }
}
