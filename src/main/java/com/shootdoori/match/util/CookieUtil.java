package com.shootdoori.match.util;

import org.springframework.http.ResponseCookie;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import jakarta.servlet.http.HttpServletResponse;
import com.shootdoori.match.policy.CookieSecurityPolicy;

public final class CookieUtil {

    private static final Logger logger = LoggerFactory.getLogger(CookieUtil.class);

    private CookieUtil() {}

    public static void setHttpOnlyCookie(
            HttpServletResponse response,
            String name,
            String value,
            long maxAgeSeconds,
            CookieSecurityPolicy policy
    ) {
        try {
            ResponseCookie cookie = ResponseCookie.from(name, value)
                    .httpOnly(true)
                    .secure(policy.isSecure())
                    .sameSite("Lax")
                    .maxAge(maxAgeSeconds)
                    .path("/")
                    .build();
            response.addHeader("Set-Cookie", cookie.toString());
            logger.debug("Set cookie: {} (secure: {})", name, policy.isSecure());
        } catch (Exception e) {
            logger.error("Error setting cookie: {}", e.getMessage(), e);
        }
    }

    public static void clearHttpOnlyCookie(
            HttpServletResponse response,
            String name,
            CookieSecurityPolicy policy
    ) {
        ResponseCookie cookie = ResponseCookie.from(name, "")
                .httpOnly(true)
                .secure(policy.isSecure())
                .sameSite("Lax")
                .maxAge(0)
                .path("/")
                .build();
        response.addHeader("Set-Cookie", cookie.toString());
        logger.debug("Cleared cookie: {} (secure: {})", name, policy.isSecure());
    }
}
