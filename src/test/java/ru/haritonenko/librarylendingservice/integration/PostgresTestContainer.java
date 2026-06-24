package ru.haritonenko.librarylendingservice.integration;

import org.testcontainers.containers.PostgreSQLContainer;

public final class PostgresTestContainer {

    public static final PostgreSQLContainer<?> POSTGRES_CONTAINER =
            new PostgreSQLContainer<>("postgres:14-alpine")
                    .withDatabaseName("libraryLendingTestDb")
                    .withUsername("libraryTest")
                    .withPassword("libraryTest");

    static {
        POSTGRES_CONTAINER.start();
    }

    private PostgresTestContainer() {
    }
}
