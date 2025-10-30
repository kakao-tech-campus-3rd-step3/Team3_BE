package com.shootdoori.match.controller;

import com.shootdoori.match.dto.*;
import com.shootdoori.match.entity.common.DeviceType;
import com.shootdoori.match.policy.CookieSecurityPolicy;
import com.shootdoori.match.resolver.LoginUser;
import com.shootdoori.match.service.AuthService;
import com.shootdoori.match.service.TokenRefreshService;
import com.shootdoori.match.util.CookieUtil;
import com.shootdoori.match.util.HttpServletRequestHelper;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class LoginController {
    private static final Logger logger = LoggerFactory.getLogger(LoginController.class);

    private final AuthService authService;
    private final TokenRefreshService tokenRefreshService;
    private final CookieSecurityPolicy cookieSecurityPolicy;

    private static final long ACCESS_TOKEN_EXPIRES_IN_SECONDS = 30 * 60L;
    private static final long REFRESH_TOKEN_EXPIRES_IN_SECONDS = 30 * 24 * 60 * 60L;

    public LoginController(AuthService authService, TokenRefreshService tokenRefreshService, CookieSecurityPolicy cookieSecurityPolicy) {
        this.authService = authService;
        this.tokenRefreshService = tokenRefreshService;
        this.cookieSecurityPolicy = cookieSecurityPolicy;

        logger.info("Cookie secure mode: {} (profile: {})", cookieSecurityPolicy.isSecure(), cookieSecurityPolicy.getActiveProfile());
    }

    @PostMapping("/login")
    public ResponseEntity<AuthTokenResponse> login(
        @Valid @RequestBody LoginRequest loginRequest,
        HttpServletRequest request
    ) {
        ClientInfo clientInfo = getClientInfo(request);
        AuthToken token = authService.login(loginRequest, clientInfo);

        return new ResponseEntity<>(
                new AuthTokenResponse(token.accessToken(), token.refreshToken(), ACCESS_TOKEN_EXPIRES_IN_SECONDS, REFRESH_TOKEN_EXPIRES_IN_SECONDS),
                HttpStatus.OK);
    }

    @PostMapping("/register")
    public ResponseEntity<AuthTokenResponse> register(
        @Valid @RequestBody ProfileCreateRequest profileCreateRequest,
        HttpServletRequest request
    ) {
        ClientInfo clientInfo = getClientInfo(request);
        AuthToken token = authService.register(profileCreateRequest, clientInfo);

        return new ResponseEntity<>(new AuthTokenResponse(
                token.accessToken(), token.refreshToken(), ACCESS_TOKEN_EXPIRES_IN_SECONDS, REFRESH_TOKEN_EXPIRES_IN_SECONDS),
                HttpStatus.OK);
    }

    @PostMapping("/refresh")
    public ResponseEntity<AuthTokenResponse> refresh(
        @RequestBody TokenRefreshRequest token
    ) {
        AuthToken newTokens = tokenRefreshService.refreshAccessToken(token.refreshToken());

        return new ResponseEntity<>(new AuthTokenResponse(
                newTokens.accessToken(), newTokens.refreshToken(), ACCESS_TOKEN_EXPIRES_IN_SECONDS, REFRESH_TOKEN_EXPIRES_IN_SECONDS),
                HttpStatus.OK);
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(@RequestBody TokenRefreshRequest token) {
        authService.logout(token.refreshToken());

        return new ResponseEntity<>(HttpStatus.OK);
    }

    @PostMapping("/logout-all")
    public ResponseEntity<Void> logoutAll(
        @LoginUser Long userId
    ) {
        authService.logoutAll(userId);

        return new ResponseEntity<>(HttpStatus.OK);
    }

    @PostMapping("/login-cookie")
    public ResponseEntity<Void> loginWithCookie(
        @Valid @RequestBody LoginRequest loginRequest,
        HttpServletResponse response
    ) {
        HttpServletRequest request = HttpServletRequestHelper.get();
        ClientInfo clientInfo = getClientInfo(request);

        AuthToken token = authService.login(loginRequest, clientInfo);
        CookieUtil.setHttpOnlyCookie(response, "accessToken", token.accessToken(), ACCESS_TOKEN_EXPIRES_IN_SECONDS, cookieSecurityPolicy);
        CookieUtil.setHttpOnlyCookie(response, "refreshToken", token.refreshToken(), REFRESH_TOKEN_EXPIRES_IN_SECONDS, cookieSecurityPolicy);

        return new ResponseEntity<>(HttpStatus.OK);
    }

    @PostMapping("/logout-cookie")
    public ResponseEntity<Void> logoutWithCookie(
        HttpServletResponse response
    ) {
        HttpServletRequest request = HttpServletRequestHelper.get();

        String refreshToken = HttpServletRequestHelper
                .cookie(request, "refreshToken")
                .map(Cookie::getValue)
                .orElse(null);

        if (refreshToken != null) {
            try {
                authService.logout(refreshToken);
            } catch (Exception e) {
                logger.warn("Token already invalid or not found: {}", e.getMessage());
            }
        }

        CookieUtil.clearHttpOnlyCookie(response, "accessToken", cookieSecurityPolicy);
        CookieUtil.clearHttpOnlyCookie(response, "refreshToken", cookieSecurityPolicy);

        return new ResponseEntity<>(HttpStatus.OK);
    }
    
    private ClientInfo getClientInfo(HttpServletRequest request) {
        String userAgent = request.getHeader("User-Agent");
        DeviceType deviceType = DeviceType.fromUserAgent(userAgent);
        return new ClientInfo(userAgent, deviceType);
    }
}