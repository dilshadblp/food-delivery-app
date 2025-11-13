package com.foodapp.auth.controller;

import com.foodapp.auth.dto.AuthResponse;
import com.foodapp.auth.dto.LoginRequest;
import com.foodapp.auth.dto.RegisterRequest;
import com.foodapp.auth.security.JwtUtil;
import com.foodapp.auth.service.AuthService;
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

    // (Optional) logout: client sends Bearer token; we extract jti
    @PostMapping("/logout")
    public ResponseEntity<Void> logout(@RequestHeader("Authorization") String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) return ResponseEntity.badRequest().build();
        String token = authHeader.substring(7);
        var claims = jwtUtil.parse(token).getBody();
        String jti = claims.getId();
        long ttl = (claims.getExpiration().getTime() - System.currentTimeMillis())/1000;
        service.logout(jti, ttl);
        return ResponseEntity.ok().build();
    }
}
