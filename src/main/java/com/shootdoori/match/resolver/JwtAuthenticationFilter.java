package com.shootdoori.match.resolver;

import com.shootdoori.match.exception.common.BusinessException;
import com.shootdoori.match.service.AuthService;
import com.shootdoori.match.util.JwtUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.servlet.HandlerExceptionResolver;

import java.io.IOException;

public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(JwtAuthenticationFilter.class);

    private static final String AUTH_HEADER = "Authorization";
    private static final String BEARER_PREFIX = "Bearer ";

    private final AuthService authService;
    private final JwtUtil jwtUtil;
    private final HandlerExceptionResolver handlerExceptionResolver;

    public JwtAuthenticationFilter(AuthService authService, JwtUtil jwtUtil, HandlerExceptionResolver handlerExceptionResolver) {
        this.authService = authService;
        this.jwtUtil = jwtUtil;
        this.handlerExceptionResolver = handlerExceptionResolver;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
        throws ServletException, IOException {

        try {
            String requestURI = request.getRequestURI();
            log.debug("[JwtFilter] Incoming request: {}", requestURI);

            String authorizationHeader = request.getHeader(AUTH_HEADER);
            if (authorizationHeader == null) {
                String tokenFromCookie = jwtUtil.extractToken(request);
                if (tokenFromCookie != null) {
                    authorizationHeader = BEARER_PREFIX + tokenFromCookie;
                    log.trace("[JwtFilter] Token extracted from cookie");
                }
            }

            UsernamePasswordAuthenticationToken authenticationToken =
                    authService.authenticationToken(authorizationHeader);

            if (authenticationToken != null) {
                SecurityContextHolder.getContext().setAuthentication(authenticationToken);
                log.trace("[JwtFilter] Authentication set for userId={}", authenticationToken.getPrincipal());
            }

            filterChain.doFilter(request, response);

        } catch (BusinessException e) {
            log.warn("[JwtFilter] Business exception: {}", e.getMessage());
            handlerExceptionResolver.resolveException(request, response, null, e);
            return;
        } catch (Exception e) {
            log.error("[JwtFilter] Unexpected error occurred: {}", e.getMessage(), e);
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write("{\"error\":\"Internal Server Error\", \"message\":\"" + e.getMessage() + "\"}");
        }
    }
}