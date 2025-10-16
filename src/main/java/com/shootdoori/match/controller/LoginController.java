package com.shootdoori.match.controller;

import com.shootdoori.match.dto.*;
import com.shootdoori.match.resolver.LoginUser;
import com.shootdoori.match.service.AuthService;
import com.shootdoori.match.service.TokenRefreshService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class LoginController {
    private static final Logger logger = LoggerFactory.getLogger(LoginController.class);
    
    private final AuthService authService;
    private final TokenRefreshService tokenRefreshService;
    private final boolean isSecure;

    private static final long ACCESS_TOKEN_EXPIRES_IN_SECONDS = 30 * 60L;
    private static final long REFRESH_TOKEN_EXPIRES_IN_SECONDS = 30 * 24 * 60 * 60L;

    public LoginController(AuthService authService, TokenRefreshService tokenRefreshService,
                          @Value("${spring.profiles.active:test}") String activeProfile) {
        this.authService = authService;
        this.tokenRefreshService = tokenRefreshService;

        this.isSecure = !"test".equals(activeProfile);
        logger.info("Cookie secure mode: {} (profile: {})", this.isSecure, activeProfile);
    }

    @PostMapping("/login")
    public ResponseEntity<AuthTokenResponse> login(
        @Valid @RequestBody LoginRequest loginRequest,
        HttpServletRequest request
    ) {
        AuthToken token = authService.login(loginRequest, request);

        return ResponseEntity.ok(new AuthTokenResponse(
                token.accessToken(), token.refreshToken(), ACCESS_TOKEN_EXPIRES_IN_SECONDS, REFRESH_TOKEN_EXPIRES_IN_SECONDS));
    }

    @PostMapping("/register")
    public ResponseEntity<AuthTokenResponse> register(
        @Valid @RequestBody ProfileCreateRequest profileCreateRequest,
        HttpServletRequest request
    ) {
        AuthToken token = authService.register(profileCreateRequest, request);

        return ResponseEntity.ok(new AuthTokenResponse(
                token.accessToken(), token.refreshToken(), ACCESS_TOKEN_EXPIRES_IN_SECONDS, REFRESH_TOKEN_EXPIRES_IN_SECONDS));
    }

    @PostMapping("/refresh")
    public ResponseEntity<AuthTokenResponse> refresh(
        @RequestBody TokenRefreshRequest token
    ) {
        AuthToken newTokens = tokenRefreshService.refreshAccessToken(token.refreshToken());

        return ResponseEntity.ok(new AuthTokenResponse(
                newTokens.accessToken(), newTokens.refreshToken(), ACCESS_TOKEN_EXPIRES_IN_SECONDS, REFRESH_TOKEN_EXPIRES_IN_SECONDS));
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(@RequestBody TokenRefreshRequest token) {
        authService.logout(token.refreshToken());

        return ResponseEntity.ok().build();
    }

    @PostMapping("/logout-all")
    public ResponseEntity<Void> logoutAll(
        @LoginUser Long userId
    ) {
        authService.logoutAll(userId);

        return ResponseEntity.ok().build();
    }

    @PostMapping("/login-cookie")
    public ResponseEntity<AuthTokenResponse> loginWithCookie(
        @Valid @RequestBody LoginRequest loginRequest,
        HttpServletRequest request,
        HttpServletResponse response
    ) {

        AuthToken token = authService.login(loginRequest, request);
        setHttpOnlyCookie(response, "accessToken", token.accessToken(), ACCESS_TOKEN_EXPIRES_IN_SECONDS);
        setHttpOnlyCookie(response, "refreshToken", token.refreshToken(), REFRESH_TOKEN_EXPIRES_IN_SECONDS);

        return ResponseEntity.ok().build();
    }

    @PostMapping("/logout-cookie")
    public ResponseEntity<Void> logoutWithCookie(
        HttpServletRequest request,
        HttpServletResponse response
    ) {
        String refreshToken = extractTokenFromCookie(request, "refreshToken");
        
        if (refreshToken != null) {
            try {
                authService.logout(refreshToken);
            } catch (Exception e) {
                logger.warn("Token already invalid or not found: {}", e.getMessage());
            }
        }
        
        clearHttpOnlyCookie(response, "accessToken");
        clearHttpOnlyCookie(response, "refreshToken");

        return ResponseEntity.ok().build();
    }

    private void setHttpOnlyCookie(HttpServletResponse response, String name, String value, long maxAgeSeconds) {
        try {
            ResponseCookie cookie = ResponseCookie.from(name, value)
                .httpOnly(true)
                .secure(isSecure)
                .sameSite("Lax")
                .maxAge(maxAgeSeconds)
                .path("/")
                .build();
            response.addHeader("Set-Cookie", cookie.toString());
            logger.debug("Set cookie: {} (secure: {})", name, isSecure);
        } catch (Exception e) {
            logger.error("Error setting cookie: {}", e.getMessage(), e);
        }
    }

    private void clearHttpOnlyCookie(HttpServletResponse response, String name) {
        ResponseCookie cookie = ResponseCookie.from(name, "")
            .httpOnly(true)
            .secure(isSecure)
            .sameSite("Lax")
            .maxAge(0)
            .path("/")
            .build();
        response.addHeader("Set-Cookie", cookie.toString());
        logger.debug("Cleared cookie: {} (secure: {})", name, isSecure);
    }

    private String extractTokenFromCookie(HttpServletRequest request, String cookieName) {
        if (request.getCookies() == null) {
            return null;
        }
        
        return java.util.Arrays.stream(request.getCookies())
            .filter(cookie -> cookieName.equals(cookie.getName()))
            .findFirst()
            .map(cookie -> {
                try {
                    String value = java.net.URLDecoder.decode(cookie.getValue(), java.nio.charset.StandardCharsets.UTF_8);
                    return value;
                } catch (Exception e) {
                    logger.warn("Failed to decode cookie value: {}", e.getMessage());
                    return null;
                }
            })
            .orElse(null);
    }
}