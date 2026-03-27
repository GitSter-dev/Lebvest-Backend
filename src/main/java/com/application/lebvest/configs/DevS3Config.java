package com.application.lebvest.configs;

import io.awspring.cloud.autoconfigure.core.CredentialsProperties;
import io.awspring.cloud.autoconfigure.s3.properties.S3Properties;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.S3Configuration;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;

import java.net.URI;

@Profile("dev")
@Configuration
@RequiredArgsConstructor
public class DevS3Config {

    private final S3Properties s3Properties;
    private final CredentialsProperties credentialsProperties;
    @Bean
    public S3Client s3Client() {

        return S3Client.builder()
                .endpointOverride(URI.create(String.valueOf(s3Properties.getEndpoint())))
                .region(Region.US_EAST_1) // MinIO requires a region, though it's ignored
                .credentialsProvider(StaticCredentialsProvider.create(
                        AwsBasicCredentials.create(
                                credentialsProperties.getAccessKey(),
                                credentialsProperties.getSecretKey()
                        )))
                .serviceConfiguration(S3Configuration.builder()
                        .pathStyleAccessEnabled(true) // Crucial for MinIO
                        .build())
                .build();
    }


    @Bean
    public S3Presigner s3Presigner() {
        return S3Presigner.builder()
                .endpointOverride(URI.create(String.valueOf(s3Properties.getEndpoint())))
                .region(Region.US_EAST_1)
                .credentialsProvider(StaticCredentialsProvider.create(
                        AwsBasicCredentials.create(
                                credentialsProperties.getAccessKey(),
                                credentialsProperties.getSecretKey()
                        )))
                // S3Presigner automatically handles path-style if the endpoint is provided
                .build();
    }
}
