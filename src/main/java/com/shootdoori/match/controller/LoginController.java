package com.shootdoori.match.controller;

import com.shootdoori.match.dto.AuthToken;
import com.shootdoori.match.dto.AuthTokenResponse;
import com.shootdoori.match.dto.LoginRequest;
import com.shootdoori.match.dto.ProfileCreateRequest;
import com.shootdoori.match.service.AuthService;
import com.shootdoori.match.service.TokenRefreshService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class LoginController {
    private final AuthService authService;
    private final TokenRefreshService tokenRefreshService;

    public LoginController(AuthService authService, TokenRefreshService tokenRefreshService) {
        this.authService = authService;
        this.tokenRefreshService = tokenRefreshService;
    }

    @PostMapping("/login")
    public ResponseEntity<AuthTokenResponse> login(
        @RequestBody LoginRequest loginRequest,
        HttpServletRequest request,
        HttpServletResponse response
    ) {
        AuthToken token = authService.login(loginRequest, request);
        setRefreshTokenCookie(response, token.refreshToken());

        return ResponseEntity.ok(new AuthTokenResponse(token.accessToken()));
    }

    @PostMapping("/register")
    public ResponseEntity<AuthTokenResponse> register(
        @RequestBody ProfileCreateRequest profileCreateRequest,
        HttpServletRequest request,
        HttpServletResponse response
    ) {
        AuthToken token = authService.register(profileCreateRequest, request);
        setRefreshTokenCookie(response, token.refreshToken());

        return ResponseEntity.ok(new AuthTokenResponse(token.accessToken()));
    }

    @PostMapping("/refresh")
    public ResponseEntity<AuthTokenResponse> refresh(
        @CookieValue("refreshToken") String refreshToken,
        HttpServletResponse response
    ) {
        AuthToken newTokens = tokenRefreshService.refreshAccessToken(refreshToken);
        setRefreshTokenCookie(response, newTokens.refreshToken());

        return ResponseEntity.ok(new AuthTokenResponse(newTokens.accessToken()));
    }

    private void setRefreshTokenCookie(HttpServletResponse response, String refreshToken) {
        Cookie cookie = new Cookie("refreshToken", refreshToken);
        cookie.setHttpOnly(true);
        cookie.setSecure(true);
        cookie.setPath("/");
        cookie.setMaxAge(7 * 24 * 60 * 60);
        response.addCookie(cookie);
    }
}