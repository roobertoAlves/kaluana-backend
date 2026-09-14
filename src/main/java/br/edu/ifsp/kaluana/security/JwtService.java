package br.edu.ifsp.kaluana.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import java.util.UUID;

@Component
public class JwtService {
    private final SecretKey key;
    private final long accessTokeExpirationInMinutes;

    public JwtService(@Value("${app.jwt.secret}")String secret,
                      @Value("${app.jwt.access-token-expiration-minutes}") long accessTokeExpirationInMinutes) {
        this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.accessTokeExpirationInMinutes = accessTokeExpirationInMinutes;
    }

    public String generateAccessToken(UUID userId, String role) {
        Instant now = Instant.now();
        return Jwts.builder()
                .subject(userId.toString())
                .claim("role", role)
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plusSeconds(accessTokeExpirationInMinutes * 60)))
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    public ParsedToken parse(String token){
        Claims claims = Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();

        UUID userId = UUID.fromString(claims.getSubject());
        String role = claims.get("role", String.class);
        return new ParsedToken(userId, role);
    }

    public record ParsedToken(UUID userId, String role) {}
}
