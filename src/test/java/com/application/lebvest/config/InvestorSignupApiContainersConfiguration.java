package com.application.lebvest.config;

import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.postgresql.PostgreSQLContainer;
import org.testcontainers.utility.DockerImageName;

/**
 * Shared Testcontainers for investor signup API integration tests (PostgreSQL, MinIO, Mailpit).
 * Subclasses must be annotated with {@link org.testcontainers.junit.jupiter.Testcontainers}.
 */
public abstract class InvestorSignupApiContainersConfiguration {

    @Container
    public static final PostgreSQLContainer POSTGRES = new PostgreSQLContainer(
            DockerImageName.parse("postgres:16-alpine"));

    @Container
    public static final GenericContainer<?> MINIO = new GenericContainer<>(
            DockerImageName.parse("minio/minio:latest"))
            .withCommand("server", "/data")
            .withEnv("MINIO_ROOT_USER", "testminio")
            .withEnv("MINIO_ROOT_PASSWORD", "testminiosecret")
            .withExposedPorts(9000)
            .waitingFor(Wait.forListeningPort());

    @Container
    public static final GenericContainer<?> MAILPIT = new GenericContainer<>(
            DockerImageName.parse("axllent/mailpit:latest"))
            .withExposedPorts(1025)
            .waitingFor(Wait.forListeningPort());

    @DynamicPropertySource
    static void registerContainerProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", POSTGRES::getJdbcUrl);
        registry.add("spring.datasource.username", POSTGRES::getUsername);
        registry.add("spring.datasource.password", POSTGRES::getPassword);
        registry.add("spring.datasource.driver-class-name", () -> "org.postgresql.Driver");

        registry.add("spring.cloud.aws.s3.endpoint",
                () -> "http://127.0.0.1:" + MINIO.getMappedPort(9000));
        registry.add("spring.cloud.aws.s3.path-style-access-enabled", () -> "true");
        registry.add("spring.cloud.aws.region.static", () -> "us-east-1");
        registry.add("spring.cloud.aws.credentials.access-key", () -> "testminio");
        registry.add("spring.cloud.aws.credentials.secret-key", () -> "testminiosecret");

        registry.add("spring.mail.host", MAILPIT::getHost);
        registry.add("spring.mail.port", () -> String.valueOf(MAILPIT.getMappedPort(1025)));
    }
}
