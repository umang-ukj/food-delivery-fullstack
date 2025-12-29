package com.fd.api_gateway.filter;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpHeaders;
import org.springframework.security.oauth2.jwt.NimbusReactiveJwtDecoder;
import org.springframework.security.oauth2.jwt.ReactiveJwtDecoder;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;

import jakarta.annotation.PostConstruct;
import reactor.core.publisher.Mono;

/**
 * This filter is NOT responsible for JWT validation.
 * JWT validation is handled by Spring Security (OAuth2 Resource Server).
 *
 * Responsibility of this filter:
 * - Logging
 * - Optional header propagation
 */
@Component
@Order(-1)
public class JwtFilter implements GlobalFilter {

    @PostConstruct
    public void init() {
        System.out.println(" JwtFilter REGISTERED (no manual JWT validation)");
    }
    private static final String SECRET =
            "food-delivery-fullstack-project-secret-key";
    
    @Bean
    public ReactiveJwtDecoder reactiveJwtDecoder() {
        SecretKey secretKey = new SecretKeySpec(
                SECRET.getBytes(),
                "HmacSHA256"
        );
        return NimbusReactiveJwtDecoder.withSecretKey(secretKey).build();
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange,
                             GatewayFilterChain chain) {

        String path = exchange.getRequest().getURI().getPath();
        String authHeader = exchange.getRequest()
                                    .getHeaders()
                                    .getFirst(HttpHeaders.AUTHORIZATION);

        // Allow auth endpoints without any processing
        if (path.startsWith("/auth")) {
            return chain.filter(exchange);
        }

        // Just log — DO NOT validate here
        System.out.println("Gateway request path: " + path);
        System.out.println("Authorization header present: " + (authHeader != null));

        // Spring Security already authenticated the request
        return chain.filter(exchange);
    }
}
