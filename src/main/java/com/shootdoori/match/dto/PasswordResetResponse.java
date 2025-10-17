package com.shootdoori.match.dto;

public record PasswordResetResponse(
    String message,
    long expiresInSeconds
) {
}
