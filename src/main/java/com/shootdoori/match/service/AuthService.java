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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;

@Service
public class AuthService {

    private static final Logger log = LoggerFactory.getLogger(AuthService.class);

    private final JwtUtil jwtUtil;
    private final ProfileService profileService;
    private final RefreshTokenRepository refreshTokenRepository;
    private final TokenIssuer tokenIssuer;

    private static final String BEARER_PREFIX = "Bearer ";

    public AuthService(JwtUtil jwtUtil, ProfileService profileService, RefreshTokenRepository refreshTokenRepository, TokenIssuer tokenIssuer) {
        this.jwtUtil = jwtUtil;
        this.profileService = profileService;
        this.refreshTokenRepository = refreshTokenRepository;
        this.tokenIssuer = tokenIssuer;
    }

    @Transactional
    public AuthToken register(ProfileCreateRequest request, ClientInfo clientInfo) {
        log.debug("[AuthService] Register request received for email={}", request.email());
        profileService.createProfile(request);
        User savedUser = profileService.findByEmail(request.email())
            .orElseThrow(() -> new UnauthorizedException(ErrorCode.FAIL_REGISTER));

        return issueTokens(savedUser, clientInfo);
    }

    @Transactional
    public AuthToken login(LoginRequest request, ClientInfo clientInfo) {
        log.debug("[AuthService] Login attempt for email={}", request.email());
        User user = profileService.findByEmail(request.email())
            .orElseThrow(() -> new UnauthorizedException(ErrorCode.FAIL_LOGIN));
        user.validatePasswordMatches(request.password());

        return issueTokens(user, clientInfo);
    }

    @Transactional
    public void logout(String refreshTokenValue) {
        String tokenId = jwtUtil.getClaims(refreshTokenValue).getId();
        log.info("[AuthService] Logout requested (tokenId prefix={})", tokenId.length() > 8 ? tokenId.substring(0, 8) : tokenId);

        refreshTokenRepository.findById(tokenId).ifPresent(refreshTokenRepository::delete);
    }

    @Transactional
    public void logoutAll(Long userId) {
        log.info("[AuthService] Logout all sessions (userId={})", userId);
        refreshTokenRepository.deleteAllByUserId(userId);
    }

    public UsernamePasswordAuthenticationToken authenticationToken(String authorizationHeader) {

        if (authorizationHeader != null && authorizationHeader.startsWith(BEARER_PREFIX)) {
            String jwt = authorizationHeader.substring(BEARER_PREFIX.length());

            try {
                if (jwtUtil.validateToken(jwt) && SecurityContextHolder.getContext().getAuthentication() == null) {
                    String userId = jwtUtil.getUserId(jwt);
                    log.debug("[AuthService] Valid JWT for userId={}", userId);
                    Long principalUserId = Long.parseLong(userId);

                    return new UsernamePasswordAuthenticationToken(
                        principalUserId, null, Collections.emptyList());
                }
            } catch (io.jsonwebtoken.ExpiredJwtException e) {
                log.warn("[AuthService] Expired JWT detected");
                throw new UnauthorizedException(ErrorCode.EXPIRED_TOKEN);
            } catch (JwtException | NumberFormatException e) {
                log.warn("[AuthService] Invalid JWT detected (type={})", e.getClass().getSimpleName());
                throw new UnauthorizedException(ErrorCode.INVALID_TOKEN);
            }
        }
        return null;
    }

    private AuthToken issueTokens(User user, ClientInfo clientInfo) {
        log.info("[AuthService] Issuing tokens (userId={}, device={}, agent={})",
                user.getId(), clientInfo.deviceType(), clientInfo.userAgent());
        return tokenIssuer.issue(user, clientInfo.deviceType(), clientInfo.userAgent());
    }
}