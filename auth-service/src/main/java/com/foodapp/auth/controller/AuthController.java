package com.foodapp.auth.controller;

import com.foodapp.auth.dto.AuthResponse;
import com.foodapp.auth.dto.BlacklistStatusResponse;
import com.foodapp.auth.dto.LoginRequest;
import com.foodapp.auth.dto.RegisterRequest;
import com.foodapp.auth.security.JwtUtil;
import com.foodapp.auth.service.AuthService;
import io.jsonwebtoken.Claims;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService service;
    private final JwtUtil jwtUtil;

    public AuthController(AuthService service, JwtUtil jwtUtil) {
        this.service = service; this.jwtUtil = jwtUtil;
    }

    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("AUTH-SERVICE OK");
    }

    @PostMapping("/register")
    public ResponseEntity<Void> register(@Valid @RequestBody RegisterRequest req) {
        service.register(req);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest req) {
        String token = service.login(req);
        long exp = 60L * 15L; // mirror config
        return ResponseEntity.ok(new AuthResponse(token, exp));
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return ResponseEntity.badRequest().build();
        }

        String token = authHeader.substring(7);
        Claims claims = jwtUtil.validateAndGetClaims(token);

        // 1️⃣ Get jti (unique token id)
        String jti = claims.getId();

        // 2️⃣ Calculate remaining lifetime in seconds
        long nowMillis = System.currentTimeMillis();
        long expMillis = claims.getExpiration().getTime();
        long ttlSeconds = Math.max(0, (expMillis - nowMillis) / 1000L);

        // 3️⃣ Store in Redis blacklist
        service.logout(jti, ttlSeconds);

        return ResponseEntity.ok().build();
    }

    @GetMapping("/internal/blacklist/{jti}")
    public ResponseEntity<BlacklistStatusResponse> checkBlacklist(@PathVariable("jti") String jti) {
        System.out.println("+++++++++++++++++++++++++++"+jti);
        try {
            boolean blacklisted = service.isBlacklisted(jti);
            return ResponseEntity.ok(new BlacklistStatusResponse(blacklisted));
        } catch (Exception ex) {
            // VERY IMPORTANT: prevent 500 for gateway calls
            return ResponseEntity.ok(new BlacklistStatusResponse(false));
        }
    }
}
