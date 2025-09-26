package com.shootdoori.match.resolver;

import com.shootdoori.match.util.JwtUtil;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final String AUTH_HEADER = "Authorization";
    private static final String BEARER_PREFIX = "Bearer ";

    private final JwtUtil jwtUtil;

    public JwtAuthenticationFilter(JwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
        throws ServletException, IOException {

        final String authorizationHeader = request.getHeader(AUTH_HEADER);

        if (authorizationHeader != null && authorizationHeader.startsWith(BEARER_PREFIX)) {
            String jwt = authorizationHeader.substring(BEARER_PREFIX.length());

            try {
                if (jwtUtil.validateToken(jwt) && SecurityContextHolder.getContext().getAuthentication() == null) {
                    String userId = jwtUtil.getUserId(jwt);
                    Long principalUserId = Long.parseLong(userId);

                    UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(
                        principalUserId, null, Collections.emptyList());

                    SecurityContextHolder.getContext().setAuthentication(authenticationToken);
                }
            } catch (JwtException e) {
                logger.warn("Invalid JWT Token: " + e.getMessage());
            }
        }
        filterChain.doFilter(request, response);
    }
}