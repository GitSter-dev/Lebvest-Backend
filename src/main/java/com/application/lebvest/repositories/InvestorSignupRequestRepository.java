package com.application.lebvest.repositories;

import com.application.lebvest.models.entities.InvestorSignupRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface InvestorSignupRequestRepository extends JpaRepository<InvestorSignupRequest, Long> {
}
