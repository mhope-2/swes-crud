package com.michaelhope.logging;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.servlet.HandlerMapping;

import java.io.IOException;
import java.util.UUID;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class RequestLoggingFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(RequestLoggingFilter.class);
    private static final String REQUEST_ID_HEADER = "X-Request-ID";
    private static final int MAX_REQUEST_ID_LENGTH = 128;

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {
        String requestId = requestIdFrom(request);
        long startedAt = System.nanoTime();

        MDC.put("requestId", requestId);
        response.setHeader(REQUEST_ID_HEADER, requestId);

        try {
            filterChain.doFilter(request, response);
        } finally {
            long durationMs = (System.nanoTime() - startedAt) / 1_000_000;
            String route = routeFor(request);
            int status = response.getStatus();

            if (status >= 500) {
                log.error("request.completed method={} route={} status={} durationMs={}",
                    request.getMethod(), route, status, durationMs);
            } else if (status >= 400) {
                log.warn("request.completed method={} route={} status={} durationMs={}",
                    request.getMethod(), route, status, durationMs);
            } else {
                log.info("request.completed method={} route={} status={} durationMs={}",
                    request.getMethod(), route, status, durationMs);
            }

            MDC.remove("requestId");
        }
    }

    private String requestIdFrom(HttpServletRequest request) {
        String suppliedRequestId = request.getHeader(REQUEST_ID_HEADER);
        if (suppliedRequestId != null
                && !suppliedRequestId.isBlank()
                && suppliedRequestId.length() <= MAX_REQUEST_ID_LENGTH
                && suppliedRequestId.matches("[A-Za-z0-9._:-]+")) {
            return suppliedRequestId;
        }
        return UUID.randomUUID().toString();
    }

    private String routeFor(HttpServletRequest request) {
        Object pattern = request.getAttribute(HandlerMapping.BEST_MATCHING_PATTERN_ATTRIBUTE);
        return pattern == null ? request.getRequestURI() : pattern.toString();
    }
}
