package com.application.lebvest.config;

import io.awspring.cloud.autoconfigure.core.CredentialsProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;

import java.net.URI;

@Configuration
@Profile("dev")
@RequiredArgsConstructor
public class DevS3Config {

    private final io.awspring.cloud.autoconfigure.s3.properties.S3Properties s3Properties;
    private final CredentialsProperties credentialsProperties;

    @Bean
    public S3Client s3Client() {
        assert s3Properties.getRegion() != null;
        return S3Client.builder()
                .endpointOverride(URI.create(String.valueOf(s3Properties.getEndpoint())))
                .region(Region.of(s3Properties.getRegion()))
                .credentialsProvider(
                        StaticCredentialsProvider.create(
                                AwsBasicCredentials.create(
                                        credentialsProperties.getAccessKey(),
                                        credentialsProperties.getSecretKey()
                                )
                        )
                )
                .forcePathStyle(true)
                .build();
    }
    @Bean
    public S3Presigner s3Presigner() {
        assert s3Properties.getRegion() != null;
        return S3Presigner.builder()
                .endpointOverride(URI.create(String.valueOf(s3Properties.getEndpoint())))
                .region(Region.of(s3Properties.getRegion()))
                .credentialsProvider(
                        StaticCredentialsProvider.create(
                                AwsBasicCredentials.create(
                                        credentialsProperties.getAccessKey(),
                                        credentialsProperties.getSecretKey()
                                )
                        )
                )
                .build();
    }

}