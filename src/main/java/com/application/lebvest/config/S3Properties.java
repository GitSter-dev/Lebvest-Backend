package com.application.lebvest.config;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.s3")
@RequiredArgsConstructor
@Getter
public class S3Properties {

    private final String bucketName;
}
