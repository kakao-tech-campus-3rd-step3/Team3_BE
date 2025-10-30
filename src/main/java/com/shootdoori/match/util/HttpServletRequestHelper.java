package com.shootdoori.match.util;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.Arrays;
import java.util.Optional;

public final class HttpServletRequestHelper {

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
            return Optional.empty();
        }
    }
}
