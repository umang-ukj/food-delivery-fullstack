package com.fd.api_gateway.filter;

import java.nio.charset.StandardCharsets;

import javax.crypto.SecretKey;

import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;

import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import reactor.core.publisher.Mono;
//centralized jwt validation
//if every MS validates jwt, it results in dupliation.

@Component
@Order(-1)
public class JwtFilter implements GlobalFilter {

    private static final String SECRET =
            "food-delivery-fullstack-project-secret-key";

    private final SecretKey key =
            Keys.hmacShaKeyFor(SECRET.getBytes(StandardCharsets.UTF_8));

    //testing purpose
    @PostConstruct
    public void init() {
        System.out.println(" JwtFilter REGISTERED");
    }
    
    
    //ServerWebExchange- wraps incoming http request and response
    @Override
    public Mono<Void> filter(ServerWebExchange exchange,
                             GatewayFilterChain chain) {
    	//extracting path to find which microservice the request is pointing
        String path = exchange.getRequest().getURI().getPath();

        // Allow auth endpoints
        if (path.startsWith("/auth")) {
            return chain.filter(exchange);
        }

        String authHeader =
                exchange.getRequest().getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
        //header is must, so if header is null or doesn't start with bearer, it throws 401 unauthorized
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
            return exchange.getResponse().setComplete();
        }

        String token = authHeader.substring(7);

        try {
            Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token);
            System.out.println("JWT VALIDATED SUCCESSFULLY"); // 
        } catch (JwtException e) {
            e.printStackTrace(); // 
            exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
            return exchange.getResponse().setComplete();
        }

        return chain.filter(exchange);
    }
}

