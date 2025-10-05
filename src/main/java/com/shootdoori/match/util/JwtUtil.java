package com.shootdoori.match.util;

import com.shootdoori.match.entity.user.User;
import com.shootdoori.match.exception.common.UnauthorizedException;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Arrays;
import java.util.Date;
import java.util.UUID;

@Component
public class JwtUtil {

    private final Key secretKey;
    private final long accessTokenValidityInMilliseconds;
    private final long refreshTokenValidityInMilliseconds;
    private static final String BEARER = "Bearer ";

    public JwtUtil(@Value("${jwt.secret}") String secret,
                   @Value("${jwt.access-token-validity-in-seconds}") long accessTokenValidity,
                   @Value("${jwt.refresh-token-validity-in-seconds}") long refreshTokenValidity) {
        this.secretKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.accessTokenValidityInMilliseconds = accessTokenValidity * 1000;
        this.refreshTokenValidityInMilliseconds = refreshTokenValidity * 1000;
    }

    public String generateAccessToken(User user) {
        Date now = new Date();
        Date expirationTime = new Date(now.getTime() + accessTokenValidityInMilliseconds);

        return Jwts.builder()
            .setSubject(user.getId().toString())
            .setId(UUID.randomUUID().toString())
            .claim("email", user.getEmail())
            .setIssuedAt(now)
            .setExpiration(expirationTime)
            .signWith(secretKey, SignatureAlgorithm.HS256)
            .compact();
    }

    public String generateRefreshToken(User user) {
        Date now = new Date();
        Date expirationTime = new Date(now.getTime() + refreshTokenValidityInMilliseconds);

        return Jwts.builder()
            .setSubject(user.getId().toString())
            .setId(UUID.randomUUID().toString())
            .setIssuedAt(now)
            .setExpiration(expirationTime)
            .signWith(secretKey, SignatureAlgorithm.HS256)
            .compact();
    }

    public String extractToken(HttpServletRequest request) {
        String header = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (header != null && header.startsWith(BEARER)) {
            return header.substring(BEARER.length());
        }

        if (request.getCookies() != null) {
            return Arrays.stream(request.getCookies())
                .filter(c -> "accessToken".equals(c.getName()))
                .findFirst()
                .map(c -> {
                    try {
                        String v = URLDecoder.decode(c.getValue(), StandardCharsets.UTF_8);
                        if (v.startsWith(BEARER)) {
                            return v.substring(BEARER.length());
                        }
                        return v;
                    } catch (Exception ex) {
                        return null;
                    }
                })
                .orElse(null);
        }
        return null;
    }

    public boolean validateToken(String token) {
        try {
            Jwts.parserBuilder()
                .setSigningKey(secretKey)
                .build()
                .parseClaimsJws(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }

    public Claims getClaims(String token) {
        try {
            return Jwts.parserBuilder()
                .setSigningKey(secretKey)
                .build()
                .parseClaimsJws(token)
                .getBody();
        } catch (ExpiredJwtException ex) {
            throw new UnauthorizedException("토큰이 만료되었습니다.", ex);
        } catch (JwtException | IllegalArgumentException ex) {
            throw new UnauthorizedException("유효하지 않은 토큰입니다.", ex);
        }
    }

    public String getUserId(String token) {
        return getClaims(token).getSubject();
    }
}