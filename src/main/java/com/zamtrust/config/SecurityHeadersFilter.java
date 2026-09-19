package com.zamtrust.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * Adds security headers to every HTTP response.
 *
 * Findings resolved:
 *   #1 - Content-Security-Policy
 *   #2 - Referrer-Policy
 *   #3 - Permissions-Policy
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class SecurityHeadersFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain chain)
            throws ServletException, IOException {

        response.setHeader("Content-Security-Policy",
                "default-src 'none'; frame-ancestors 'none'; base-uri 'none'; form-action 'none'");

        response.setHeader("Referrer-Policy", "strict-origin-when-cross-origin");

        response.setHeader("Permissions-Policy",
                "geolocation=(), microphone=(), camera=(), payment=(), usb=()");

        response.setHeader("Cross-Origin-Resource-Policy", "same-site");
        response.setHeader("Cross-Origin-Opener-Policy", "same-origin");

        if (response.getHeader("X-Content-Type-Options") == null) {
            response.setHeader("X-Content-Type-Options", "nosniff");
        }
        if (response.getHeader("X-Frame-Options") == null) {
            response.setHeader("X-Frame-Options", "DENY");
        }

        chain.doFilter(request, response);
    }
}
