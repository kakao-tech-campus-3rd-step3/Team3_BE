package com.shootdoori.match.resolver;

import com.shootdoori.match.service.AuthService;
import com.shootdoori.match.util.JwtUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final String AUTH_HEADER = "Authorization";
    private static final String BEARER_PREFIX = "Bearer ";

    private final AuthService authService;
    private final JwtUtil jwtUtil;

    public JwtAuthenticationFilter(AuthService authService, JwtUtil jwtUtil) {
        this.authService = authService;
        this.jwtUtil = jwtUtil;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
        throws ServletException, IOException {
        
        String authorizationHeader = request.getHeader(AUTH_HEADER);

        if (authorizationHeader == null) {
            String tokenFromCookie = jwtUtil.extractToken(request);
            if (tokenFromCookie != null) {
                authorizationHeader = BEARER_PREFIX + tokenFromCookie;
            }
        }

        UsernamePasswordAuthenticationToken authenticationToken
            = authService.authenticationToken(authorizationHeader);

        if (authenticationToken != null) {
            SecurityContextHolder.getContext().setAuthentication(authenticationToken);
        }

        filterChain.doFilter(request, response);
    }
}