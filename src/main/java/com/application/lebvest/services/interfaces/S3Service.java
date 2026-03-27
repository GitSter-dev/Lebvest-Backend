package com.application.lebvest.services.interfaces;

import software.amazon.awssdk.services.s3.presigner.model.PresignedPutObjectRequest;

public interface S3Service {
    PresignedPutObjectRequest generatePresignedUrl(String key, String contentType);
}
