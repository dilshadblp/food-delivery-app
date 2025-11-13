package com.foodapp.auth.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.time.Instant;
import java.util.Date;
import java.util.Map;
import java.util.UUID;

@Component
public class JwtUtil {

    private final SecretKey key;
    private final String issuer;
    private final long expiresMin;

    public JwtUtil(@Value("${security.jwt.secret}") String secret,
                   @Value("${security.jwt.issuer}") String issuer,
                   @Value("${security.jwt.expires-min}") long expiresMin) {
        this.key = Keys.hmacShaKeyFor(secret.getBytes());
        this.issuer = issuer;
        this.expiresMin = expiresMin;
    }

    public String createToken(Long userId, String role) {
        Instant now = Instant.now();
        Instant exp = now.plusSeconds(expiresMin * 60);
        return Jwts.builder()
                .setHeaderParam(Header.TYPE, Header.JWT_TYPE)
                .setIssuer(issuer)
                .setSubject(String.valueOf(userId))
                .setId(UUID.randomUUID().toString()) // jti
                .setIssuedAt(Date.from(now))
                .setExpiration(Date.from(exp))
                .addClaims(Map.of("role", role))
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    public Jws<Claims> parse(String token) {
        return Jwts.parserBuilder().setSigningKey(key).requireIssuer(issuer).build().parseClaimsJws(token);
    }

    public Long getUserId(String token) {
        return Long.valueOf(parse(token).getBody().getSubject());
    }

    public String getJti(String token) {
        return parse(token).getBody().getId();
    }
}
