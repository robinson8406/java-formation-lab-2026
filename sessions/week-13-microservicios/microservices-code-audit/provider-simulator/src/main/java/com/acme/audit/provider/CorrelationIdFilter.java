package com.acme.audit.provider;

import java.io.IOException;
import java.util.UUID;
import org.apache.logging.log4j.ThreadContext;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class CorrelationIdFilter extends OncePerRequestFilter {
    public static final String HEADER_NAME = "X-Correlation-Id";
    public static final String CONTEXT_KEY = "idTx";

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        String suppliedId = request.getHeader(HEADER_NAME);
        String correlationId = suppliedId != null && suppliedId.matches("[A-Za-z0-9._:-]{1,128}")
                ? suppliedId : UUID.randomUUID().toString();
        ThreadContext.put(CONTEXT_KEY, correlationId);
        response.setHeader(HEADER_NAME, correlationId);
        try {
            chain.doFilter(request, response);
        } finally {
            ThreadContext.remove(CONTEXT_KEY);
        }
    }
}
