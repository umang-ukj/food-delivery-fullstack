package com.fd.restaurant.config;

import java.security.Key;

import org.springframework.stereotype.Component;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

@Component
public class JwtUtil {

    private static final String SECRET =
            "food-delivery-fullstack-project-secret-key";

    private final Key key =
            Keys.hmacShaKeyFor(SECRET.getBytes());

    public String extractRole(String token) {
        Claims claims = Jwts.parser()
                .setSigningKey(key)
                .parseClaimsJws(token)
                .getBody();

        return claims.get("role", String.class);
    }
}
