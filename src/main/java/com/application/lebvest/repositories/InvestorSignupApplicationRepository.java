package com.application.lebvest.repositories;

import com.application.lebvest.models.entities.InvestorSignupApplication;
import org.springframework.data.jpa.repository.JpaRepository;

public interface InvestorSignupApplicationRepository extends JpaRepository<InvestorSignupApplication, Long> {

    boolean existsByEmailIgnoreCase(String email);
}
