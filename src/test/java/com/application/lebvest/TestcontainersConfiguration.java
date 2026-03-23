package com.application.lebvest;

import ch.martinelli.oss.testcontainers.mailpit.MailpitContainer;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Bean;
//import org.testcontainers.grafana.LgtmStackContainer;
import org.springframework.test.context.DynamicPropertyRegistrar;
import org.testcontainers.containers.MinIOContainer;
import org.testcontainers.postgresql.PostgreSQLContainer;
//import org.testcontainers.rabbitmq.RabbitMQContainer;
import org.testcontainers.utility.DockerImageName;

@TestConfiguration(proxyBeanMethods = false)
public class TestcontainersConfiguration {

//    @Bean
//    @ServiceConnection
//    LgtmStackContainer grafanaLgtmContainer() {
//        return new LgtmStackContainer(DockerImageName.parse("grafana/otel-lgtm:latest"));
//    }

    @Bean
    @ServiceConnection
    PostgreSQLContainer postgresContainer() {
        return new PostgreSQLContainer(DockerImageName.parse("postgres:16"));
    }

//    @Bean
//    @ServiceConnection
//    RabbitMQContainer rabbitContainer() {
//        return new RabbitMQContainer(DockerImageName.parse("rabbitmq:latest"));
//    }

    @Bean
    MinIOContainer minIOContainer() {
        return new MinIOContainer(DockerImageName.parse("minio/minio:latest"));
    }

    @Bean
    org.springframework.test.context.DynamicPropertyRegistrar minIOPropertyRegistrar(MinIOContainer container) {
        return registry -> {
            registry.add("spring.cloud.aws.s3.endpoint", container::getS3URL);
            registry.add("spring.cloud.aws.s3.region", () -> "us-east-1");
            registry.add("spring.cloud.aws.credentials.access-key", container::getUserName);
            registry.add("spring.cloud.aws.credentials.secret-key", container::getPassword);
        };
    }

    @Bean
    @ServiceConnection
    MailpitContainer mailpitContainer() {
        return new MailpitContainer(DockerImageName.parse("axllent/mailpit"));
    }
}
