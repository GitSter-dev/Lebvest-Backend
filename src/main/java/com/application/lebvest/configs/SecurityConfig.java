package com.application.lebvest.configs;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
@RequiredArgsConstructor
public class SecurityConfig {

    private final JsonSecurityErrorHandlers jsonSecurityErrorHandlers;
    private final JwtAuthenticationConverter adminJwtAuthenticationConverter;

    /**
     * Public investor HTTP API: no JWT resource server, so a stale or invalid
     * {@code Authorization: Bearer} header (e.g. from another tab) cannot cause 401
     * on {@code POST /investors/application} or {@code POST /investors/auth/set-password}.
     */
    @Bean
    @Order(0)
    public SecurityFilterChain investorPublicSecurityFilterChain(HttpSecurity http) throws Exception {
        return http
                .securityMatcher("/investors/**")
                .csrf(AbstractHttpConfigurer::disable)
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth.anyRequest().permitAll())
                .build();
    }

    @Bean
    @Order(1)
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http
                .csrf(AbstractHttpConfigurer::disable)
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                        .requestMatchers("/admin/auth/login").permitAll()
                        .requestMatchers(
                                "/swagger-ui.html",
                                "/swagger-ui/**",
                                "/v3/api-docs",
                                "/v3/api-docs/**"
                        ).permitAll()
                        .requestMatchers("/admin/**").hasRole("ADMIN")
                        .anyRequest().denyAll()
                )
                .oauth2ResourceServer(oauth2 -> oauth2
                        .authenticationEntryPoint(jsonSecurityErrorHandlers.authenticationEntryPoint())
                        .accessDeniedHandler(jsonSecurityErrorHandlers.accessDeniedHandler())
                        .jwt(jwt -> jwt.jwtAuthenticationConverter(adminJwtAuthenticationConverter))
                )
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint(jsonSecurityErrorHandlers.authenticationEntryPoint())
                        .accessDeniedHandler(jsonSecurityErrorHandlers.accessDeniedHandler())
                )
                .build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        // Patterns allow any localhost port (ng serve, IDE preview, etc.); not a wildcard origin.
        config.setAllowedOriginPatterns(List.of("http://localhost:*", "http://127.0.0.1:*"));
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        config.setAllowCredentials(true);
        config.setAllowedHeaders(List.of("Authorization", "Content-Type", "Last-Event-ID", "Cache-Control"));
        config.setExposedHeaders(List.of("Authorization", "Cache-Control"));

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
