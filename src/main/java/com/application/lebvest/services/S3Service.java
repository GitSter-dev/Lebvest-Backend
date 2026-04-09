package com.application.lebvest.services;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.PresignedPutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.model.PutObjectPresignRequest;

import java.time.Duration;
import java.time.Instant;

@Service
@RequiredArgsConstructor
public class S3Service {

    private final S3Presigner s3Presigner;
    private static final String BUCKET_NAME = "investor-documents-bucket";

    public PresignedPutObjectRequest presignUrl(String key) {
        PutObjectRequest request = PutObjectRequest
                .builder()
                .bucket(BUCKET_NAME)
                .key(key)
                .build();

        PutObjectPresignRequest presignRequest = PutObjectPresignRequest
                .builder()
                .putObjectRequest(request)
                .signatureDuration(Duration.ofMinutes(15))
                .build();
        return s3Presigner.presignPutObject(presignRequest);
    }
}
