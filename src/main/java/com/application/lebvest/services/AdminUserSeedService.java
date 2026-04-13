package com.application.lebvest.services;

import com.application.lebvest.configs.AdminProperties;
import com.application.lebvest.models.entities.AdminUser;
import com.application.lebvest.repositories.AdminUserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Slf4j
@Component
@RequiredArgsConstructor
public class AdminUserSeedService implements ApplicationRunner {

    private final AdminUserRepository adminUserRepository;
    private final AdminProperties adminProperties;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(ApplicationArguments args) {
        if (!StringUtils.hasText(adminProperties.seedPassword())) {
            log.debug("Skipping admin user seed: lebvest.admin.seed-password is not set");
            return;
        }
        if (!StringUtils.hasText(adminProperties.email())) {
            log.warn("Skipping admin user seed: lebvest.admin.email is not set");
            return;
        }
        if (adminUserRepository.findByEmail(adminProperties.email().trim()).isPresent()) {
            return;
        }
        AdminUser admin = AdminUser.builder()
                .email(adminProperties.email().trim())
                .name(adminProperties.name() != null ? adminProperties.name() : "Admin")
                .passwordHash(passwordEncoder.encode(adminProperties.seedPassword()))
                .role("ADMIN")
                .enabled(true)
                .build();
        adminUserRepository.save(admin);
        log.info("Seeded default admin user for email={}", adminProperties.email());
    }
}
