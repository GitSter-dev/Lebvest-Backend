package com.application.lebvest.repositories;

import com.application.lebvest.models.entities.InvestorApplication;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface InvestorApplicationRepository extends JpaRepository<InvestorApplication, Long> {

    boolean existsByEmail(String email);
}
