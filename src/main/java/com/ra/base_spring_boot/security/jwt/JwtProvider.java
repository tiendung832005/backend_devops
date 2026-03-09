package com.ra.base_spring_boot.security.jwt;

import com.ra.base_spring_boot.model.entity.User;
import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

@Component
public class JwtProvider {

    @Value("${jwt.secret.key}")
    private String secretKey;

    @Value("${jwt.expired.access}")
    private Long expiredAccess;

    // ===================== EXTRACTION =====================

    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    public Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    public <T> T extractClaim(String token, Function<Claims, T> resolver) {
        Claims claims = extractAllClaims(token);
        return resolver.apply(claims);
    }

    private Claims extractAllClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getSignKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    // ===================== VALIDATION =====================

    public boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    public boolean validateToken(String token, UserDetails userDetails) {
        if (token == null || token.isBlank()) return false;

        return extractUsername(token).equals(userDetails.getUsername())
                && !isTokenExpired(token);
    }

    // ===================== GENERATE =====================

    public String generateToken(User user) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", user.getId());
        claims.put(
                "roles",
                user.getRoles()
                        .stream()
                        .map(r -> r.getRoleName().name())
                        .toList()
        );

        return createToken(claims, user.getUsername());
    }

    public String createToken(Map<String, Object> claims, String subject) {
        return Jwts.builder()
                .setClaims(claims)
                .setSubject(subject)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + expiredAccess))
                .signWith(getSignKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    // ===================== KEY =====================

    private Key getSignKey() {
        if (secretKey == null || secretKey.isBlank()) {
            throw new IllegalStateException("JWT secret key is not configured");
        }

        byte[] keyBytes = Decoders.BASE64.decode(secretKey);

        if (keyBytes.length < 32) {
            throw new IllegalStateException("JWT secret must be at least 256 bits");
        }

        return Keys.hmacShaKeyFor(keyBytes);
    }

    // ===================== RESET PASSWORD =====================

    public String generateResetPasswordToken(String email) {
        return Jwts.builder()
                .setSubject(email)
                .claim("type", "RESET_PASSWORD")
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + 5 * 60 * 1000))
                .signWith(getSignKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    public String extractEmailFromResetToken(String token) {
        Claims claims = extractAllClaims(token);

        if (!"RESET_PASSWORD".equals(claims.get("type"))) {
            throw new RuntimeException("Invalid reset token");
        }

        return claims.getSubject();
    }

    public String generateTokenForOAuth2(Long userId, String email) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", userId);
        return createToken(claims, email);
    }
}

