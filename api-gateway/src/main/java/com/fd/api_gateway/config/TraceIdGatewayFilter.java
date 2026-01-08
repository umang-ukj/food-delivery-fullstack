package com.fd.api_gateway.config;


import org.slf4j.MDC;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Component
public class TraceIdGatewayFilter implements GlobalFilter, Ordered {

    public static final String TRACE_HEADER = "X-Trace-Id";
    public static final String TRACE_ID = "traceId";

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, 
                             org.springframework.cloud.gateway.filter.GatewayFilterChain chain) {

        String traceId = exchange.getRequest()
                .getHeaders()
                .getFirst(TRACE_HEADER);

        if (traceId == null || traceId.isBlank()) {
            traceId = UUID.randomUUID().toString();
        }

        // Put in MDC for logging
        MDC.put(TRACE_ID, traceId);

        ServerHttpRequest mutatedRequest = exchange.getRequest()
                .mutate()
                .header(TRACE_HEADER, traceId)
                .build();

        return chain.filter(exchange.mutate().request(mutatedRequest).build())
                .doFinally(signal -> MDC.clear());
    }

    @Override
    public int getOrder() {
        return -1; // run early
    }
}
