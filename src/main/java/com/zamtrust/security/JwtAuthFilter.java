package com.zamtrust.security;

import com.zamtrust.repository.IssuedTokenRepository;
import com.zamtrust.repository.UserRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.stream.Collectors;

@Component
public class JwtAuthFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final UserRepository userRepository;
    private final IssuedTokenRepository issuedTokenRepository;

    public JwtAuthFilter(JwtService jwtService,
                         UserRepository userRepository,
                         IssuedTokenRepository issuedTokenRepository) {
        this.jwtService = jwtService;
        this.userRepository = userRepository;
        this.issuedTokenRepository = issuedTokenRepository;
    }

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
                                    @NonNull HttpServletResponse response,
                                    @NonNull FilterChain filterChain)
            throws ServletException, IOException {

        String header = request.getHeader("Authorization");
        if (header != null && header.startsWith("Bearer ")) {
            String token = header.substring(7);
            if (jwtService.isValid(token)) {
                String jti = jwtService.extractJti(token);

                // Revocation check (finding #8)
                boolean revoked = jti == null
                        || issuedTokenRepository.findByJti(jti)
                                .map(it -> it.isRevoked())
                                .orElse(true); // missing jti record => treat as revoked
                if (!revoked) {
                    String username = jwtService.extractUsername(token);
                    userRepository.findByUsername(username).ifPresent(user -> {
                        var authorities = user.getRoles().stream()
                                .map(r -> new SimpleGrantedAuthority("ROLE_" + r.name()))
                                .collect(Collectors.toList());
                        var auth = new UsernamePasswordAuthenticationToken(
                                user.getUsername(), null, authorities);
                        auth.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                        SecurityContextHolder.getContext().setAuthentication(auth);
                    });
                }
            }
        }
        filterChain.doFilter(request, response);
    }
}
