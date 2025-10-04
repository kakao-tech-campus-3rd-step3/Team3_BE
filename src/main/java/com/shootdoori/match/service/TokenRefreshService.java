package com.shootdoori.match.service;

import com.shootdoori.match.dto.AuthToken;
import com.shootdoori.match.entity.auth.RefreshToken;
import com.shootdoori.match.entity.user.User;
import com.shootdoori.match.exception.common.UnauthorizedException;
import com.shootdoori.match.repository.RefreshTokenRepository;
import com.shootdoori.match.util.JwtUtil;
import io.jsonwebtoken.Claims;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.ZoneId;

@Service
@Transactional
public class TokenRefreshService {
    private final JwtUtil jwtUtil;
    private final RefreshTokenRepository refreshTokenRepository;

    public TokenRefreshService(JwtUtil jwtUtil, RefreshTokenRepository refreshTokenRepository) {
        this.jwtUtil = jwtUtil;
        this.refreshTokenRepository = refreshTokenRepository;
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

        User user = storedToken.getUser();
        String newAccessToken = jwtUtil.generateAccessToken(user);

        storedToken.revoke();

        String newRefreshTokenValue = jwtUtil.generateRefreshToken(user);
        Claims newClaims = jwtUtil.getClaims(newRefreshTokenValue);
        String newTokenId = newClaims.getId();
        LocalDateTime newExpiryDate = newClaims.getExpiration().toInstant()
            .atZone(ZoneId.systemDefault()).toLocalDateTime();

        RefreshToken newRefreshToken = new RefreshToken(
            newTokenId,
            user,
            newExpiryDate,
            storedToken.getDeviceType(),
            storedToken.getUserAgent()
        );

        refreshTokenRepository.save(newRefreshToken);

        return new AuthToken(newAccessToken, newRefreshTokenValue);
    }
}