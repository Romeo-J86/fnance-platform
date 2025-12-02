package com.smatech.finance.jwt;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.*;
import java.util.function.Function;

/**
 * createdBy romeo
 * createdDate 29/11/2025
 * createdTime 12:14
 * projectName Finance Platform
 **/

@Slf4j
@Component
public class JwtUtil {

    @Value("${jwt.secret:mySuperSecureSecretKeyForJWTGenerationInFinanceApp2024ThatIsLongEnoughForHS256}")
    private String secret;

    @Value("${jwt.expiration:86400000}")
    private Long expiration;

    @Value("${jwt.issuer:finance-platform}")
    private String issuer;

    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(secret.getBytes());
    }

    public String generateToken(String email, Map<String, Object> claims) {
        return Jwts.builder()
                .claims(claims)
                .subject(email)
                .issuer(issuer)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + expiration))
                .signWith(getSigningKey(), Jwts.SIG.HS256)
                .compact();
    }

    public String generateToken(String email) {
        Map<String, Object> claims = new HashMap<>();
        return generateToken(email, claims);
    }

    public String extractEmail(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    @SuppressWarnings("unchecked")
    public List<String> extractRoles(String token) {
        Claims claims = extractAllClaims(token);
        return claims.get("roles", List.class);
    }

    public Long extractUserId(String token) {
        Claims claims = extractAllClaims(token);
        return claims.get("userId", Long.class);
    }

    public String extractFirstName(String token) {
        Claims claims = extractAllClaims(token);
        return claims.get("firstName", String.class);
    }

    public String extractLastName(String token) {
        Claims claims = extractAllClaims(token);
        return claims.get("lastName", String.class);
    }

    public Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    private Claims extractAllClaims(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public boolean validateToken(String token) {
        try {
            Jwts.parser()
                    .verifyWith(getSigningKey())
                    .build()
                    .parseSignedClaims(token);
            return true;
        } catch (MalformedJwtException e) {
            log.error("Invalid JWT token: {}", e.getMessage());
        } catch (ExpiredJwtException e) {
            log.error("JWT token is expired: {}", e.getMessage());
        } catch (UnsupportedJwtException e) {
            log.error("JWT token is unsupported: {}", e.getMessage());
        } catch (IllegalArgumentException e) {
            log.error("JWT claims string is empty: {}", e.getMessage());
        } catch (JwtException e) {
            log.error("JWT validation error: {}", e.getMessage());
        }
        return false;
    }

    public boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    public boolean isTokenAboutToExpire(String token, long thresholdMs) {
        try {
            Date expiration = extractExpiration(token);
            long timeUntilExpiration = expiration.getTime() - System.currentTimeMillis();
            return timeUntilExpiration <= thresholdMs;
        } catch (Exception e) {
            log.warn("Unable to check token expiration: {}", e.getMessage());
            return false;
        }
    }

    public Map<String, Object> extractAllClaimsAsMap(String token) {
        try {
            Claims claims = extractAllClaims(token);
            Map<String, Object> claimsMap = new HashMap<>();
            claimsMap.put("subject", claims.getSubject());
            claimsMap.put("issuer", claims.getIssuer());
            claimsMap.put("issuedAt", claims.getIssuedAt());
            claimsMap.put("expiration", claims.getExpiration());
            claimsMap.put("roles", claims.get("roles"));
            claimsMap.put("firstName", claims.get("firstName"));
            claimsMap.put("lastName", claims.get("lastName"));
            claimsMap.put("userId", claims.get("userId"));
            return claimsMap;
        } catch (Exception e) {
            log.error("Error extracting claims from token: {}", e.getMessage());
            return new HashMap<>();
        }
    }

    public long getTokenExpiration() {
        return expiration;
    }

    public String getTokenIssuer() {
        return issuer;
    }
}