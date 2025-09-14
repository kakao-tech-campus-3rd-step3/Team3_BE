package com.shootdoori.match.value;

import jakarta.persistence.Embeddable;

@Embeddable
public record UniversityName(String name) {

    private static final int MAX_UNIVERSITY_NAME = 100;

    public UniversityName {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("대학교는 필수입니다.");
        }
        if (name.length() > MAX_UNIVERSITY_NAME) {
            throw new IllegalArgumentException("대학교명은 최대 100자입니다.");
        }
    }

    public static UniversityName of(String name) {
        return new UniversityName(name);
    }
}
