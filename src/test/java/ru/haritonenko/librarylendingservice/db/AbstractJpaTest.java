package ru.haritonenko.librarylendingservice.db;

import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.autoconfigure.liquibase.LiquibaseAutoConfiguration;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.junit.jupiter.Testcontainers;
import ru.haritonenko.librarylendingservice.integration.PostgresTestContainer;

@DataJpaTest
@Testcontainers
@ActiveProfiles("test")
@ImportAutoConfiguration(LiquibaseAutoConfiguration.class)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
public abstract class AbstractJpaTest {

    @DynamicPropertySource
    static void datasourceProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", PostgresTestContainer.POSTGRES_CONTAINER::getJdbcUrl);
        registry.add("spring.datasource.username", PostgresTestContainer.POSTGRES_CONTAINER::getUsername);
        registry.add("spring.datasource.password", PostgresTestContainer.POSTGRES_CONTAINER::getPassword);
        registry.add("spring.datasource.driver-class-name", PostgresTestContainer.POSTGRES_CONTAINER::getDriverClassName);
        registry.add("spring.liquibase.enabled", () -> "true");
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "none");
        registry.add("spring.jpa.database-platform", () -> "org.hibernate.dialect.PostgreSQLDialect");
    }
}
