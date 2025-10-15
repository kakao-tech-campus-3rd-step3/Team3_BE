package com.shootdoori.match.service;

import com.shootdoori.match.dto.AuthToken;
import com.shootdoori.match.dto.LoginRequest;
import com.shootdoori.match.dto.ProfileCreateRequest;
import com.shootdoori.match.entity.common.DeviceType;
import com.shootdoori.match.entity.user.User;
import com.shootdoori.match.exception.common.UnauthorizedException;
import com.shootdoori.match.repository.RefreshTokenRepository;
import com.shootdoori.match.util.JwtUtil;
import com.shootdoori.match.util.TokenIssuer;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.Collections;

@Service
public class AuthService {
    private static final Logger log = LoggerFactory.getLogger(AuthService.class);
    private final JwtUtil jwtUtil;
    private final ProfileService profileService;
    private final PasswordEncoder passwordEncoder;
    private final RefreshTokenRepository refreshTokenRepository;
    private final TokenIssuer tokenIssuer;

    private static final String AUTH_HEADER = "Authorization";
    private static final String BEARER_PREFIX = "Bearer ";

    public AuthService(JwtUtil jwtUtil, ProfileService profileService, PasswordEncoder passwordEncoder, RefreshTokenRepository refreshTokenRepository, TokenIssuer tokenIssuer) {
        this.jwtUtil = jwtUtil;
        this.profileService = profileService;
        this.passwordEncoder = passwordEncoder;
        this.refreshTokenRepository = refreshTokenRepository;
        this.tokenIssuer = tokenIssuer;
    }

    @Transactional
    public AuthToken register(ProfileCreateRequest request, HttpServletRequest httpServletRequest) {
        profileService.createProfile(request);
        User savedUser = profileService.findByEmail(request.email())
            .orElseThrow(() -> new UnauthorizedException("회원가입에 실패하였습니다."));

        return issueTokens(savedUser, httpServletRequest);
    }

    @Transactional
    public AuthToken login(LoginRequest request, HttpServletRequest httpServletRequest) {
        User user = profileService.findByEmail(request.email())
            .orElseThrow(() -> new UnauthorizedException("잘못된 이메일 또는 비밀번호입니다."));
        user.validatePassword(request.password(), passwordEncoder);

        return issueTokens(user, httpServletRequest);
    }

    @Transactional
    public void logout(String refreshTokenValue) {
        String tokenId = jwtUtil.getClaims(refreshTokenValue).getId();
        refreshTokenRepository.findById(tokenId).ifPresent(refreshTokenRepository::delete);
    }

    @Transactional
    public void logoutAll(Long userId) {
        refreshTokenRepository.deleteAllByUserId(userId);
    }

    @Transactional(readOnly = true)
    public UsernamePasswordAuthenticationToken authenticationToken(HttpServletRequest request) {
        final String authorizationHeader = request.getHeader(AUTH_HEADER);

        if (authorizationHeader != null && authorizationHeader.startsWith(BEARER_PREFIX)) {
            String jwt = authorizationHeader.substring(BEARER_PREFIX.length());

            try {
                if (jwtUtil.validateToken(jwt) && SecurityContextHolder.getContext().getAuthentication() == null) {
                    String userId = jwtUtil.getUserId(jwt);
                    Long principalUserId = Long.parseLong(userId);

                    return new UsernamePasswordAuthenticationToken(
                        principalUserId, null, Collections.emptyList());
                }
            } catch (JwtException e) {
                log.warn("Invalid JWT Token: {}", e.getMessage());
            }
        }
        return null;
    }

    private AuthToken issueTokens(User user, HttpServletRequest httpServletRequest) {
        String userAgent = httpServletRequest.getHeader("User-Agent");
        DeviceType deviceType = parseDeviceTypeFromUserAgent(userAgent);
        return tokenIssuer.issue(user, deviceType, userAgent);
    }

    private DeviceType parseDeviceTypeFromUserAgent(String userAgent) {
        if (!StringUtils.hasText(userAgent)) {
            return DeviceType.UNKNOWN;
        }

        String lowerCaseUserAgent = userAgent.toLowerCase();
        if (lowerCaseUserAgent.contains("android")) {
            return DeviceType.ANDROID;
        }

        if (lowerCaseUserAgent.contains("iphone") || lowerCaseUserAgent.contains("ipad")) {
            return DeviceType.IOS;
        }

        return DeviceType.WEB;
    }
}