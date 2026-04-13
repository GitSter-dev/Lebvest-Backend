package com.application.lebvest.repositories;

import com.application.lebvest.models.entities.InvestorAccount;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface InvestorAccountRepository extends JpaRepository<InvestorAccount, Long> {

    boolean existsByEmail(String email);

    boolean existsByApplication_Id(Long applicationId);

    Optional<InvestorAccount> findByEmail(String email);
}
