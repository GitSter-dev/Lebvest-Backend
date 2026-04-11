package com.application.lebvest.jobs;

import com.application.lebvest.repositories.SetPasswordTokenRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Clock;
import java.time.Instant;
import java.time.temporal.TemporalAmount;

@Slf4j
@Component
@RequiredArgsConstructor
public class SetPasswordTokenCleanup {

    private final SetPasswordTokenRepository setPasswordTokenRepository;
    private final Clock clock;
    @Scheduled(cron = "0 0 0 * * *")
    public void cleanupSetPasswordTokens() {
        Instant cutoff = Instant.now(clock).minusSeconds(86400);
        log.info("Deleting all expired set password tokens");
        setPasswordTokenRepository.deleteAllExpiredTokens(cutoff);
        log.info("All expired set password tokens has been cleared");
    }
}
