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
            .route("user", r -> r.path("/auth/**")
                .uri("lb://user"))
            .route("order", r -> r.path("/orders/**")
                .uri("lb://order"))
            .route("restaurant", r -> r.path("/restaurants","/restaurants/**")
                    .uri("lb://restaurant"))
            .build();
	}
}
