package com.fd.api_gateway.config;

import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RouteConfig {

	@Bean
    public RouteLocator routes(RouteLocatorBuilder builder) {
        return builder.routes()
        		.route("user-docs", r -> r.path("/user/v3/api-docs")
                        .filters(f -> f.setPath("/v3/api-docs"))
                        .uri("lb://user"))
                    .route("restaurant-docs", r -> r.path("/restaurants/v3/api-docs")
                        .filters(f -> f.setPath("/v3/api-docs"))
                        .uri("lb://restaurant"))
                    .route("order-docs", r -> r.path("/orders/v3/api-docs")
                        .filters(f -> f.setPath("/v3/api-docs"))
                        .uri("lb://order"))
                    .route("payment-docs", r -> r.path("/payments/v3/api-docs")
                        .filters(f -> f.setPath("/v3/api-docs"))
                        .uri("lb://payment"))
                    .route("delivery-docs", r -> r.path("/delivery/v3/api-docs")
                        .filters(f -> f.setPath("/v3/api-docs"))
                        .uri("lb://delivery"))
            .route("user", r -> r.path("/auth/**")
                .uri("lb://user"))
            .route("order", r -> r.path("/orders","/orders/**")
                .uri("lb://order"))
            .route("restaurant", r -> r.path("/restaurants","/restaurants/**")
                    .uri("lb://restaurant"))
            .route("payment", r -> r.path("/payments","/payments/**")
                    .uri("lb://payment"))
            .route("delivery", r -> r.path("/delivery","/delivery/**")
                    .uri("lb://delivery"))
            
            .build();
	}
}
