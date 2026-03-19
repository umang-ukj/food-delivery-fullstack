package com.fd.user.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.MDC;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import io.micrometer.tracing.Tracer;

import java.io.IOException;
import java.util.UUID;

@Component
public class CorrelationIdFilter extends OncePerRequestFilter {

    private static final String TRACE_ID = "traceId";
    private static final String B3_TRACE_ID = "X-B3-TraceId";
    private static final String TRACE_PARENT = "traceparent";
    private final ObjectProvider<Tracer> tracerProvider;

    public CorrelationIdFilter(ObjectProvider<Tracer> tracerProvider) {
        this.tracerProvider = tracerProvider;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,FilterChain filterChain)
    		throws ServletException, IOException {

    	String traceId = request.getHeader(B3_TRACE_ID);
        if (traceId == null || traceId.isBlank()) {
            //traceId = UUID.randomUUID().toString();
            traceId = request.getHeader("X-Trace-Id");
        }
        if (traceId == null || traceId.isBlank()) {
            traceId = extractFromTraceParent(request.getHeader(TRACE_PARENT));
        }
        Tracer tracer = tracerProvider.getIfAvailable();
        if ((traceId == null || traceId.isBlank()) && tracer != null && tracer.currentSpan() != null) {
            traceId = tracer.currentSpan().context().traceId();
        }
        if (traceId != null && !traceId.isBlank()) {
            MDC.put(TRACE_ID, traceId);
            response.setHeader("X-Trace-Id", traceId);
        }

        try {
            filterChain.doFilter(request, response);
        } finally {
        	MDC.remove(TRACE_ID);
        }
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
