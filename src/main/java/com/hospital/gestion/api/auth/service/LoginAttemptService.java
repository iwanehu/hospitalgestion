package com.hospital.gestion.api.auth.service;

import com.hospital.gestion.api.common.exception.LoginRateLimitException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Locale;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@Service
public class LoginAttemptService {

    private static final String BLOCKED_MESSAGE =
            "Too many failed login attempts. Try again later";

    private final ConcurrentMap<String, AttemptState>
            attempts = new ConcurrentHashMap<>();

    private final int maxAttempts;
    private final long attemptWindowMs;
    private final long blockDurationMs;

    public LoginAttemptService(
            @Value("${security.login.max-attempts}")
            int maxAttempts,

            @Value("${security.login.attempt-window-ms}")
            long attemptWindowMs,

            @Value("${security.login.block-duration-ms}")
            long blockDurationMs
    ) {
        if (maxAttempts <= 0) {
            throw new IllegalArgumentException(
                    "Maximum login attempts must be greater than zero"
            );
        }

        if (attemptWindowMs <= 0) {
            throw new IllegalArgumentException(
                    "Login attempt window must be greater than zero"
            );
        }

        if (blockDurationMs <= 0) {
            throw new IllegalArgumentException(
                    "Login block duration must be greater than zero"
            );
        }

        this.maxAttempts = maxAttempts;
        this.attemptWindowMs = attemptWindowMs;
        this.blockDurationMs = blockDurationMs;
    }

    public void checkAllowed(String email) {
        String key = normalizeEmail(email);
        Instant now = Instant.now();

        AttemptState state = attempts.get(key);

        if (state == null) {
            return;
        }

        if (state.blockedUntil() != null) {
            if (now.isBefore(state.blockedUntil())) {
                throw new LoginRateLimitException(
                        BLOCKED_MESSAGE
                );
            }

            attempts.remove(key, state);
            return;
        }

        if (isAttemptWindowExpired(state, now)) {
            attempts.remove(key, state);
        }
    }

    public void recordFailedAttempt(String email) {
        String key = normalizeEmail(email);
        Instant now = Instant.now();

        attempts.compute(key, (ignored, current) -> {
            if (current == null
                    || isAttemptWindowExpired(
                            current,
                            now
                    )) {
                return new AttemptState(
                        1,
                        now,
                        null
                );
            }

            int failedAttempts =
                    current.failedAttempts() + 1;

            Instant blockedUntil = null;

            if (failedAttempts >= maxAttempts) {
                blockedUntil = now.plusMillis(
                        blockDurationMs
                );
            }

            return new AttemptState(
                    failedAttempts,
                    current.firstFailureAt(),
                    blockedUntil
            );
        });
    }

    public void recordSuccessfulLogin(String email) {
        attempts.remove(normalizeEmail(email));
    }

    private boolean isAttemptWindowExpired(
            AttemptState state,
            Instant now
    ) {
        return state.firstFailureAt()
                .plusMillis(attemptWindowMs)
                .isBefore(now);
    }

    private String normalizeEmail(String email) {
        if (email == null || email.isBlank()) {
            throw new IllegalArgumentException(
                    "Email cannot be empty"
            );
        }

        return email.trim()
                .toLowerCase(Locale.ROOT);
    }

    private record AttemptState(
            int failedAttempts,
            Instant firstFailureAt,
            Instant blockedUntil
    ) {
    }
}
