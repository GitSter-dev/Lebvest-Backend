package com.application.lebvest.configs;

import com.nimbusds.jose.jwk.source.ImmutableSecret;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.List;

@Configuration
@RequiredArgsConstructor
public class AdminJwtBeans {

    private final AdminProperties adminProperties;

    @PostConstruct
    void validateJwtSecret() {
        AdminProperties.Jwt jwt = adminProperties.jwt();
        if (jwt == null || jwt.secret() == null || jwt.secret().isBlank()) {
            throw new IllegalStateException("lebvest.admin.jwt.secret must be configured");
        }
        if (jwt.secret().getBytes(StandardCharsets.UTF_8).length < 32) {
            throw new IllegalStateException("lebvest.admin.jwt.secret must be at least 32 bytes for HS256");
        }
    }

    @Bean
    public JwtEncoder jwtEncoder() {
        byte[] key = adminProperties.jwt().secret().getBytes(StandardCharsets.UTF_8);
        return new NimbusJwtEncoder(new ImmutableSecret<>(key));
    }

    @Bean
    public JwtDecoder jwtDecoder() {
        SecretKey secretKey = new SecretKeySpec(
                adminProperties.jwt().secret().getBytes(StandardCharsets.UTF_8),
                "HS256"
        );
        return NimbusJwtDecoder.withSecretKey(secretKey).build();
    }

    @Bean
    public JwtAuthenticationConverter adminJwtAuthenticationConverter() {
        JwtAuthenticationConverter converter = new JwtAuthenticationConverter();
        converter.setJwtGrantedAuthoritiesConverter(jwt -> {
            String role = jwt.getClaimAsString("role");
            if (role == null || role.isBlank()) {
                return List.of();
            }
            return List.of(new SimpleGrantedAuthority("ROLE_" + role));
        });
        return converter;
    }
}
