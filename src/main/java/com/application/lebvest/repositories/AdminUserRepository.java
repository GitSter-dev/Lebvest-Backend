package com.application.lebvest.repositories;

import com.application.lebvest.models.entities.AdminUser;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AdminUserRepository extends JpaRepository<AdminUser, Long> {

    Optional<AdminUser> findByEmail(String email);
}
