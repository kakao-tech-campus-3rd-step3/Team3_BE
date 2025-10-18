package com.shootdoori.match.util;

import com.shootdoori.match.dto.AuthToken;
import com.shootdoori.match.entity.auth.RefreshToken;
import com.shootdoori.match.entity.common.DeviceType;
import com.shootdoori.match.entity.user.User;
import com.shootdoori.match.repository.RefreshTokenRepository;
import io.jsonwebtoken.Claims;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.ZoneId;

@Component
public class TokenIssuer {

    private final JwtUtil jwtUtil;
    private final RefreshTokenRepository refreshTokenRepository;

    public TokenIssuer(JwtUtil jwtUtil, RefreshTokenRepository refreshTokenRepository) {
        this.jwtUtil = jwtUtil;
        this.refreshTokenRepository = refreshTokenRepository;
    }

    public AuthToken issue(User user, DeviceType deviceType, String userAgent) {
        String accessToken = jwtUtil.generateAccessToken(user);
        String refreshTokenValue = jwtUtil.generateRefreshToken(user);

        Claims claims = jwtUtil.getClaims(refreshTokenValue);
        String tokenId = claims.getId();
        LocalDateTime expiryDate = claims.getExpiration().toInstant()
            .atZone(ZoneId.systemDefault()).toLocalDateTime();

        RefreshToken refreshToken = new RefreshToken(tokenId, user, expiryDate, deviceType, userAgent);
        refreshTokenRepository.save(refreshToken);

        return new AuthToken(accessToken, refreshTokenValue);
    }
}