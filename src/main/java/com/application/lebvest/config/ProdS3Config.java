package com.application.lebvest.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;

@Configuration
@Profile("prod")
@RequiredArgsConstructor
public class ProdS3Config {
    private final io.awspring.cloud.autoconfigure.s3.properties.S3Properties s3Properties;
    @Bean
    public S3Client s3Client() {
        assert s3Properties.getRegion() != null;
        return S3Client.builder()
                .region(Region.of(s3Properties.getRegion()))
                .build();
    }

    @Bean
    public S3Presigner s3Presigner() {
        assert s3Properties.getRegion() != null;
        return S3Presigner.builder()
                .region(Region.of(s3Properties.getRegion()))
                .build(); // picks up IAM role automatically
    }

}
