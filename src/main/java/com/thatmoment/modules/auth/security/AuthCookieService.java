package com.thatmoment.modules.auth.security;

import com.thatmoment.modules.auth.config.AuthCookieProperties;
import com.thatmoment.modules.auth.service.JwtService;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Component;

import java.time.Duration;

@Component
public class AuthCookieService {

    private static final String ACCESS_TOKEN_COOKIE = "accessToken";
    private static final String REFRESH_TOKEN_COOKIE = "refreshToken";
    private static final String ACCESS_PATH = "/";
    private static final String REFRESH_PATH = "/api/v1/auth";

    private final AuthCookieProperties properties;
    private final JwtService jwtService;

    public AuthCookieService(AuthCookieProperties properties, JwtService jwtService) {
        this.properties = properties;
        this.jwtService = jwtService;
    }

    public ResponseCookie buildAccessCookie(String token) {
        return baseCookie(ACCESS_TOKEN_COOKIE, token)
                .path(ACCESS_PATH)
                .maxAge(Duration.ofSeconds(jwtService.getAccessTokenExpirationSeconds()))
                .build();
    }

    public ResponseCookie buildRefreshCookie(String token) {
        return baseCookie(REFRESH_TOKEN_COOKIE, token)
                .path(REFRESH_PATH)
                .maxAge(Duration.ofSeconds(jwtService.getRefreshTokenExpirationSeconds()))
                .build();
    }

    public ResponseCookie clearAccessCookie() {
        return clearCookie(ACCESS_TOKEN_COOKIE, ACCESS_PATH);
    }

    public ResponseCookie clearRefreshCookie() {
        return clearCookie(REFRESH_TOKEN_COOKIE, REFRESH_PATH);
    }

    private ResponseCookie.ResponseCookieBuilder baseCookie(String name, String value) {
        return ResponseCookie.from(name, value)
                .httpOnly(true)
                .secure(properties.isSecure())
                .sameSite(properties.getSameSite());
    }

    private ResponseCookie clearCookie(String name, String path) {
        return ResponseCookie.from(name, "")
                .httpOnly(true)
                .secure(properties.isSecure())
                .sameSite(properties.getSameSite())
                .path(path)
                .maxAge(0)
                .build();
    }
}
