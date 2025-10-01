package com.shootdoori.match.dto;

public record ResetPasswordRequest(String token, String password) {
}
