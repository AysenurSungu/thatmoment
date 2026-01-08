package com.thatmoment.modules.auth.security;

import com.thatmoment.modules.auth.service.JwtService;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.UUID;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(JwtAuthenticationFilter.class);
    private static final String AUTHORIZATION_HEADER = "Authorization";
    private static final String BEARER_PREFIX = "Bearer ";

    private final JwtService jwtService;

    public JwtAuthenticationFilter(JwtService jwtService) {
        this.jwtService = jwtService;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {
        try {
            String jwt = extractJwtFromRequest(request);

            if (jwt != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                if (jwtService.isTokenValid(jwt)) {
                    String tokenType = jwtService.extractTokenType(jwt);

                    if (!"access".equals(tokenType)) {
                        log.debug("Invalid token type: {}", tokenType);
                        request.setAttribute("jwt_error", "INVALID_TOKEN_TYPE");
                    } else {
                        UUID userId = jwtService.extractUserId(jwt);
                        String email = jwtService.extractEmail(jwt);
                        UUID sessionId = jwtService.extractSessionId(jwt);

                        UserPrincipal principal = UserPrincipal.of(userId, sessionId, email, true);

                        UsernamePasswordAuthenticationToken authentication =
                                new UsernamePasswordAuthenticationToken(
                                        principal,
                                        null,
                                        principal.getAuthorities()
                                );

                        authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                        SecurityContextHolder.getContext().setAuthentication(authentication);

                        log.debug("User authenticated: {} session: {}", userId, sessionId);
                    }
                }
            }
        } catch (ExpiredJwtException e) {
            log.debug("JWT token expired: {}", e.getMessage());
            request.setAttribute("jwt_error", "TOKEN_EXPIRED");
        } catch (JwtException e) {
            log.debug("Invalid JWT token: {}", e.getMessage());
            request.setAttribute("jwt_error", "INVALID_TOKEN");
        } catch (Exception e) {
            log.error("JWT authentication error", e);
            request.setAttribute("jwt_error", "AUTH_ERROR");
        }

        filterChain.doFilter(request, response);
    }

    private String extractJwtFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader(AUTHORIZATION_HEADER);
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith(BEARER_PREFIX)) {
            return bearerToken.substring(BEARER_PREFIX.length());
        }
        return null;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();
        return path.startsWith("/api/v1/auth/register")
                || path.startsWith("/api/v1/auth/verify-email")
                || path.startsWith("/api/v1/auth/resend-code")
                || path.startsWith("/api/v1/auth/login")
                || path.startsWith("/api/v1/auth/refresh")
                || path.startsWith("/api/v1/health")
                || path.startsWith("/swagger")
                || path.startsWith("/v3/api-docs");
    }
}
