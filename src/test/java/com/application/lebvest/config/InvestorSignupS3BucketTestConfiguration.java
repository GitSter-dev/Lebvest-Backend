package com.application.lebvest.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.CreateBucketRequest;

@TestConfiguration(proxyBeanMethods = false)
public class InvestorSignupS3BucketTestConfiguration {

    @Bean
    ApplicationRunner ensureS3BucketExists(S3Client s3Client, @Value("${app.s3.bucket-name}") String bucket) {
        return args -> {
            boolean exists = s3Client.listBuckets().buckets().stream()
                    .anyMatch(b -> b.name().equals(bucket));
            if (!exists) {
                s3Client.createBucket(CreateBucketRequest.builder().bucket(bucket).build());
            }
        };
    }
}
