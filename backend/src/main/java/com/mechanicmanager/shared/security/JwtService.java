package com.mechanicmanager.shared.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.time.Duration;
import java.time.Instant;
import java.util.Date;
import java.util.Optional;

@Service
public class JwtService {
    
    private final SecretKey key;
    private final Duration expiration;
    
    public JwtService(
            @Value("${security.jwt.secret}") String secret,
            @Value("${security.jwt.expiration-minutes}") long expirationMinutes
    ) {
        this.key = buildKey(secret);
        this.expiration = Duration.ofMinutes(expirationMinutes);
    }

    /**
     * Sin clave la aplicación no arranca, y el mensaje dice qué hacer: el error
     * por defecto ("key must be at least 256 bits") no menciona que falta una
     * variable de entorno ni dónde se configura.
     */
    private static SecretKey buildKey(String secret) {
        if (secret == null || secret.isBlank()) {
            throw new IllegalStateException(
                    "Falta JWT_SECRET. En local, copiá backend/.env.example a backend/.env y "
                            + "completá el valor (el archivo explica cómo generarlo). "
                            + "En un servidor, definilo como variable de entorno.");
        }

        try {
            return Keys.hmacShaKeyFor(Decoders.BASE64.decode(secret.trim()));
        } catch (JwtException | IllegalArgumentException e) {
            throw new IllegalStateException(
                    "JWT_SECRET no es válido: tiene que ser base64 y decodificar a al menos 32 bytes. "
                            + "Ver backend/.env.example para generarlo.", e);
        }
    }
    
    public String generateToken(AuthenticatedUser user) {
        Instant now = Instant.now();
        
        return Jwts.builder()
                .subject(user.email())
                .claim("uid", user.id())
                .claim("role", user.role().name())
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plus(expiration)))
                .signWith(key)
                .compact();
    }
    
    public Optional<String> extractEmail(String token) {
        try {
            Claims claims = Jwts.parser()
                    .verifyWith(key)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
            
            return Optional.of(claims.getSubject());
        } catch (JwtException | IllegalArgumentException e) {
            return Optional.empty();
        }
    }
    
    public long getExpirationSeconds() {
        return expiration.toSeconds();
    }
}