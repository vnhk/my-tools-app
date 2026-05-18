package com.bervan.toolsapp.security;

import com.bervan.common.user.User;
import com.bervan.common.user.UserRepository;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Optional;
import java.util.UUID;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(JwtAuthenticationFilter.class);

    private final JwtService jwtService;
    private final UserRepository userRepository;

    public JwtAuthenticationFilter(JwtService jwtService, UserRepository userRepository) {
        this.jwtService = jwtService;
        this.userRepository = userRepository;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        String authHeader = request.getHeader("Authorization");
        String token = null;
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            token = authHeader.substring(7);
        } else {
            token = request.getParameter("token");
        }
        final boolean hasToken = token != null && !token.isBlank();

        if (!hasToken) {
            Authentication existing = SecurityContextHolder.getContext().getAuthentication();
            if (existing != null && existing.isAuthenticated()) {
                filterChain.doFilter(request, response);
                return;
            }
            filterChain.doFilter(request, response);
            return;
        }

        try {
            Claims claims = jwtService.validateAndParse(token);
            UUID userId = jwtService.extractUserId(claims);
            Optional<User> userOpt = userRepository.findById(userId);
            if (userOpt.isEmpty()) {
                String username = claims.get("username", String.class);
                if (username != null && !username.isBlank()) {
                    userOpt = userRepository.findByUsername(username.trim());
                    if (userOpt.isPresent() && !userOpt.get().getId().equals(userId)) {
                        log.warn(
                                "JWT subject userId {} not found; loaded user by username '{}' (id {}). Re-login recommended.",
                                userId, username, userOpt.get().getId());
                    }
                }
            }
            userOpt.ifPresentOrElse(
                    user -> {
                        UsernamePasswordAuthenticationToken auth =
                                new UsernamePasswordAuthenticationToken(user, null, user.getAuthorities());
                        SecurityContextHolder.getContext().setAuthentication(auth);
                    },
                    () -> log.warn(
                            "Valid JWT but no User for subject {} (username claim: {}).",
                            claims.getSubject(),
                            claims.get("username", String.class)));
        } catch (JwtException e) {
            log.debug("JWT rejected: {}", e.getMessage());
        }

        filterChain.doFilter(request, response);
    }
}
