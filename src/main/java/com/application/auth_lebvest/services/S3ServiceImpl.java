package com.application.auth_lebvest.services;

import com.application.auth_lebvest.config.S3Properties;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.PutObjectPresignRequest;

import java.time.Duration;

@Service
@RequiredArgsConstructor
public class S3ServiceImpl implements S3Service {

    private final S3Presigner s3Presigner;
    private final S3Properties s3Properties;

    @Override
    public String generatePresignedUrl(String key) {
        PutObjectPresignRequest presignRequest = PutObjectPresignRequest.builder()
                .signatureDuration(Duration.ofMinutes(s3Properties.presignedUrlExpiryMinutes()))
                .putObjectRequest(PutObjectRequest.builder()
                        .bucket(s3Properties.bucket())
                        .key(key)
                        .build())
                .build();

        return s3Presigner.presignPutObject(presignRequest).url().toString();
    }
}
