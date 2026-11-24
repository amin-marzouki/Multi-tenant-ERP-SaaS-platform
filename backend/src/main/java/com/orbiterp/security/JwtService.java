package com.orbiterp.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/** Issues and checks the signed tokens; they carry user_id, tenant_id, tenant_code and roles. */
@Service
public class JwtService {

    private final SecretKey key;
    private final Duration expiration;

    public JwtService(@Value("${orbiterp.jwt.secret}") String secret,
                      @Value("${orbiterp.jwt.expiration:PT8H}") Duration expiration) {
        byte[] bytes = secret.getBytes(StandardCharsets.UTF_8);
        if (bytes.length < 32) {
            throw new IllegalStateException("orbiterp.jwt.secret (JWT_SECRET) must be at least 32 bytes long");
        }
        this.key = Keys.hmacShaKeyFor(bytes);
        this.expiration = expiration;
    }

    public IssuedToken issue(AuthenticatedUser user) {
        Instant now = Instant.now();
        Instant expiresAt = now.plus(expiration);
        String token = Jwts.builder()
                .subject(user.username())
                .claim("user_id", user.userId())
                .claim("tenant_id", user.tenantId())
                .claim("tenant_code", user.tenantCode())
                .claim("roles", List.copyOf(user.roles()))
                .issuedAt(Date.from(now))
                .expiration(Date.from(expiresAt))
                .signWith(key)
                .compact();
        return new IssuedToken(token, expiresAt);
    }

    /** Throws if the token is not signed by us, has expired or misses a claim. */
    public AuthenticatedUser parse(String token) {
        Claims claims = Jwts.parser().verifyWith(key).build().parseSignedClaims(token).getPayload();
        Number userId = claims.get("user_id", Number.class);
        Number tenantId = claims.get("tenant_id", Number.class);
        String tenantCode = claims.get("tenant_code", String.class);
        List<?> roles = claims.get("roles", List.class);
        if (userId == null || tenantId == null || tenantCode == null || roles == null) {
            throw new IllegalArgumentException("Token is missing a required claim");
        }
        Set<String> roleNames = roles.stream().map(String::valueOf).collect(Collectors.toUnmodifiableSet());
        return new AuthenticatedUser(userId.longValue(), tenantId.longValue(), tenantCode, claims.getSubject(), roleNames);
    }

    public record IssuedToken(String token, Instant expiresAt) {
    }
}
