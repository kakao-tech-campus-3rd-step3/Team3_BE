package com.shootdoori.match.service;

import com.shootdoori.match.dto.AuthToken;
import com.shootdoori.match.dto.LoginRequest;
import com.shootdoori.match.dto.ProfileCreateRequest;
import com.shootdoori.match.entity.DeviceType;
import com.shootdoori.match.entity.RefreshToken;
import com.shootdoori.match.entity.User;
import com.shootdoori.match.exception.UnauthorizedException;
import com.shootdoori.match.repository.RefreshTokenRepository;
import com.shootdoori.match.util.JwtUtil;
import io.jsonwebtoken.Claims;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.time.ZoneId;

@Service
public class AuthService {
    private final JwtUtil jwtUtil;
    private final ProfileService profileService;
    private final PasswordEncoder passwordEncoder;
    private final RefreshTokenRepository refreshTokenRepository;

    public AuthService(JwtUtil jwtUtil, ProfileService profileService, PasswordEncoder passwordEncoder, RefreshTokenRepository refreshTokenRepository) {
        this.jwtUtil = jwtUtil;
        this.profileService = profileService;
        this.passwordEncoder = passwordEncoder;
        this.refreshTokenRepository = refreshTokenRepository;
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

    private AuthToken issueTokens(User user, HttpServletRequest httpServletRequest) {
        String accessToken = jwtUtil.generateAccessToken(user);
        String refreshTokenValue = jwtUtil.generateRefreshToken(user);

        Claims claims = jwtUtil.getClaims(refreshTokenValue);
        String tokenId = claims.getId();
        LocalDateTime expiryDate = claims.getExpiration().toInstant()
            .atZone(ZoneId.systemDefault()).toLocalDateTime();
        String userAgent = httpServletRequest.getHeader("User-Agent");
        DeviceType deviceType = parseDeviceTypeFromUserAgent(userAgent);

        RefreshToken refreshToken = new RefreshToken(tokenId, user, expiryDate, deviceType, userAgent);
        refreshTokenRepository.save(refreshToken);

        return new AuthToken(accessToken, refreshTokenValue);
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