package com.shootdoori.match.dto;

import java.time.LocalDateTime;

public record ProfileResponse(
    String name,
    String university,
    LocalDateTime createdAt
) {
}
