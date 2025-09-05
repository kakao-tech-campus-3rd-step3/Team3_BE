package com.shootdoori.match.dto;

public record ProfileCreateRequest(
    String name,
    String email,
    String universityEmail,
    String phoneNumber,
    String university,
    String department,
    String studentYear,
    String bio
) {
}
