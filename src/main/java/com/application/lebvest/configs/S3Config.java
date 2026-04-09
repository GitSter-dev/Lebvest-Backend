package com.application.lebvest.configs;

import io.awspring.cloud.autoconfigure.core.CredentialsProperties;
import io.awspring.cloud.autoconfigure.s3.properties.S3Properties;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.providers.AwsRegionProvider;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;

@Configuration
@RequiredArgsConstructor
public class S3Config {

    private final CredentialsProperties credsProperties;
    private final S3Properties s3Properties;
    private final AwsRegionProvider regionProvider;

    @Bean
    public S3Presigner s3Presigner() {
        S3Presigner.Builder builder = S3Presigner.builder()
                .credentialsProvider(StaticCredentialsProvider.create(
                        AwsBasicCredentials.create(
                                credsProperties.getAccessKey(),
                                credsProperties.getSecretKey()
                        )
                ))
                .region(regionProvider.getRegion());

        // for dev only
        if (s3Properties.getEndpoint() != null) {
            builder.endpointOverride(s3Properties.getEndpoint());
        }

        return builder.build();
    }
}