package com.chungpr0.bookhub.security;

import com.chungpr0.bookhub.common.enums.Role;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtTokenProvider {

    private final JwtProperties jwtProperties;
    private SecretKey signingKey;

    @PostConstruct
    public void init() {
        this.signingKey = Keys.hmacShaKeyFor(jwtProperties.getSecret().getBytes(StandardCharsets.UTF_8));
    }

    public String generateAccessToken(Long accountId, Role role, int tokenVersion) {
        Instant now = Instant.now();
        Instant expiry = now.plusSeconds(jwtProperties.getAccessTokenExpirationSeconds());

        return Jwts.builder()
                .header()
                .type("JWT")
                .and()
                .subject(String.valueOf(accountId))
                .claim("role", role.name())
                .claim("tv", tokenVersion)
                .id(UUID.randomUUID().toString())
                .issuedAt(Date.from(now))
                .expiration(Date.from(expiry))
                .signWith(signingKey, Jwts.SIG.HS256)
                .compact();
    }

    public String generateAccessToken(com.chungpr0.bookhub.modules.auth.entity.Account account) {
        return generateAccessToken(account.getId(), account.getRole(), account.getTokenVersion());
    }

    public Claims parseClaims(String token) {
        return Jwts.parser()
                .verifyWith(signingKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public Long getAccountId(Claims claims) {
        return Long.valueOf(claims.getSubject());
    }

    public Role getRole(Claims claims) {
        String roleStr = claims.get("role", String.class);
        return Role.valueOf(roleStr);
    }

    public Integer getTokenVersion(Claims claims) {
        return claims.get("tv", Integer.class);
    }

    public boolean isExpired(Claims claims) {
        return claims.getExpiration().before(new Date());
    }
}

