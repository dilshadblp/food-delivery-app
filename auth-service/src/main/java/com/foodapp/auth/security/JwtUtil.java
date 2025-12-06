package com.foodapp.auth.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;

@Component
public class JwtUtil {

    private final String privateKeyPem;
    private final String publicKeyPem;
    private final String issuer;
    private final long expiresMin;

    private PrivateKey privateKey;
    private PublicKey publicKey;

    public JwtUtil(@Value("${security.jwt.private-key}") String privateKeyPem,
                   @Value("${security.jwt.public-key}") String publicKeyPem,
                   @Value("${security.jwt.issuer}") String issuer,
                   @Value("${security.jwt.expires-min}") long expiresMin) {
        this.privateKeyPem = privateKeyPem;
        this.publicKeyPem = publicKeyPem;
        this.issuer = issuer;
        this.expiresMin = expiresMin;
    }

    @PostConstruct
    void init() throws Exception {
        this.privateKey = loadPrivateKey(privateKeyPem);
        this.publicKey = loadPublicKey(publicKeyPem);
    }

    private PrivateKey loadPrivateKey(String pem) throws Exception {
        String clean = pem
                .replace("-----BEGIN PRIVATE KEY-----", "")
                .replace("-----END PRIVATE KEY-----", "")
                .replaceAll("\\s+", "");
        byte[] decoded = Base64.getDecoder().decode(clean);
        PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(decoded);
        return KeyFactory.getInstance("RSA").generatePrivate(keySpec);
    }

    private PublicKey loadPublicKey(String pem) throws Exception {
        String clean = pem
                .replace("-----BEGIN PUBLIC KEY-----", "")
                .replace("-----END PUBLIC KEY-----", "")
                .replaceAll("\\s+", "");
        byte[] decoded = Base64.getDecoder().decode(clean);
        X509EncodedKeySpec keySpec = new X509EncodedKeySpec(decoded);
        return KeyFactory.getInstance("RSA").generatePublic(keySpec);
    }

    // ✅ NEW: include userId as subject, email as separate claim
    public String generateToken(Long userId, String email, List<String> roles) {
        Instant now = Instant.now();
        Instant exp = now.plus(expiresMin, ChronoUnit.MINUTES);

        return Jwts.builder()
                .setSubject(String.valueOf(userId))      // sub = userId (String)
                .setIssuer(issuer)
                .setIssuedAt(Date.from(now))
                .setExpiration(Date.from(exp))
                .claim("email", email)                   // custom claim
                .claim("roles", roles)                   // custom claim
                .setId(UUID.randomUUID().toString())     // jti
                .signWith(privateKey, SignatureAlgorithm.RS256)
                .compact();
    }

    // (Optional) Validate token inside auth-service if needed
    public Claims validateAndGetClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(publicKey)  // verify with public key
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    /*public String createToken(Long userId, String role) {
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
    }*/
}
