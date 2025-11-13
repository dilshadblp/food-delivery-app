package com.foodapp.auth.service;

import com.foodapp.auth.dto.LoginRequest;
import com.foodapp.auth.dto.RegisterRequest;
import com.foodapp.auth.entity.UserAccount;
import com.foodapp.auth.repo.UserAccountRepository;
import com.foodapp.auth.security.JwtUtil;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.concurrent.TimeUnit;

@Service
public class AuthService {
    private final UserAccountRepository repo;
    private final PasswordEncoder encoder;
    private final JwtUtil jwt;
    private final StringRedisTemplate redis;

    public AuthService(UserAccountRepository repo, PasswordEncoder encoder, JwtUtil jwt, StringRedisTemplate redis) {
        this.repo = repo; this.encoder = encoder; this.jwt = jwt; this.redis = redis;
    }

    public void register(RegisterRequest req) {
        if (repo.existsByEmail(req.getEmail())) {
            throw new IllegalArgumentException("Email already registered");
        }
        UserAccount u = new UserAccount();
        u.setEmail(req.getEmail().toLowerCase());
        u.setPasswordHash(encoder.encode(req.getPassword()));
        u.setRole("USER");
        u.setStatus("ACTIVE");
        try {
            repo.save(u);
        } catch (DataIntegrityViolationException e) {
            throw new IllegalArgumentException("Email already registered");
        }
    }

    public String login(LoginRequest req) {
        String key = "login_fail:" + req.getEmail().toLowerCase();
        Long attempts = redis.opsForValue().increment(key);
        if (attempts != null && attempts == 1L) {
            redis.expire(key, 5, TimeUnit.MINUTES); // start 5-min window
        }
        if (attempts != null && attempts > 5) {
            throw new IllegalStateException("Too many attempts. Try again in 5 minutes.");
        }

        Optional<UserAccount> or = repo.findByEmail(req.getEmail().toLowerCase());
        if (or.isEmpty()) throw new IllegalArgumentException("Invalid credentials");

        UserAccount u = or.get();
        if (!"ACTIVE".equals(u.getStatus())) throw new IllegalStateException("Account not active");

        if (!encoder.matches(req.getPassword(), u.getPasswordHash()))
            throw new IllegalArgumentException("Invalid credentials");

        // success: reset counter
        redis.delete(key);

        return jwt.createToken(u.getId(), u.getRole());
    }

    public void logout(String jti, long ttlSeconds) {
        // (optional) blacklist:{jti} = true EX ttlSeconds
        redis.opsForValue().set("blacklist:" + jti, "1", ttlSeconds, TimeUnit.SECONDS);
    }

    public boolean isBlacklisted(String jti) {
        String v = redis.opsForValue().get("blacklist:" + jti);
        return v != null;
    }
}
