package ru.haritonenko.librarylendingservice.integration;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.junit.jupiter.Testcontainers;

@Testcontainers
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public abstract class AbstractIntegrationTest {

    @DynamicPropertySource
    static void datasourceProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", PostgresTestContainer.POSTGRES_CONTAINER::getJdbcUrl);
        registry.add("spring.datasource.username", PostgresTestContainer.POSTGRES_CONTAINER::getUsername);
        registry.add("spring.datasource.password", PostgresTestContainer.POSTGRES_CONTAINER::getPassword);
        registry.add("spring.datasource.driver-class-name", PostgresTestContainer.POSTGRES_CONTAINER::getDriverClassName);
        registry.add("spring.liquibase.enabled", () -> "true");
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "none");
        registry.add("spring.jpa.database-platform", () -> "org.hibernate.dialect.PostgreSQLDialect");
        registry.add("spring.autoconfigure.exclude", () ->
                "org.springframework.boot.autoconfigure.data.redis.RedisAutoConfiguration," +
                        "org.springframework.boot.autoconfigure.data.redis.RedisRepositoriesAutoConfiguration"
        );
        registry.add("jwt.secret-key", () -> "GRO16WVD3nAoqU1dqzcAVblU1m4p0oWpiyU-MSQ0i5XQoOcFuOowoPTMyAq9KigqcdFWXrvCv-MVSc-E1rycjw");
        registry.add("jwt.lifetime", () -> "86400000");
    }
}
