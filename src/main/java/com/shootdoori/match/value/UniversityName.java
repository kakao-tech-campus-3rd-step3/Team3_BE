package com.shootdoori.match.value;

import jakarta.persistence.Embeddable;
import java.util.Objects;

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

    @Override
    public int hashCode() {
        return name.hashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }

        if (other == null || getClass() != other.getClass()) {
            return false;
        }

        UniversityName that = (UniversityName) other;
        return this.name.equals(that.name());
    }
}
