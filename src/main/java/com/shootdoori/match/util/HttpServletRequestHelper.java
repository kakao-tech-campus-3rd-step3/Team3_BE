package com.shootdoori.match.util;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Optional;

public final class HttpServletRequestHelper {

    private static final Logger logger = LoggerFactory.getLogger(HttpServletRequestHelper.class);

    private HttpServletRequestHelper() {}

    public static HttpServletRequest get() {
        return ((ServletRequestAttributes)
                RequestContextHolder.currentRequestAttributes()).getRequest();
    }

    public static String remoteIpAddress() {
        HttpServletRequest request = get();
        String ip = request.getHeader("X-FORWARDED-FOR");
        if (ip == null || ip.isBlank()) {
            ip = request.getRemoteAddr();
        }
        return ip.replaceAll("\\s*,.*", "");
    }

    public static String userAgent() {
        return get().getHeader("User-Agent");
    }

    public static Optional<Cookie> cookie(HttpServletRequest request, String name) {
        try {
            if (request.getCookies() == null) return Optional.empty();
            return Arrays.stream(request.getCookies())
                    .filter(c -> name.equals(c.getName()))
                    .findFirst();
        } catch (Exception e) {
            logger.warn("Failed to read cookies: {}", e.getMessage());
            return Optional.empty();
        }
    }

    public static Optional<String> cookieValue(HttpServletRequest request, String name) {
        return cookie(request, name)
                .map(Cookie::getValue)
                .flatMap(value -> {
                    try {
                        String decoded = URLDecoder.decode(value, StandardCharsets.UTF_8);
                        return decoded.isBlank() ? Optional.empty() : Optional.of(decoded);
                    } catch (Exception e) {
                        logger.warn("Failed to decode cookie value: {}", e.getMessage());
                        return Optional.empty();
                    }
                });
    }
}
