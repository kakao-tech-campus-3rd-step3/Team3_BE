package com.shootdoori.match.service;

import com.shootdoori.match.dto.AuthToken;
import com.shootdoori.match.entity.auth.RefreshToken;
import com.shootdoori.match.exception.common.UnauthorizedException;
import com.shootdoori.match.repository.RefreshTokenRepository;
import com.shootdoori.match.util.JwtUtil;
import com.shootdoori.match.util.TokenIssuer;
import io.jsonwebtoken.Claims;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class TokenRefreshService {
    private final JwtUtil jwtUtil;
    private final RefreshTokenRepository refreshTokenRepository;
    private final TokenIssuer tokenIssuer;

    public TokenRefreshService(JwtUtil jwtUtil, RefreshTokenRepository refreshTokenRepository, TokenIssuer tokenIssuer) {
        this.jwtUtil = jwtUtil;
        this.refreshTokenRepository = refreshTokenRepository;
        this.tokenIssuer = tokenIssuer;
    }

    public AuthToken refreshAccessToken(String refreshToken) {
        Claims claims = jwtUtil.getClaims(refreshToken);
        String tokenId = claims.getId();

        RefreshToken storedToken = refreshTokenRepository.findById(tokenId)
            .orElseThrow(() -> new UnauthorizedException("유효하지 않은 리프레시 토큰"));

        if (storedToken.isExpired() || storedToken.isRevoked()) {
            refreshTokenRepository.delete(storedToken);
            throw new UnauthorizedException("만료된 토큰입니다. 다시 로그인해주세요.");
        }

        storedToken.revoke();

        return tokenIssuer.issue(storedToken.getUser(), storedToken.getDeviceType(), storedToken.getUserAgent());

    }
}