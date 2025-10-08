package ru.practicum.service.jwtvalidation;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import javax.crypto.SecretKey;

@Service
@Slf4j
public class JwtValidationService {

    private final SecretKey secretKey;

    public JwtValidationService(@Value("${jwt.secret}") String jwtSecret) {
        this.secretKey = Keys.hmacShaKeyFor(jwtSecret.getBytes());
    }

    public Mono<Claims> validateToken(String token) {
        return Mono.fromCallable(() -> Jwts.parserBuilder()
                        .setSigningKey(secretKey)
                        .build()
                        .parseClaimsJws(token)
                        .getBody())
                .onErrorResume(e -> {
                    log.error("JWT validation error: {}", e.getMessage());
                    return Mono.error(new RuntimeException("Invalid JWT token"));
                });
    }
}