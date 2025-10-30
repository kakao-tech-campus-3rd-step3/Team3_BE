package com.shootdoori.match.entity.common;

import org.springframework.util.StringUtils;

public enum DeviceType {
    ANDROID,
    IOS,
    WEB,
    UNKNOWN;

    public static DeviceType fromUserAgent(String userAgent) {
        if (!StringUtils.hasText(userAgent)) {
            return UNKNOWN;
        }

        String lower = userAgent.toLowerCase();
        if (lower.contains("android")) {
            return ANDROID;
        }
        if (lower.contains("iphone") || lower.contains("ipad")) {
            return IOS;
        }
        if (lower.contains("windows") || lower.contains("mac") || lower.contains("linux")) {
            return WEB;
        }

        return UNKNOWN;
    }
}