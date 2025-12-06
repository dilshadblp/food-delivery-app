package com.foodapp.gateway.security;

import com.foodapp.gateway.dto.BlacklistStatusResponse;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.core.Ordered;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.security.KeyFactory;
import java.security.PublicKey;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

@Component
public class JwtAuthFilter implements GlobalFilter, Ordered {

    private final PublicKey publicKey;
    private final WebClient authClient;

    public JwtAuthFilter(@Value("${security.jwt.public-key}") String publicKeyPem,
                         WebClient.Builder webClientBuilder) throws Exception {
        this.publicKey = loadPublicKey(publicKeyPem);
        this.authClient = webClientBuilder
                .baseUrl("http://AUTH-SERVICE") // via Eureka: AUTH-SERVICE
                .build();
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

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        String path = exchange.getRequest().getURI().getPath();

        // Public endpoints (no auth)
        if (path.startsWith("/api/auth/") || path.equals("/actuator/health")) {
            return chain.filter(exchange);
        }

        String authHeader = exchange.getRequest()
                .getHeaders()
                .getFirst(HttpHeaders.AUTHORIZATION);

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return unauthorized(exchange, "Missing or invalid Authorization header");
        }

        String token = authHeader.substring(7);

        Claims claims;
        try {
            claims = Jwts.parserBuilder()
                    .setSigningKey(publicKey)
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
        } catch (Exception ex) {
            ex.printStackTrace();
            return unauthorized(exchange, "Invalid or expired token");
        }
        // ✅ Now: subject = userId, email in custom claim
        String userIdStr = claims.getSubject();                   // "1"
        String email = claims.get("email", String.class);         // "testuser@example.com"
        String jti = claims.getId();

        System.out.println("email=========================" + email);
        System.out.println("userId========================" + userIdStr);
        System.out.println("jti===========================" + jti);


        // 🔹 Call auth-service to check if token is blacklisted
        return authClient.get()
                .uri("/api/auth/internal/blacklist/{jti}", jti)
                .retrieve()
                .bodyToMono(BlacklistStatusResponse.class)
                .flatMap(resp -> {
                    if (resp.blacklisted()) {
                        return unauthorized(exchange, "Token revoked");
                    }

                    // ✅ Enrich with BOTH headers
                    ServerHttpRequest mutated = exchange.getRequest()
                            .mutate()
                            .header("X-User-Id", userIdStr)
                            .header("X-User-Email", email != null ? email : "")
                            .build();

                    return chain.filter(exchange.mutate().request(mutated).build());
                })
                .onErrorResume(ex -> {
                    ex.printStackTrace();
                    // If auth-service is down, safest is to deny
                    return unauthorized(exchange, "Auth service error");
                });
    }

    private Mono<Void> unauthorized(ServerWebExchange exchange, String msg) {
        exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
        return exchange.getResponse().writeWith(
                Mono.just(exchange.getResponse()
                        .bufferFactory()
                        .wrap(msg.getBytes()))
        );
    }

    @Override
    public int getOrder() {
        return -1;
    }
}
