package com.application.lebvest.repositories;

import com.application.lebvest.models.entities.SetPasswordToken;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;

@Repository
public interface SetPasswordTokenRepository extends JpaRepository<SetPasswordToken, Long> {

    @Modifying
    @Transactional
    // NOW -24 HRS
    @Query("DELETE FROM SetPasswordToken s WHERE s.createdAt < :cutoff")
    void deleteAllExpiredTokens(@Param("cutoff")Instant cutoff);
}
