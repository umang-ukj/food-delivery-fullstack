package com.fd.order.util;

import java.security.Key;

import org.springframework.stereotype.Component;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

@Component
public class JwtUtil {

	private final static String SECRET = "food-delivery-fullstack-project-secret-key";

    private final static Key key = Keys.hmacShaKeyFor(SECRET.getBytes());

    public static Long extractUserId(String token) {

        Claims claims = Jwts.parser()
                .setSigningKey(key)
                .parseClaimsJws(token)
                .getBody();

        return claims.get("userId", Long.class);
    }
    public String extractRole(String token) {
    	Claims claims = Jwts.parser()
                .setSigningKey(key)
                .parseClaimsJws(token)
                .getBody();
        return claims.get("role", String.class);
    }

}
