package com.application.lebvest.services.impls;

import com.application.lebvest.config.S3Properties;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.PresignedPutObjectRequest;

import java.time.Duration;

@Service
@RequiredArgsConstructor
@Profile("dev")
public class DevS3Service {

    private final S3Properties s3Properties;
    private final S3Presigner s3Presigner;

    public String generatePresignedUrlResponse(String destinationPath, String fileName) {

        PresignedPutObjectRequest presigned = s3Presigner.presignPutObject(req -> req
                .signatureDuration(Duration.ofMinutes(15))
                .putObjectRequest(put -> put.bucket(s3Properties.getBucketName())
                        .key(destinationPath)
                        .build())
                .build()
        );
        return presigned.url().toString();
    }
}
