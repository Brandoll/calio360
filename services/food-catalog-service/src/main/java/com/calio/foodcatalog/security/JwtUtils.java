package com.calio.foodcatalog.security;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import javax.crypto.SecretKey;
@Component @Slf4j
public class JwtUtils {
    @Value("${calio.jwt.secret}")
    private String jwtSecret;
    private SecretKey key() { return Keys.hmacShaKeyFor(jwtSecret.getBytes()); }
    public String getUserIdFromToken(String token) {
        return Jwts.parser().verifyWith(key()).build().parseSignedClaims(token).getPayload().getSubject();
    }
    public boolean validateToken(String token) {
        try { Jwts.parser().verifyWith(key()).build().parseSignedClaims(token); return true; }
        catch (JwtException | IllegalArgumentException e) { log.warn("JWT inválido: {}", e.getMessage()); return false; }
    }
}
