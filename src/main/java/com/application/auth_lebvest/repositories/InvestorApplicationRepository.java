package com.application.auth_lebvest.repositories;

import com.application.auth_lebvest.models.entities.InvestorApplication;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface InvestorApplicationRepository extends JpaRepository<InvestorApplication, Integer> {
}
