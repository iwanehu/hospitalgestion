package com.hospital.gestion.api.security.jwt;

import com.hospital.gestion.api.security.user.HospitalUserPrincipal;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.time.Instant;
import java.util.Date;

@Service
public class JwtService {

    private final SecretKey signingKey;
    private final long expirationMs;
    private final String issuer;

    public JwtService(
            @Value("${security.jwt.secret}")
            String encodedSecret,

            @Value("${security.jwt.expiration-ms}")
            long expirationMs,

            @Value("${security.jwt.issuer}")
            String issuer
    ) {
        if (encodedSecret == null
                || encodedSecret.isBlank()) {
            throw new IllegalArgumentException(
                    "JWT secret cannot be empty"
            );
        }

        if (expirationMs <= 0) {
            throw new IllegalArgumentException(
                    "JWT expiration must be greater than zero"
            );
        }

        if (issuer == null || issuer.isBlank()) {
            throw new IllegalArgumentException(
                    "JWT issuer cannot be empty"
            );
        }

        this.signingKey = Keys.hmacShaKeyFor(
                Decoders.BASE64.decode(encodedSecret)
        );

        this.expirationMs = expirationMs;
        this.issuer = issuer.trim();
    }

    // ============================================================
    // GENERATE TOKEN
    // ============================================================

    public String generateAccessToken(
            HospitalUserPrincipal principal
    ) {
        Instant issuedAt = Instant.now();
        Instant expiresAt = issuedAt.plusMillis(
                expirationMs
        );

        return Jwts.builder()
                .issuer(issuer)
                .subject(principal.getUsername())
                .issuedAt(Date.from(issuedAt))
                .expiration(Date.from(expiresAt))
                .claim(
                        "userId",
                        principal.id()
                )
                .claim(
                        "role",
                        principal.role().name()
                )
                .signWith(signingKey)
                .compact();
    }

    // ============================================================
    // EXTRACT CLAIMS
    // ============================================================

    public String extractUsername(String token) {
        return extractAllClaims(token).getSubject();
    }

    public Long extractUserId(String token) {
        Number userId = extractAllClaims(token).get(
                "userId",
                Number.class
        );

        if (userId == null) {
            return null;
        }

        return userId.longValue();
    }

    public String extractRole(String token) {
        return extractAllClaims(token).get(
                "role",
                String.class
        );
    }

    public Date extractIssuedAt(String token) {
        return extractAllClaims(token).getIssuedAt();
    }

    public Date extractExpiration(String token) {
        return extractAllClaims(token).getExpiration();
    }

    // ============================================================
    // VALIDATION
    // ============================================================

    public boolean isTokenValid(
            String token,
            HospitalUserPrincipal principal
    ) {
        if (token == null
                || token.isBlank()
                || principal == null
                || !principal.isEnabled()) {
            return false;
        }

        try {
            Claims claims = extractAllClaims(token);

            String username = claims.getSubject();
            Date expiration = claims.getExpiration();

            return username != null
                    && username.equalsIgnoreCase(
                    principal.getUsername()
            )
                    && expiration != null
                    && expiration.after(new Date());

        } catch (
                JwtException
                | IllegalArgumentException exception
        ) {
            return false;
        }
    }

    // ============================================================
    // CONFIGURATION INFORMATION
    // ============================================================

    public long getExpirationSeconds() {
        return expirationMs / 1000;
    }

    // ============================================================
    // PRIVATE
    // ============================================================

    private Claims extractAllClaims(String token) {
        return Jwts.parser()
                .verifyWith(signingKey)
                .requireIssuer(issuer)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}