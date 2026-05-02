package com.application.auth_lebvest.services;

public interface S3Service {
    String generatePresignedUrl(String key);
}
