package com.application.lebvest.configs;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.*;

/**
 * Allows SSE endpoints to receive the JWT via an {@code access_token} query
 * parameter, since the browser {@code EventSource} API does not support custom
 * headers. The token is promoted to a standard {@code Authorization: Bearer}
 * header so the rest of the security chain works unchanged.
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE + 1)
public class SseAccessTokenFilter extends OncePerRequestFilter {

    private static final String SSE_PATH_SUFFIX = "/notifications/events";

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {
        if (request.getRequestURI().endsWith(SSE_PATH_SUFFIX)
                && request.getHeader("Authorization") == null) {
            String token = request.getParameter("access_token");
            if (token != null && !token.isBlank()) {
                filterChain.doFilter(new BearerHeaderWrapper(request, token), response);
                return;
            }
        }
        filterChain.doFilter(request, response);
    }

    private static class BearerHeaderWrapper extends HttpServletRequestWrapper {
        private final String token;

        BearerHeaderWrapper(HttpServletRequest request, String token) {
            super(request);
            this.token = token;
        }

        @Override
        public String getHeader(String name) {
            if ("Authorization".equalsIgnoreCase(name)) {
                return "Bearer " + token;
            }
            return super.getHeader(name);
        }

        @Override
        public Enumeration<String> getHeaders(String name) {
            if ("Authorization".equalsIgnoreCase(name)) {
                return Collections.enumeration(List.of("Bearer " + token));
            }
            return super.getHeaders(name);
        }

        @Override
        public Enumeration<String> getHeaderNames() {
            Set<String> names = new LinkedHashSet<>();
            Enumeration<String> original = super.getHeaderNames();
            while (original.hasMoreElements()) {
                names.add(original.nextElement());
            }
            names.add("Authorization");
            return Collections.enumeration(names);
        }
    }
}
