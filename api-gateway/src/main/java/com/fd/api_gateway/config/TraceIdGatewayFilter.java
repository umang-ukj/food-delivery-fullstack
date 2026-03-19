package com.fd.api_gateway.config;


import org.slf4j.MDC;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;

import io.micrometer.tracing.Tracer;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Component
public class TraceIdGatewayFilter implements GlobalFilter, Ordered {

    public static final String TRACE_HEADER = "X-Trace-Id";
    public static final String TRACE_ID = "traceId";
    public static final String B3_TRACE_HEADER = "X-B3-TraceId";
    public static final String TRACE_PARENT_HEADER = "traceparent";
    private final ObjectProvider<Tracer> tracerProvider;

    public TraceIdGatewayFilter(ObjectProvider<Tracer> tracerProvider) {
        this.tracerProvider = tracerProvider;
    }


    @Override
    public Mono<Void> filter(ServerWebExchange exchange, 
                             org.springframework.cloud.gateway.filter.GatewayFilterChain chain) {

        
        String traceId = exchange.getRequest().getHeaders().getFirst(B3_TRACE_HEADER);
        if (traceId == null || traceId.isBlank()) {
        	traceId = exchange.getRequest().getHeaders().getFirst(TRACE_HEADER);
        }
        if (traceId == null || traceId.isBlank()) {
            traceId = extractFromTraceParent(
                    exchange.getRequest().getHeaders().getFirst(TRACE_PARENT_HEADER)
            );
        }
        Tracer tracer = tracerProvider.getIfAvailable();
        if ((traceId == null || traceId.isBlank()) && tracer != null && tracer.currentSpan() != null) {
            traceId = tracer.currentSpan().context().traceId();
        }
        ServerHttpRequest mutatedRequest = exchange.getRequest().mutate().build();

        if (traceId != null && !traceId.isBlank()) {
            MDC.put(TRACE_ID, traceId);
            mutatedRequest = exchange.getRequest()
                    .mutate()
                    .header(TRACE_HEADER, traceId)
                    .build();
        }
        return chain.filter(exchange.mutate().request(mutatedRequest).build())
                .doFinally(signal -> MDC.remove(TRACE_ID));
    }

    @Override
    public int getOrder() {
    	return Ordered.LOWEST_PRECEDENCE; // run after tracing context is available
    }

    private String extractFromTraceParent(String traceParent) {
        if (traceParent == null || traceParent.isBlank()) {
            return null;
        }
        String[] parts = traceParent.split("-");
        if (parts.length >= 4 && parts[1].length() == 32) {
            return parts[1];
        }
        return null;
    }
}
