package com.shootdoori.match.service;

import com.shootdoori.match.dto.AuthToken;
import com.shootdoori.match.dto.ClientInfo;
import com.shootdoori.match.dto.LoginRequest;
import com.shootdoori.match.dto.ProfileCreateRequest;
import com.shootdoori.match.entity.user.User;
import com.shootdoori.match.exception.common.ErrorCode;
import com.shootdoori.match.exception.common.UnauthorizedException;
import com.shootdoori.match.repository.RefreshTokenRepository;
import com.shootdoori.match.util.JwtUtil;
import com.shootdoori.match.util.TokenIssuer;
import io.jsonwebtoken.JwtException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;

@Service
public class AuthService {
    private static final Logger log = LoggerFactory.getLogger(AuthService.class);
    private final JwtUtil jwtUtil;
    private final ProfileService profileService;
    private final PasswordEncoder passwordEncoder;
    private final RefreshTokenRepository refreshTokenRepository;
    private final TokenIssuer tokenIssuer;

    private static final String BEARER_PREFIX = "Bearer ";

    public AuthService(JwtUtil jwtUtil, ProfileService profileService, PasswordEncoder passwordEncoder, RefreshTokenRepository refreshTokenRepository, TokenIssuer tokenIssuer) {
        this.jwtUtil = jwtUtil;
        this.profileService = profileService;
        this.passwordEncoder = passwordEncoder;
        this.refreshTokenRepository = refreshTokenRepository;
        this.tokenIssuer = tokenIssuer;
    }

    @Transactional
    public AuthToken register(ProfileCreateRequest request, ClientInfo clientInfo) {
        profileService.createProfile(request);
        User savedUser = profileService.findByEmail(request.email())
            .orElseThrow(() -> new UnauthorizedException(ErrorCode.FAIL_REGISTER));

        return issueTokens(savedUser, clientInfo);
    }

    @Transactional
    public AuthToken login(LoginRequest request, ClientInfo clientInfo) {
        User user = profileService.findByEmail(request.email())
            .orElseThrow(() -> new UnauthorizedException(ErrorCode.FAIL_LOGIN));
        user.validatePassword(request.password(), passwordEncoder);

        return issueTokens(user, clientInfo);
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

    public UsernamePasswordAuthenticationToken authenticationToken(String authorizationHeader) {

        if (authorizationHeader != null && authorizationHeader.startsWith(BEARER_PREFIX)) {
            String jwt = authorizationHeader.substring(BEARER_PREFIX.length());

            try {
                if (jwtUtil.validateToken(jwt) && SecurityContextHolder.getContext().getAuthentication() == null) {
                    String userId = jwtUtil.getUserId(jwt);
                    Long principalUserId = Long.parseLong(userId);

                    return new UsernamePasswordAuthenticationToken(
                        principalUserId, null, Collections.emptyList());
                }
            } catch (io.jsonwebtoken.ExpiredJwtException e) {
                throw new UnauthorizedException(ErrorCode.EXPIRED_TOKEN);
            } catch (JwtException | NumberFormatException e) {
                throw new UnauthorizedException(ErrorCode.INVALID_TOKEN);
            }
        }
        return null;
    }

    private AuthToken issueTokens(User user, ClientInfo clientInfo) {
        return tokenIssuer.issue(user, clientInfo.deviceType(), clientInfo.userAgent());
    }
}