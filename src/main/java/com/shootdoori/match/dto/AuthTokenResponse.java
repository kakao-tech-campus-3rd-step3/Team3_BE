package com.shootdoori.match.dto;

public record AuthTokenResponse(
        String accessToken,
        String refreshToken,
        long accessTokenExpiresIn,
        long refreshTokenExpiresIn
) {
}