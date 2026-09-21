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
 *
 * Order: HIGHEST_PRECEDENCE — runs before Spring Security and before
 * Tomcat commits the response, so setHeader() is effective.
 *
 * PDF detection is done from the request URL (not the response Content-Type)
 * because we must set headers BEFORE chain.doFilter, and Content-Type is
 * only known after the controller runs.
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class SecurityHeadersFilter extends OncePerRequestFilter {

    private static final String STRICT_CSP =
            "default-src 'none'; frame-ancestors 'none'; base-uri 'none'; form-action 'none'";

    private static final String PDF_CSP =
            "default-src 'none'; frame-ancestors 'self'; base-uri 'none'; object-src 'self'";

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain chain)
            throws ServletException, IOException {

        String uri = request.getRequestURI();
        boolean isPdf = isPdfEndpoint(uri);
        boolean isVerification = uri != null && uri.startsWith("/api/verifications/");

        // ---- Content-Security-Policy ----
        response.setHeader("Content-Security-Policy", isPdf ? PDF_CSP : STRICT_CSP);

        // ---- Referrer-Policy ----
        response.setHeader("Referrer-Policy", "strict-origin-when-cross-origin");

        // ---- Permissions-Policy ----
        response.setHeader("Permissions-Policy",
                "geolocation=(), microphone=(), camera=(), payment=(), usb=()");

        // ---- Cross-Origin-* ----
        response.setHeader("Cross-Origin-Resource-Policy", isPdf ? "cross-origin" : "same-site");
        response.setHeader("Cross-Origin-Opener-Policy", "same-origin");

        // ---- X-Content-Type-Options ----
        response.setHeader("X-Content-Type-Options", "nosniff");

        // ---- X-Frame-Options ----
        // PDFs need to be embeddable (SAMEORIGIN). Everything else is DENY.
        response.setHeader("X-Frame-Options", isPdf ? "SAMEORIGIN" : "DENY");

        // ---- Public verification hardening ----
        if (isVerification) {
            response.setHeader("X-Robots-Tag", "noindex, nofollow, noarchive, nosnippet");
            response.setHeader("Cache-Control", "private, no-store, max-age=0");
        }

        chain.doFilter(request, response);
    }

    /**
     * Endpoints that stream a PDF file inline.
     * Matched by suffix so it catches both /api/documents/{id}/signed-pdf
     * and /api/verifications/{id}/signed-pdf-preview.
     */
    private boolean isPdfEndpoint(String uri) {
        if (uri == null) return false;
        return uri.endsWith("/signed-pdf")
                || uri.endsWith("/signed-pdf-preview")
                || uri.endsWith("/original-pdf");
    }
}
