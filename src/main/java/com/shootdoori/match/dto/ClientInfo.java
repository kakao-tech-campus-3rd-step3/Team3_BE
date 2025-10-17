package com.shootdoori.match.dto;

import com.shootdoori.match.entity.common.DeviceType;

public record ClientInfo(String userAgent, DeviceType deviceType) {
}
